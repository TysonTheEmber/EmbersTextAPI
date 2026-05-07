package net.tysontheember.emberstextapi.sdf;

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
