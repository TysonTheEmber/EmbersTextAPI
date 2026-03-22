package net.tysontheember.emberstextapi.sdf;

import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;

/**
 * SheetGlyphInfo implementation for MSDF glyphs.
 * Handles uploading 3-channel MSDF byte data to the GL atlas texture.
 * <p>
 * FontTextureMixin detects instances of this class to swap the GlyphRenderTypes
 * on the resulting BakedGlyph to use the SDF shader.
 */
public class SDFSheetGlyphInfo implements SheetGlyphInfo {

    /**
     * Extra padding (in pixels) added around the MSDF data when uploading to the atlas.
     * Must be ≥ 2 so GL_LINEAR's 2×2 kernel at the MSDF data boundary only samples
     * from controlled padding, never from adjacent atlas memory. The padding is filled
     * with edge-clamped MSDF values (like GL_CLAMP_TO_EDGE within the atlas) to prevent
     * bilinear interpolation between real distance data and zeros, which causes AA fringe.
     */
    private static final int ATLAS_PAD = 2;

    private final byte[] msdfData;
    private final int width;
    private final int height;
    private final float bearingLeft;
    private final float bearingTop;
    private final float oversample;

    /**
     * @param msdfData    3-channel MSDF data (RGB, 3 bytes per pixel, row-major).
     *                    Each channel encodes signed distance to edges of that color.
     *                    128 = on edge, &gt;128 = inside, &lt;128 = outside.
     * @param width       Texture width in pixels
     * @param height      Texture height in pixels
     * @param bearingLeft Horizontal bearing in MC units
     * @param bearingTop  Vertical bearing in MC units
     * @param oversample  Texture pixels per MC unit
     */
    public SDFSheetGlyphInfo(byte[] msdfData, int width, int height,
                              float bearingLeft, float bearingTop, float oversample) {
        this.msdfData = msdfData;
        this.width = width;
        this.height = height;
        this.bearingLeft = bearingLeft;
        this.bearingTop = bearingTop;
        this.oversample = oversample;
    }

    @Override
    public int getPixelWidth() {
        return width + 2 * ATLAS_PAD;
    }

    @Override
    public int getPixelHeight() {
        return height + 2 * ATLAS_PAD;
    }

    @Override
    public float getOversample() {
        return oversample;
    }

    @Override
    public float getBearingLeft() {
        return bearingLeft - ATLAS_PAD / oversample;
    }

    @Override
    public float getBearingTop() {
        return bearingTop - ATLAS_PAD / oversample;
    }

    @Override
    public boolean isColored() {
        // Must return true so FontTexture uses GL_RGBA format (not GL_RED).
        // MSDF requires all 3 color channels (RGB) to reconstruct sharp edges
        // via median-of-three in the fragment shader. With GL_RED, the G and B
        // channels are discarded, causing median(r,0,0) = 0 → invisible glyphs.
        return true;
    }

    @Override
    public void upload(int x, int y) {
        int paddedW = width + 2 * ATLAS_PAD;
        int paddedH = height + 2 * ATLAS_PAD;
        NativeImage image = new NativeImage(NativeImage.Format.RGBA, paddedW, paddedH, false);
        try {
            // Write MSDF data with edge-clamped padding. Instead of zero-filling
            // the padding (which means "far outside glyph" in MSDF encoding), we
            // copy the nearest MSDF edge texel — like GL_CLAMP_TO_EDGE within the
            // atlas. This prevents bilinear filtering at the MSDF data boundary
            // from interpolating between real distance values and zero, which can
            // produce ghost low-alpha fragments that appear as an AA fringe artifact.
            for (int py = 0; py < paddedH; py++) {
                int srcY = Math.max(0, Math.min(py - ATLAS_PAD, height - 1));
                for (int px = 0; px < paddedW; px++) {
                    int srcX = Math.max(0, Math.min(px - ATLAS_PAD, width - 1));
                    int idx = (srcY * width + srcX) * 3;
                    int r = idx < msdfData.length ? (msdfData[idx] & 0xFF) : 0;
                    int g = idx + 1 < msdfData.length ? (msdfData[idx + 1] & 0xFF) : 0;
                    int b = idx + 2 < msdfData.length ? (msdfData[idx + 2] & 0xFF) : 0;

                    // NativeImage.setPixelRGBA uses ABGR format. A=0 so any
                    // non-SDF shader that renders this texture sees transparent.
                    int pixel = (b << 16) | (g << 8) | r;
                    image.setPixelRGBA(px, py, pixel);
                }
            }
            // Upload the padded image to the GL texture at the atlas position.
            // blur=true sets GL_LINEAR filtering on the atlas texture — CRITICAL for SDF.
            // Without bilinear filtering, the SDF produces hard pixel-stepping artifacts
            // instead of smooth anti-aliased edges.
            image.upload(0, x, y, 0, 0, paddedW, paddedH, true, false);
        } finally {
            image.close();
        }
    }
}
