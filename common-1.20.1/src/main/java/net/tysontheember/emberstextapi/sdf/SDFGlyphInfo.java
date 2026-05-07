package net.tysontheember.emberstextapi.sdf;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.lwjgl.util.freetype.FT_Face;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.function.Function;

public class SDFGlyphInfo implements GlyphInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/SDFGlyph");

    private static final boolean SDF_DEBUG = Boolean.getBoolean("eta.sdf.debug");

    private final float advance;
    private final FT_Face ftFace;
    private final SDFConfig config;
    private final int unitsPerEM;
    private final int codepoint;
    private final int glyphIndex;
    private final SDFGlyphProvider provider;

    public SDFGlyphInfo(float advance, FT_Face ftFace,
                         SDFConfig config, int unitsPerEM,
                         int codepoint, int glyphIndex,
                         SDFGlyphProvider provider) {
        this.advance = advance;
        this.ftFace = ftFace;
        this.config = config;
        this.unitsPerEM = unitsPerEM;
        this.codepoint = codepoint;
        this.glyphIndex = glyphIndex;
        this.provider = provider;
    }

    @Override
    public float getAdvance() {
        return advance;
    }

    @Override
    public float getShadowOffset() {
        return 0.5f;
    }

    @Override
    public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> baker) {

        PreBakedMSDF preBaked = provider != null ? provider.getPreBaked(codepoint) : null;
        if (preBaked != null) {
            LOGGER.debug("Using pre-baked MSDF for '{}' (cp={})", (char) codepoint, codepoint);
            if (SDF_DEBUG) {
                debugDumpMSDF(preBaked.msdfData(), preBaked.texW(), preBaked.texH(), codepoint);
            }
            return baker.apply(new SDFSheetGlyphInfo(
                    preBaked.msdfData(), preBaked.texW(), preBaked.texH(),
                    preBaked.bearingX(), preBaked.bearingY(), preBaked.oversample()
            ));
        }

        FreeTypeManager ft = FreeTypeManager.getInstance();

        GlyphOutline outline = ft.extractOutline(ftFace, glyphIndex);

        if (outline == null || outline.contours().isEmpty()) {

            return baker.apply(new SDFSheetGlyphInfo(
                    new byte[3], 1, 1, 0, 0, 1.0f));
        }

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
                outline, colored,
                texW, texH,
                outline.minX(), outline.minY(),
                outline.maxX(), outline.maxY(),
                pxRange
        );

        if (SDF_DEBUG) {
            debugDumpMSDF(msdfData, texW, texH, codepoint);
        }

        float effectivePixelSize = (Math.max(texW, texH) - pxRange) * unitsPerEM / maxDim;
        float oversample = effectivePixelSize * config.oversample() / config.fontSize();

        float fontAscent = (float) ftFace.ascender() * effectivePixelSize / unitsPerEM;

        float scaleToPixel = effectivePixelSize / unitsPerEM;
        float ftBearingX = outline.minX() * scaleToPixel - padPx;
        float ftBearingY = fontAscent - outline.maxY() * scaleToPixel - padPx;

        float bearingX = ftBearingX / oversample;
        float bearingY = ftBearingY / oversample;

        bearingX += config.shift()[0];
        bearingY -= config.shift()[1];

        LOGGER.debug("Bake '{}': MSDF tex={}x{} oversample={} bearingX={} bearingY={}",
                (char) codepoint, texW, texH, oversample, bearingX, bearingY);

        return baker.apply(new SDFSheetGlyphInfo(
                msdfData,
                texW,
                texH,
                bearingX,
                bearingY,
                oversample
        ));
    }

    private void debugDumpMSDF(byte[] msdfData, int width, int height, int codepoint) {
        try {
            String charStr = Character.isLetterOrDigit(codepoint)
                    ? String.valueOf((char) codepoint)
                    : String.format("U+%04X", codepoint);
            Path dir = Path.of("debug-sdf");
            Files.createDirectories(dir);
            Path file = dir.resolve(String.format("glyph_%s_gi%d.png", charStr, glyphIndex));

            NativeImage image = new NativeImage(NativeImage.Format.RGBA, width, height, false);
            try {
                for (int py = 0; py < height; py++) {
                    for (int px = 0; px < width; px++) {
                        int idx = (py * width + px) * 3;
                        int r = idx < msdfData.length ? (msdfData[idx] & 0xFF) : 0;
                        int g = idx + 1 < msdfData.length ? (msdfData[idx + 1] & 0xFF) : 0;
                        int b = idx + 2 < msdfData.length ? (msdfData[idx + 2] & 0xFF) : 0;

                        int pixel = 0xFF000000 | (b << 16) | (g << 8) | r;
                        image.setPixelRGBA(px, py, pixel);
                    }
                }
                image.writeToFile(file);
                LOGGER.info("DEBUG MSDF dump: '{}' (cp={}) -> {}", charStr, codepoint, file.toAbsolutePath());
            } finally {
                image.close();
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to dump MSDF debug texture for codepoint {}", codepoint, e);
        }
    }
}
