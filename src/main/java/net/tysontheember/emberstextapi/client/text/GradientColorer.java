package net.tysontheember.emberstextapi.client.text;

import java.util.function.IntFunction;

/**
 * Utility methods for gradient color computations.
 */
public final class GradientColorer {
    private GradientColorer() {
    }

    public static int rgbLerp(int startRgb, int endRgb, float t) {
        float clamped = clamp(t);
        int sr = (startRgb >> 16) & 0xFF;
        int sg = (startRgb >> 8) & 0xFF;
        int sb = startRgb & 0xFF;
        int er = (endRgb >> 16) & 0xFF;
        int eg = (endRgb >> 8) & 0xFF;
        int eb = endRgb & 0xFF;
        int r = lerp(sr, er, clamped);
        int g = lerp(sg, eg, clamped);
        int b = lerp(sb, eb, clamped);
        return (r << 16) | (g << 8) | b;
    }

    public static int hsvLerp(int startRgb, int endRgb, float t) {
        float clamped = clamp(t);
        float[] start = java.awt.Color.RGBtoHSB((startRgb >> 16) & 0xFF, (startRgb >> 8) & 0xFF, startRgb & 0xFF, null);
        float[] end = java.awt.Color.RGBtoHSB((endRgb >> 16) & 0xFF, (endRgb >> 8) & 0xFF, endRgb & 0xFF, null);
        float hue = interpolateHue(start[0], end[0], clamped);
        float saturation = start[1] + (end[1] - start[1]) * clamped;
        float brightness = start[2] + (end[2] - start[2]) * clamped;
        return java.awt.Color.HSBtoRGB(hue, clamp01(saturation), clamp01(brightness)) & 0xFFFFFF;
    }

    public static float tFor(SpanNode node, int logicalIndex) {
        int length = Math.max(1, node.getEnd() - node.getStart() - 1);
        float numerator = logicalIndex - node.getStart();
        return clamp(numerator / (float) length);
    }

    public static int sample(SpanNode node, int fromRgb, int toRgb, boolean hsv, int logicalIndex, float tHint) {
        int span = Math.max(1, node.getEnd() - node.getStart());
        final int denominator = Math.max(1, span - 1);
        IntFunction<int[]> factory = len -> {
            int[] colors = new int[len];
            for (int i = 0; i < len; i++) {
                float position = len == 1 ? 0.0f : (float) i / (float) denominator;
                colors[i] = hsv ? hsvLerp(fromRgb, toRgb, position) : rgbLerp(fromRgb, toRgb, position);
            }
            return colors;
        };
        int[] cached = SpanCache.getGradientColors(node, span, factory);
        int offset = Math.max(0, Math.min(span - 1, logicalIndex - node.getStart()));
        if (offset >= cached.length) {
            return hsv ? hsvLerp(fromRgb, toRgb, clamp(tHint)) : rgbLerp(fromRgb, toRgb, clamp(tHint));
        }
        return cached[offset];
    }

    private static float interpolateHue(float start, float end, float t) {
        float delta = end - start;
        if (delta > 0.5f) {
            delta -= 1.0f;
        } else if (delta < -0.5f) {
            delta += 1.0f;
        }
        float hue = (start + delta * t) % 1.0f;
        if (hue < 0.0f) {
            hue += 1.0f;
        }
        return hue;
    }

    private static int lerp(int start, int end, float t) {
        return Math.round(start + (end - start) * t);
    }

    private static float clamp(float value) {
        if (value < 0.0f) {
            return 0.0f;
        }
        if (value > 1.0f) {
            return 1.0f;
        }
        return value;
    }

    private static float clamp01(float value) {
        return clamp(value);
    }
}
