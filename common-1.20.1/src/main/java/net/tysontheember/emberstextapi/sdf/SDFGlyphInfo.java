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

/**
 * {@link GlyphInfo} implementation for MSDF (Multi-Channel Signed Distance Field) glyphs.
 * <p>
 * Each instance represents a single glyph from an SDF font provider. On {@link #bake},
 * the glyph outline is extracted via FreeType, edge-colored, and rendered as a 3-channel
 * MSDF texture by {@link MSDFGenerator}. The result is wrapped in an {@link SDFSheetGlyphInfo}
 * for atlas upload. {@code FontTextureMixin} detects this and swaps render types to SDF shaders.
 * <p>
 * <b>Debug mode:</b> Launch with {@code -Deta.sdf.debug=true} to dump each generated MSDF
 * texture as a PNG file to {@code <game_dir>/debug-sdf/}. Useful for diagnosing rendering
 * artifacts — inspect the dump to determine whether stray pixels originate in the MSDF data
 * or in atlas packing/shader rendering.
 *
 * @see SDFSheetGlyphInfo
 * @see SDFGlyphProvider
 * @see MSDFGenerator
 */
public class SDFGlyphInfo implements GlyphInfo {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/SDFGlyph");

    /**
     * When {@code true}, each baked glyph's MSDF texture is written to disk as a PNG.
     * Enabled via JVM property {@code -Deta.sdf.debug=true}.
     * Output directory: {@code <game_dir>/debug-sdf/glyph_<char>_gi<index>.png}
     */
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
        // Check pre-bake cache first to avoid expensive MSDF computation on the render thread
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

        // Extract glyph outline (in font units, no scaling)
        GlyphOutline outline = ft.extractOutline(ftFace, glyphIndex);

        if (outline == null || outline.contours().isEmpty()) {
            // Empty glyph (space, etc.) — use a minimal sheet glyph
            return baker.apply(new SDFSheetGlyphInfo(
                    new byte[3], 1, 1, 0, 0, 1.0f));
        }

        // Compute MSDF texture dimensions based on glyph metrics
        float glyphW = outline.width();
        float glyphH = outline.height();
        float maxDim = Math.max(glyphW, glyphH);
        if (maxDim < 1.0f) maxDim = 1.0f;

        float pxRange = config.pxRange();
        int sdfRes = config.sdfResolution();

        // Texture size scales with glyph aspect ratio
        // The longer dimension gets sdfRes pixels, shorter dimension scales proportionally
        int texW, texH;
        if (glyphW >= glyphH) {
            texW = sdfRes;
            texH = Math.max(1, Math.round(sdfRes * glyphH / glyphW));
        } else {
            texH = sdfRes;
            texW = Math.max(1, Math.round(sdfRes * glyphW / glyphH));
        }
        // Add padding for the pixel range
        int padPx = (int) Math.ceil(pxRange);
        texW += 2 * padPx;
        texH += 2 * padPx;

        // Edge coloring
        EdgeColoring.ColoredContour[] colored = EdgeColoring.colorEdges(outline, config.angleThreshold());

        // Generate 3-channel MSDF
        byte[] msdfData = MSDFGenerator.generate(
                outline, colored,
                texW, texH,
                outline.minX(), outline.minY(),
                outline.maxX(), outline.maxY(),
                pxRange
        );

        // DEBUG: Dump MSDF texture to PNG for inspection
        if (SDF_DEBUG) {
            debugDumpMSDF(msdfData, texW, texH, codepoint);
        }

        // Compute oversample and bearings
        // Oversample: texture pixels per MC text unit.
        // SDFGlyphProvider computes advance as:
        //   advance_mc = advanceUnits * fontSize / unitsPerEM / oversample_config
        // The MSDF texture maps maxDim in font units to (texSize - pxRange) pixels.
        // So: 1 font unit = (texSize - pxRange) / maxDim pixels
        // And: 1 MC text unit = advance_mc / (advanceUnits / unitsPerEM * fontSize)
        // oversample = pixelSize * oversample_config / fontSize
        // For MSDF: we use the effective pixel scale of the texture
        float effectivePixelSize = (Math.max(texW, texH) - pxRange) * unitsPerEM / maxDim;
        float oversample = effectivePixelSize * config.oversample() / config.fontSize();

        // Compute bearings using FreeType glyph metrics
        // Load glyph at a known pixel size to get bearings
        float fontAscent = (float) ftFace.ascender() * effectivePixelSize / unitsPerEM;

        // Map outline bounding box to pixel bearings
        // In the MSDF texture, the glyph is centered with pxRange/2 padding on each side
        // The left bearing = outline.minX() mapped to pixel coordinates minus half pxRange
        float scaleToPixel = effectivePixelSize / unitsPerEM;
        float ftBearingX = outline.minX() * scaleToPixel - padPx;
        float ftBearingY = fontAscent - outline.maxY() * scaleToPixel - padPx;

        // MC 1.20.1 bearing convention
        float bearingX = ftBearingX / oversample;
        float bearingY = ftBearingY / oversample;

        // Apply shift from config (in MC units)
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

    /**
     * Writes the MSDF texture data to a PNG file for visual inspection.
     * <p>
     * Output: {@code debug-sdf/glyph_<char>_gi<glyphIndex>.png} — an RGB image where
     * each channel encodes signed distance to edges of that color.
     * Median of RGB = edge threshold (128 = on edge).
     *
     * @param msdfData  raw MSDF byte array (row-major, 3 bytes per texel: RGB)
     * @param width     texture width in pixels
     * @param height    texture height in pixels
     * @param codepoint Unicode codepoint of the glyph
     */
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
                        // ABGR format for NativeImage
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
