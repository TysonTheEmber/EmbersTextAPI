package net.tysontheember.emberstextapi.immersivemessages.effects.util;

import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;

/**
 * Utility methods for color manipulation and conversion.
 * Centralizes color-related operations used across multiple effects.
 */
public class ColorUtil {

    /**
     * Combines an ImmersiveColor with an alpha multiplier to produce an ARGB integer.
     *
     * @param color The base color
     * @param alphaMultiplier Alpha multiplier [0, 1]
     * @return ARGB color integer
     */
    public static int applyAlpha(ImmersiveColor color, float alphaMultiplier) {
        int a = Math.min(255, (int)(color.getAlpha() * alphaMultiplier));
        return (a << 24) | color.getRGB();
    }

    /**
     * Combines a text style color value with an alpha value to produce an ARGB integer.
     *
     * @param rgbColor RGB color value (no alpha channel)
     * @param alpha Alpha value [0, 1]
     * @return ARGB color integer
     */
    public static int combineWithAlpha(int rgbColor, float alpha) {
        int a = (int)(alpha * 255);
        return (a << 24) | (rgbColor & 0xFFFFFF);
    }

    /**
     * Extracts RGB components from a combined ARGB or RGB integer.
     *
     * @param color ARGB or RGB color integer
     * @return RGB value with alpha channel zeroed out
     */
    public static int extractRGB(int color) {
        return color & 0xFFFFFF;
    }

    /**
     * Extracts the alpha component from an ARGB color integer.
     *
     * @param color ARGB color integer
     * @return Alpha value [0, 255]
     */
    public static int extractAlpha(int color) {
        return (color >> 24) & 0xFF;
    }

    /**
     * Converts a float alpha [0, 1] to an integer alpha [0, 255].
     *
     * @param alpha Float alpha value
     * @return Integer alpha value, clamped to [0, 255]
     */
    public static int alphaFloatToInt(float alpha) {
        return Math.min(255, Math.max(0, (int)(alpha * 255)));
    }
}
