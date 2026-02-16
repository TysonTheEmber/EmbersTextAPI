package net.tysontheember.emberstextapi.immersivemessages.effects.color;

import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;

/**
 * Utility class for calculating multi-stop color gradients.
 * Supports smooth color interpolation across text characters.
 */
public class GradientCalculator {

    /**
     * Linearly interpolates between two RGB colors.
     *
     * @param start Starting color (RGB, no alpha)
     * @param end Ending color (RGB, no alpha)
     * @param t Interpolation factor [0, 1] where 0 = start, 1 = end
     * @return Interpolated RGB color
     */
    public static int lerpColor(int start, int end, float t) {
        int sr = (start >> 16) & 0xFF;
        int sg = (start >> 8) & 0xFF;
        int sb = start & 0xFF;
        int er = (end >> 16) & 0xFF;
        int eg = (end >> 8) & 0xFF;
        int eb = end & 0xFF;
        int r = (int) Mth.lerp(t, sr, er);
        int g = (int) Mth.lerp(t, sg, eg);
        int b = (int) Mth.lerp(t, sb, eb);
        return (r << 16) | (g << 8) | b;
    }

    /**
     * Builds an array of gradient colors for each character in text.
     * Supports multi-stop gradients with smooth interpolation between stops.
     *
     * @param textLength Number of characters in the text
     * @param gradientStops Array of color stops defining the gradient
     * @return Array of TextColor values, one per character
     */
    public static TextColor[] buildGradientColors(int textLength, TextColor[] gradientStops) {
        TextColor[] gradientColors = new TextColor[textLength];
        if (gradientStops == null || gradientStops.length < 2) {
            return gradientColors; // Empty array if invalid gradient
        }

        int segments = gradientStops.length - 1;
        for (int i = 0; i < textLength; i++) {
            // Calculate position in gradient [0, 1]
            float t = textLength <= 1 ? 0f : i / (float) (textLength - 1);

            // Scale to segment count and find which segment we're in
            float scaled = t * segments;
            int idx = Mth.clamp((int) Math.floor(scaled), 0, segments - 1);
            float local = scaled - idx;

            // Interpolate between the two stops in this segment
            int start = gradientStops[idx].getValue();
            int end = gradientStops[idx + 1].getValue();
            int rgb = lerpColor(start, end, local);
            gradientColors[i] = TextColor.fromRgb(rgb);
        }

        return gradientColors;
    }

    /**
     * Gets the color for a specific character index from a gradient color array.
     * Returns null if the gradient colors are invalid or index is out of bounds.
     *
     * @param gradientColors Pre-computed gradient color array
     * @param charIndex Character index
     * @return TextColor for this character, or null if not available
     */
    public static TextColor getColorAt(TextColor[] gradientColors, int charIndex) {
        if (gradientColors == null || charIndex < 0 || charIndex >= gradientColors.length) {
            return null;
        }
        return gradientColors[charIndex];
    }
}
