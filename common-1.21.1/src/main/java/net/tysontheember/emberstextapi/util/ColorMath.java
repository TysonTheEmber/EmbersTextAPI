package net.tysontheember.emberstextapi.util;

public final class ColorMath {

    private ColorMath() {
    }

    public static float[] rgbToHsv(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float h, s;
        float d = max - min;
        s = max == 0 ? 0 : d / max;

        if (d == 0) {
            h = 0;
        } else if (max == r) {
            h = (g - b) / d + (g < b ? 6 : 0);
        } else if (max == g) {
            h = (b - r) / d + 2;
        } else {
            h = (r - g) / d + 4;
        }
        h /= 6f;
        return new float[]{h, s, max};
    }

    public static float[] rgbToHsv(float[] rgb) {
        return rgbToHsv(rgb[0], rgb[1], rgb[2]);
    }

    public static float[] hsvToRgb(float h, float s, float v) {
        int i = (int) (h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        return switch (i % 6) {
            case 0 -> new float[]{v, t, p};
            case 1 -> new float[]{q, v, p};
            case 2 -> new float[]{p, v, t};
            case 3 -> new float[]{p, q, v};
            case 4 -> new float[]{t, p, v};
            default -> new float[]{v, p, q};
        };
    }

    public static int hsvToRgbPacked(float h, float s, float v) {
        float[] rgb = hsvToRgb(h, s, v);
        int r = (int) (rgb[0] * 255);
        int g = (int) (rgb[1] * 255);
        int b = (int) (rgb[2] * 255);
        return (r << 16) | (g << 8) | b;
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static float lerpHue(float a, float b, float t) {
        float diff = (b - a + 1f) % 1f;
        if (diff > 0.5f) {
            diff -= 1f;
        }
        return (a + diff * t + 1f) % 1f;
    }

    public static float[] lerpRgbViaHsv(float[] rgb1, float[] rgb2, float t) {
        float[] hsv1 = rgbToHsv(rgb1);
        float[] hsv2 = rgbToHsv(rgb2);

        float h = lerpHue(hsv1[0], hsv2[0], t);
        float s = lerp(hsv1[1], hsv2[1], t);
        float v = lerp(hsv1[2], hsv2[2], t);

        return hsvToRgb(h, s, v);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float normalize(float value, float fromMin, float fromMax, float toMin, float toMax) {
        float t = (value - fromMin) / (fromMax - fromMin);
        return lerp(toMin, toMax, t);
    }
}
