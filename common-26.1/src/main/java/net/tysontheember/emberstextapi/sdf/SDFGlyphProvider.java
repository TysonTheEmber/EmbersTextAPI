package net.tysontheember.emberstextapi.sdf;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.lwjgl.util.freetype.FreeType.*;

public class SDFGlyphProvider implements GlyphProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/SDFGlyphProvider");
    private static final int MAX_CACHE_SIZE = 4096;

    private final FT_Face ftFace;
    private final ByteBuffer fontData;
    private final SDFConfig config;
    private final IntSet supportedGlyphs;
    private final int unitsPerEM;
    private final int ascender;
    private final Map<Integer, SDFGlyphInfo> glyphCache;
    private final Map<Integer, PreBakedMSDF> preBakeCache;
    private final IntSet unsupportedGlyphs;
    private volatile boolean closed;

    public SDFGlyphProvider(FT_Face ftFace, ByteBuffer fontData, SDFConfig config) {
        this.ftFace = ftFace;
        this.fontData = fontData;
        this.config = config.validated();

        this.unitsPerEM = ftFace.units_per_EM();
        this.ascender = (int) ftFace.ascender();

        this.supportedGlyphs = buildSupportedGlyphs(ftFace, config.skip());

        this.unsupportedGlyphs = new IntOpenHashSet();

        this.glyphCache = Collections.synchronizedMap(new LinkedHashMap<>(256, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, SDFGlyphInfo> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        });

        long fontHash = hashFontData(fontData);
        PreBakedMSDFCache.CacheKey cacheKey = new PreBakedMSDFCache.CacheKey(
                fontHash,
                this.config.sdfResolution(),
                this.config.padding(),
                this.config.spread(),
                this.config.fontSize(),
                this.config.oversample(),
                this.config.pxRange(),
                this.config.angleThreshold(),
                this.config.shift()[0],
                this.config.shift()[1],
                this.config.skip());
        this.preBakeCache = PreBakedMSDFCache.getOrCreate(cacheKey);

        LOGGER.info("SDF glyph provider initialized: {} supported glyphs, {} upem, ascender={}",
                supportedGlyphs.size(), unitsPerEM, ascender);

        if (!isPreBakeCoverageComplete()) {
            preBakeCommonGlyphs();
        }
    }

    private static long hashFontData(ByteBuffer fontData) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(fontData.duplicate());
            byte[] digest = md.digest();
            long h = 0L;
            for (int i = 0; i < 8; i++) {
                h = (h << 8) | (digest[i] & 0xFFL);
            }
            return h;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private boolean isPreBakeCoverageComplete() {
        for (int cp = 32; cp <= 255; cp++) {
            if (!supportedGlyphs.contains(cp)) continue;
            if (!preBakeCache.containsKey(cp)) return false;
        }
        return true;
    }

    private void preBakeCommonGlyphs() {
        CompletableFuture.runAsync(() -> {
            FreeTypeManager ft = FreeTypeManager.getInstance();
            int count = 0;
            for (int cp = 32; cp <= 255; cp++) {
                if (closed) break;
                if (!supportedGlyphs.contains(cp)) continue;
                if (preBakeCache.containsKey(cp)) continue;
                try {
                    PreBakedMSDF data = computeMSDF(cp, ft);
                    if (data != null && preBakeCache.putIfAbsent(cp, data) == null) {
                        count++;
                    }
                } catch (Exception e) {
                    LOGGER.warn("Pre-bake failed for codepoint {} ('{}')", cp, (char) cp, e);
                }
            }
            LOGGER.info("Pre-baked {} SDF glyphs", count);
        }, Util.backgroundExecutor());
    }

    @Nullable
    PreBakedMSDF computeMSDF(int codepoint, FreeTypeManager ft) {
        int glyphIndex = ft.getCharIndex(ftFace, codepoint);
        if (glyphIndex == 0) return null;

        GlyphOutline outline = ft.extractOutline(ftFace, glyphIndex);
        if (outline == null || outline.contours().isEmpty()) return null;

        float glyphW = outline.width();
        float glyphH = outline.height();
        float maxDim = Math.max(glyphW, glyphH);
        if (maxDim < 1.0f) maxDim = 1.0f;

        float pxRange = config.pxRange();
        int sdfRes = config.sdfResolution();

        int texW, texH;
        if (glyphW >= glyphH) {
            texW = sdfRes;
            texH = Math.max(1, Math.round(sdfRes * glyphH / glyphW));
        } else {
            texH = sdfRes;
            texW = Math.max(1, Math.round(sdfRes * glyphW / glyphH));
        }
        int padPx = (int) Math.ceil(pxRange);
        texW += 2 * padPx;
        texH += 2 * padPx;

        EdgeColoring.ColoredContour[] colored = EdgeColoring.colorEdges(outline, config.angleThreshold());
        byte[] msdfData = MSDFGenerator.generate(
                outline, colored, texW, texH,
                outline.minX(), outline.minY(),
                outline.maxX(), outline.maxY(),
                pxRange
        );

        float effectivePixelSize = (Math.max(texW, texH) - pxRange) * unitsPerEM / maxDim;
        float oversample = effectivePixelSize * config.oversample() / config.fontSize();

        float scaleToPixel = effectivePixelSize / unitsPerEM;
        float ftBearingX = outline.minX() * scaleToPixel - padPx;
        float ftBearingY = outline.maxY() * scaleToPixel + padPx;

        float bearingLeft = ftBearingX / oversample + config.shift()[0];
        float bearingTop = ftBearingY / oversample + config.shift()[1];

        return new PreBakedMSDF(msdfData, texW, texH, bearingLeft, bearingTop, oversample);
    }

    @Nullable
    PreBakedMSDF getPreBaked(int codepoint) {
        return preBakeCache.get(codepoint);
    }

    private static IntSet buildSupportedGlyphs(FT_Face face, String skip) {
        IntSet skipSet = new IntOpenHashSet();
        for (int i = 0; i < skip.length(); ) {
            int cp = skip.codePointAt(i);
            skipSet.add(cp);
            i += Character.charCount(cp);
        }

        IntSet glyphs = new IntOpenHashSet();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pIndex = stack.mallocInt(1);
            long charcode = FT_Get_First_Char(face, pIndex);
            while (pIndex.get(0) != 0) {
                int cp = (int) charcode;
                if (!skipSet.contains(cp)) {
                    glyphs.add(cp);
                }
                charcode = FT_Get_Next_Char(face, charcode, pIndex);
            }
        }

        return glyphs;
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return supportedGlyphs;
    }

    @Nullable
    @Override
    public com.mojang.blaze3d.font.UnbakedGlyph getGlyph(int codepoint) {
        if (closed || !supportedGlyphs.contains(codepoint) || unsupportedGlyphs.contains(codepoint)) {
            return null;
        }

        SDFGlyphInfo cached = glyphCache.get(codepoint);
        if (cached != null) {
            return cached;
        }
        return createAndCacheGlyphInfo(codepoint);
    }

    @Nullable
    private SDFGlyphInfo createAndCacheGlyphInfo(int codepoint) {
        FreeTypeManager ft = FreeTypeManager.getInstance();
        int glyphIndex = ft.getCharIndex(ftFace, codepoint);
        if (glyphIndex == 0) {
            unsupportedGlyphs.add(codepoint);
            return null;
        }

        long advanceUnits = ft.getGlyphAdvance(ftFace, glyphIndex);

        float scale = config.fontSize() / unitsPerEM;
        float advance = advanceUnits * scale / config.oversample();

        SDFGlyphInfo info = new SDFGlyphInfo(
                advance,
                ftFace,
                config,
                unitsPerEM,
                codepoint,
                glyphIndex,
                this
        );
        glyphCache.put(codepoint, info);
        return info;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            glyphCache.clear();
            FreeTypeManager.getInstance().closeFace(ftFace);
            MemoryUtil.memFree(fontData);
            LOGGER.debug("SDF glyph provider closed");
        }
    }
}
