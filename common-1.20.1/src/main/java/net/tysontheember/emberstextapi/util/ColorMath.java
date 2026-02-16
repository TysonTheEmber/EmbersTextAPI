package net.tysontheember.emberstextapi.util;

/**
 * Unified color mathematics utility for the Ember's Text API.
 * <p>
 * Provides centralized HSV/RGB conversion, interpolation, and color manipulation
 * functions used across all visual effects. This consolidates previously duplicated
 * code from {@link EffectUtil} and various effect implementations.
 * </p>
 *
 * @since 2.0.0
 */
public final class ColorMath {

    private ColorMath() {
        throw new UnsupportedOperationException("Utility class");
    }

    // ===== HSV <-> RGB Conversion =====

    /**
     * Convert RGB to HSV color space.
     * <p>
     * This is useful for color interpolation in effects like gradients,
     * where HSV interpolation produces more visually pleasing results
     * than linear RGB interpolation.
     * </p>
     *
     * @param r Red component (0.0-1.0)
     * @param g Green component (0.0-1.0)
     * @param b Blue component (0.0-1.0)
     * @return HSV color as float array [hue, saturation, value] where each component is 0.0-1.0
     */
    public static float[] rgbToHsv(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float h, s;
        float d = max - min;
        s = max == 0 ? 0 : d / max;

        if (d == 0) {
            h = 0; // Achromatic (gray)
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

    /**
     * Convert RGB to HSV color space.
     * Convenience overload that accepts an RGB array.
     *
     * @param rgb RGB color as float array [r, g, b]
     * @return HSV color as float array [h, s, v]
     */
    public static float[] rgbToHsv(float[] rgb) {
        return rgbToHsv(rgb[0], rgb[1], rgb[2]);
    }

    /**
     * Convert HSV to RGB color space.
     * <p>
     * Returns RGB components as floating-point values in the range 0.0-1.0.
     * This is the preferred method for effect calculations.
     * </p>
     *
     * @param h Hue (0.0-1.0)
     * @param s Saturation (0.0-1.0)
     * @param v Value/Brightness (0.0-1.0)
     * @return RGB color as float array [r, g, b] where each component is 0.0-1.0
     */
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

    /**
     * Convert HSV to packed RGB integer.
     * <p>
     * Returns a packed RGB color value (0xRRGGBB) suitable for use with
     * Minecraft's color rendering APIs.
     * </p>
     *
     * @param h Hue (0.0-1.0)
     * @param s Saturation (0.0-1.0)
     * @param v Value/Brightness (0.0-1.0)
     * @return Packed RGB color (0xRRGGBB)
     */
    public static int hsvToRgbPacked(float h, float s, float v) {
        float[] rgb = hsvToRgb(h, s, v);
        int r = (int) (rgb[0] * 255);
        int g = (int) (rgb[1] * 255);
        int b = (int) (rgb[2] * 255);
        return (r << 16) | (g << 8) | b;
    }

    // ===== Interpolation Functions =====

    /**
     * Linear interpolation between two values.
     * <p>
     * Also known as "lerp" or "mix". This is the fundamental interpolation
     * function used throughout the effects system.
     * </p>
     *
     * @param a Start value
     * @param b End value
     * @param t Interpolation factor (0.0-1.0)
     * @return Interpolated value where t=0 returns a, t=1 returns b
     */
    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /**
     * Interpolate hue values with wraparound.
     * <p>
     * Unlike standard lerp, this takes the shortest path around the hue circle.
     * For example, interpolating from red (0.0) to blue (0.67) will go through
     * purple rather than through yellow and green.
     * </p>
     *
     * @param a Start hue (0.0-1.0)
     * @param b End hue (0.0-1.0)
     * @param t Interpolation factor (0.0-1.0)
     * @return Interpolated hue (0.0-1.0)
     */
    public static float lerpHue(float a, float b, float t) {
        float diff = (b - a + 1f) % 1f;
        if (diff > 0.5f) {
            diff -= 1f; // Take shorter path around the hue circle
        }
        return (a + diff * t + 1f) % 1f;
    }

    /**
     * Interpolate between two RGB colors in HSV space.
     * <p>
     * This produces smoother, more natural-looking color transitions than
     * linear RGB interpolation, especially for saturated colors.
     * </p>
     *
     * @param rgb1 Start color as float array [r, g, b]
     * @param rgb2 End color as float array [r, g, b]
     * @param t Interpolation factor (0.0-1.0)
     * @return Interpolated color as float array [r, g, b]
     */
    public static float[] lerpRgbViaHsv(float[] rgb1, float[] rgb2, float t) {
        float[] hsv1 = rgbToHsv(rgb1);
        float[] hsv2 = rgbToHsv(rgb2);

        float h = lerpHue(hsv1[0], hsv2[0], t);
        float s = lerp(hsv1[1], hsv2[1], t);
        float v = lerp(hsv1[2], hsv2[2], t);

        return hsvToRgb(h, s, v);
    }

    // ===== Utility Functions =====

    /**
     * Clamp a value to a specified range.
     * <p>
     * Ensures the value is within [min, max]. If the value is less than min,
     * returns min. If greater than max, returns max. Otherwise returns the value unchanged.
     * </p>
     *
     * @param value Value to clamp
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @return Clamped value
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamp an integer value to a specified range.
     *
     * @param value Value to clamp
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @return Clamped value
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Normalize a value from one range to another.
     * <p>
     * Maps a value from the range [fromMin, fromMax] to [toMin, toMax].
     * Useful for converting between different coordinate systems or value ranges.
     * </p>
     *
     * @param value Value to normalize
     * @param fromMin Source range minimum
     * @param fromMax Source range maximum
     * @param toMin Target range minimum
     * @param toMax Target range maximum
     * @return Normalized value
     */
    public static float normalize(float value, float fromMin, float fromMax, float toMin, float toMax) {
        float t = (value - fromMin) / (fromMax - fromMin);
        return lerp(toMin, toMax, t);
    }
}
