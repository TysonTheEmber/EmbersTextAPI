package net.tysontheember.emberstextapi.sdf;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Face;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link GlyphProvider} implementation that generates MSDF textures from TTF/OTF fonts.
 * <p>
 * Glyphs are created lazily as {@link SDFGlyphInfo} instances and cached in a
 * {@value MAX_CACHE_SIZE}-entry LRU cache. Actual MSDF texture generation happens
 * even later, during {@link SDFGlyphInfo#bake}, when the glyph is first rendered.
 * <p>
 * Lifecycle:
 * <ol>
 *   <li>{@link SDFGlyphProviderDefinition#load} parses the font JSON and calls
 *       {@link SDFGlyphProviderFactory} to create this provider</li>
 *   <li>Minecraft's font system calls {@link #getGlyph} per codepoint</li>
 *   <li>{@link SDFGlyphInfo#bake} extracts the outline, runs edge coloring
 *       and MSDF generation, then uploads the 3-channel texture to the atlas</li>
 * </ol>
 *
 * @see SDFGlyphInfo
 * @see SDFGlyphProviderDefinition
 */
public class SDFGlyphProvider implements GlyphProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/SDFGlyphProvider");
    private static final int MAX_CACHE_SIZE = 4096;

    private final FT_Face ftFace;
    private final ByteBuffer fontData; // Must keep alive for FreeType
    private final SDFConfig config;
    private final IntSet supportedGlyphs;
    private final int unitsPerEM;
    private final int ascender;
    private final Map<Integer, SDFGlyphInfo> glyphCache;
    private final ConcurrentHashMap<Integer, PreBakedMSDF> preBakeCache = new ConcurrentHashMap<>();
    private final IntSet unsupportedGlyphs;
    private boolean closed;

    public SDFGlyphProvider(FT_Face ftFace, ByteBuffer fontData, SDFConfig config) {
        this.ftFace = ftFace;
        this.fontData = fontData;
        this.config = config.validated();

        this.unitsPerEM = ftFace.units_per_EM();
        this.ascender = (int) ftFace.ascender();

        this.supportedGlyphs = buildSupportedGlyphs(ftFace, config.skip());

        this.unsupportedGlyphs = new IntOpenHashSet();

        // Simple LRU cache using LinkedHashMap
        this.glyphCache = new LinkedHashMap<>(256, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, SDFGlyphInfo> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };

        LOGGER.info("SDF glyph provider initialized: {} supported glyphs, {} upem, ascender={}",
                supportedGlyphs.size(), unitsPerEM, ascender);

        preBakeCommonGlyphs();
    }

    /**
     * Asynchronously pre-computes MSDF texture data for ASCII printable range and
     * common Latin-1 Supplement characters (codepoints 32–255). Runs on MC's background
     * executor so the provider constructor returns immediately.
     * <p>
     * If {@link #bake SDFGlyphInfo.bake()} is called before pre-computation finishes for
     * a given codepoint, the glyph falls through to on-demand computation.
     */
    private void preBakeCommonGlyphs() {
        CompletableFuture.runAsync(() -> {
            FreeTypeManager ft = FreeTypeManager.getInstance();
            int count = 0;
            for (int cp = 32; cp <= 255; cp++) {
                if (closed) break;
                if (!supportedGlyphs.contains(cp)) continue;
                try {
                    PreBakedMSDF data = computeMSDF(cp, ft);
                    if (data != null) {
                        preBakeCache.put(cp, data);
                        count++;
                    }
                } catch (Exception e) {
                    LOGGER.warn("Pre-bake failed for codepoint {} ('{}')", cp, (char) cp, e);
                }
            }
            LOGGER.info("Pre-baked {} SDF glyphs", count);
        }, Util.backgroundExecutor());
    }

    /**
     * Computes the MSDF texture data for a single codepoint.
     * <p>
     * This extracts the glyph outline, applies edge coloring, generates the 3-channel
     * MSDF texture, and computes bearings — the same work that {@link SDFGlyphInfo#bake}
     * does, but packaged as a {@link PreBakedMSDF} record for caching.
     *
     * @param codepoint Unicode codepoint to generate MSDF for
     * @param ft        FreeTypeManager instance (synchronized internally)
     * @return pre-computed MSDF data, or {@code null} for empty/unsupported glyphs
     */
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

        // Edge coloring and MSDF generation — pure functions, thread-safe
        EdgeColoring.ColoredContour[] colored = EdgeColoring.colorEdges(outline, config.angleThreshold());
        byte[] msdfData = MSDFGenerator.generate(
                outline, colored, texW, texH,
                outline.minX(), outline.minY(),
                outline.maxX(), outline.maxY(),
                pxRange
        );

        // Compute oversample and bearings (1.20.1 convention: bearingX, bearingY)
        float effectivePixelSize = (Math.max(texW, texH) - pxRange) * unitsPerEM / maxDim;
        float oversample = effectivePixelSize * config.oversample() / config.fontSize();

        float fontAscent = (float) ftFace.ascender() * effectivePixelSize / unitsPerEM;
        float scaleToPixel = effectivePixelSize / unitsPerEM;
        float ftBearingX = outline.minX() * scaleToPixel - padPx;
        float ftBearingY = fontAscent - outline.maxY() * scaleToPixel - padPx;

        float bearingX = ftBearingX / oversample + config.shift()[0];
        float bearingY = ftBearingY / oversample - config.shift()[1];

        return new PreBakedMSDF(msdfData, texW, texH, bearingX, bearingY, oversample);
    }

    /**
     * Returns pre-computed MSDF data for the given codepoint, or {@code null} if
     * pre-computation hasn't finished yet (or the codepoint wasn't in the pre-bake range).
     */
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
            long charcode = NativeFreeType.FT_Get_First_Char(face, pIndex);
            while (pIndex.get(0) != 0) {
                int cp = (int) charcode;
                if (!skipSet.contains(cp)) {
                    glyphs.add(cp);
                }
                charcode = NativeFreeType.FT_Get_Next_Char(face, charcode, pIndex);
            }
        }

        return glyphs;
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return supportedGlyphs;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a cached {@link SDFGlyphInfo} for the given codepoint, or creates and
     * caches a new one. Returns {@code null} if the font doesn't support this codepoint
     * or the provider has been closed.
     */
    @Nullable
    @Override
    public GlyphInfo getGlyph(int codepoint) {
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

        // Scale advance from font units to MC text coordinates
        float scale = config.fontSize() / unitsPerEM;
        float advance = advanceUnits * scale / config.oversample();

        // Pass the FT_Face to SDFGlyphInfo — SDF rendering happens lazily in bake()
        // Provider ref enables pre-bake cache lookup in bake()
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
            LOGGER.debug("SDF glyph provider closed");
        }
    }
}
