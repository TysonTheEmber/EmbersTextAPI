package net.tysontheember.emberstextapi.text;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Utility helpers for working with ARGB colour values.
 */
public final class ColorUtil {
    private ColorUtil() {
    }

    public static int parseColor(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Color value may not be null");
        }
        String value = raw.trim().toLowerCase(Locale.ROOT);
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Color value may not be blank");
        }
        if (value.startsWith("#")) {
            value = value.substring(1);
        } else if (value.startsWith("0x")) {
            value = value.substring(2);
        }
        if (value.length() == 6) {
            return (int) (0xFF000000L | Long.parseLong(value, 16));
        }
        if (value.length() == 8) {
            return (int) Long.parseLong(value, 16);
        }
        throw new IllegalArgumentException("Expected #RRGGBB or #AARRGGBB format: " + raw);
    }

    public static int lerpRgb(int from, int to, float t) {
        int a1 = (from >>> 24) & 0xFF;
        int r1 = (from >>> 16) & 0xFF;
        int g1 = (from >>> 8) & 0xFF;
        int b1 = from & 0xFF;
        int a2 = (to >>> 24) & 0xFF;
        int r2 = (to >>> 16) & 0xFF;
        int g2 = (to >>> 8) & 0xFF;
        int b2 = to & 0xFF;
        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int lerpHsv(int from, int to, float t) {
        float[] hsv1 = rgbToHsv(from);
        float[] hsv2 = rgbToHsv(to);
        float hueDelta = wrapDistance(hsv1[0], hsv2[0]);
        float h = hsv1[0] + hueDelta * t;
        float s = hsv1[1] + (hsv2[1] - hsv1[1]) * t;
        float v = hsv1[2] + (hsv2[2] - hsv1[2]) * t;
        float alpha = ((from >>> 24) & 0xFF) / 255f + (((to >>> 24) & 0xFF) / 255f - ((from >>> 24) & 0xFF) / 255f) * t;
        return hsvToRgb(h, s, v, alpha);
    }

    private static float wrapDistance(float a, float b) {
        float diff = (b - a + 3f) % 1f;
        if (diff > 0.5f) {
            diff -= 1f;
        }
        return diff;
    }

    private static int hsvToRgb(float h, float s, float v, float alpha) {
        h = (h % 1f + 1f) % 1f;
        int i = (int) Math.floor(h * 6f);
        float f = h * 6f - i;
        float p = v * (1f - s);
        float q = v * (1f - f * s);
        float t = v * (1f - (1f - f) * s);
        float r;
        float g;
        float b;
        switch (i % 6) {
            case 0 -> {
                r = v;
                g = t;
                b = p;
            }
            case 1 -> {
                r = q;
                g = v;
                b = p;
            }
            case 2 -> {
                r = p;
                g = v;
                b = t;
            }
            case 3 -> {
                r = p;
                g = q;
                b = v;
            }
            case 4 -> {
                r = t;
                g = p;
                b = v;
            }
            default -> {
                r = v;
                g = p;
                b = q;
            }
        }
        int ri = clampColor(r);
        int gi = clampColor(g);
        int bi = clampColor(b);
        int ai = clampColor(alpha);
        return (ai << 24) | (ri << 16) | (gi << 8) | bi;
    }

    private static int clampColor(float value) {
        return (int) (Math.max(0f, Math.min(1f, value)) * 255f + 0.5f);
    }

    @NotNull
    public static float[] rgbToHsv(int argb) {
        float r = ((argb >>> 16) & 0xFF) / 255f;
        float g = ((argb >>> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float h;
        float s;
        float v = max;
        float d = max - min;
        if (max == 0f) {
            s = 0f;
        } else {
            s = d / max;
        }
        if (d == 0f) {
            h = 0f;
        } else if (max == r) {
            h = (g - b) / d + (g < b ? 6f : 0f);
        } else if (max == g) {
            h = (b - r) / d + 2f;
        } else {
            h = (r - g) / d + 4f;
        }
        h /= 6f;
        return new float[]{h, s, v};
    }
}
