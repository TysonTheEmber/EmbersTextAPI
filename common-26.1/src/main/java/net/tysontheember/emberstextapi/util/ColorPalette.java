package net.tysontheember.emberstextapi.util;

public final class ColorPalette {

    public enum SampleMode { CLAMP, WRAP, PINGPONG }

    private final float[][] stops;
    private final float[] positions;
    private final boolean hsv;
    private final SampleMode mode;

    public ColorPalette(float[][] stops, float[] positions, boolean hsv, SampleMode mode) {
        if (stops.length == 0 || stops.length != positions.length) {
            throw new IllegalArgumentException("stops and positions must be non-empty and same length");
        }
        float[][] stopsCopy = new float[stops.length][];
        for (int i = 0; i < stops.length; i++) {
            stopsCopy[i] = stops[i].clone();
        }
        this.stops = stopsCopy;
        this.positions = positions.clone();
        this.hsv = hsv;
        this.mode = mode;
    }

    public int size() { return stops.length; }

    public float[] sample(float t) {
        float u = applyMode(t);

        if (stops.length == 1) {
            return stops[0].clone();
        }

        int lo = 0;
        int hi = stops.length - 1;
        if (u <= positions[0]) return stops[0].clone();
        if (u >= positions[hi]) return stops[hi].clone();

        while (hi - lo > 1) {
            int mid = (lo + hi) >>> 1;
            if (positions[mid] <= u) lo = mid; else hi = mid;
        }

        float span = positions[hi] - positions[lo];
        float local = span <= 0f ? 0f : (u - positions[lo]) / span;
        return hsv ? lerpHsv(stops[lo], stops[hi], local) : lerpRgb(stops[lo], stops[hi], local);
    }

    private float applyMode(float t) {
        switch (mode) {
            case WRAP: {
                return t - (float) Math.floor(t);
            }
            case PINGPONG: {
                float m = t - 2.0f * (float) Math.floor(t * 0.5f);
                return m > 1.0f ? 2.0f - m : m;
            }
            case CLAMP:
            default:
                return t < 0f ? 0f : (t > 1f ? 1f : t);
        }
    }

    private static float[] lerpRgb(float[] a, float[] b, float t) {
        return new float[]{
                a[0] + (b[0] - a[0]) * t,
                a[1] + (b[1] - a[1]) * t,
                a[2] + (b[2] - a[2]) * t,
                a[3] + (b[3] - a[3]) * t
        };
    }

    private static float[] lerpHsv(float[] a, float[] b, float t) {
        float[] ha = ColorMath.rgbToHsv(new float[]{a[0], a[1], a[2]});
        float[] hb = ColorMath.rgbToHsv(new float[]{b[0], b[1], b[2]});
        float h = ColorMath.lerpHue(ha[0], hb[0], t);
        float s = ha[1] + (hb[1] - ha[1]) * t;
        float v = ha[2] + (hb[2] - ha[2]) * t;
        float[] rgb = ColorMath.hsvToRgb(h, s, v);
        float alpha = a[3] + (b[3] - a[3]) * t;
        return new float[]{rgb[0], rgb[1], rgb[2], alpha};
    }
}
