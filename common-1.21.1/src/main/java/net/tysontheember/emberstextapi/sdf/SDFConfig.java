package net.tysontheember.emberstextapi.sdf;

/**
 * Configuration for SDF/MSDF glyph generation.
 *
 * @param sdfResolution Base resolution of the SDF texture per glyph (default 48).
 *                      Controls the detail level of the MSDF — higher values produce
 *                      more detailed distance fields but use more atlas space.
 * @param padding       Extra pixels around the glyph in the SDF texture (default 4)
 * @param spread        Distance field spread in glyph units (default 4.0).
 *                      Deprecated: use {@code pxRange} instead. If spread is set but
 *                      pxRange is not, pxRange = spread * 2 (approximate mapping).
 * @param fontSize      Font size in points for FreeType rendering (default 16.0)
 * @param oversample    Display scale divisor: glyphs render at fontSize/oversample MC units.
 *                      Higher values = smaller display. Default 1.0.
 * @param shift         UV shift [x, y] (default [0, 0])
 * @param skip          Characters to skip (default "")
 * @param pxRange       MSDF pixel range — how many output pixels the distance field spans
 *                      on each side of the edge. Controls anti-aliasing width. Default 4.0.
 * @param angleThreshold Corner angle threshold in radians for MSDF edge coloring.
 *                       Angles sharper than this are treated as corners. Default 3.0 (≈171.9°).
 */
public record SDFConfig(
        int sdfResolution,
        int padding,
        float spread,
        float fontSize,
        float oversample,
        float[] shift,
        String skip,
        float pxRange,
        float angleThreshold
) {
    public static final int MAX_SDF_RESOLUTION = 128;

    /**
     * Backward-compatible constructor (pre-MSDF).
     * Maps spread to pxRange via approximate conversion.
     */
    public SDFConfig(int sdfResolution, int padding, float spread,
                     float fontSize, float oversample, float[] shift, String skip) {
        this(sdfResolution, padding, spread, fontSize, oversample, shift, skip,
                spread * 2.0f, 3.0f);
    }

    public static SDFConfig defaults() {
        return new SDFConfig(48, 4, 4.0f, 16.0f, 1.0f, new float[]{0, 0}, "",
                8.0f, 3.0f);
    }

    public int textureSize() {
        return sdfResolution + 2 * padding;
    }

    public SDFConfig validated() {
        int res = Math.min(Math.max(sdfResolution, 8), MAX_SDF_RESOLUTION);
        int pad = Math.min(Math.max(padding, 1), 16);
        float spr = Math.max(spread, 0.5f);
        float fs = Math.max(fontSize, 1.0f);
        float os = Math.max(oversample, 0.5f);
        float pr = Math.max(Math.min(pxRange, 32.0f), 2.0f);
        float at = Math.max(Math.min(angleThreshold, (float) Math.PI), 0.0f);
        return new SDFConfig(res, pad, spr, fs, os, shift, skip, pr, at);
    }
}
