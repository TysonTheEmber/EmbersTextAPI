package net.tysontheember.emberstextapi.immersivemessages.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Centralized color parsing utility for consistent color handling across the API.
 * <p>
 * Supports multiple color formats:
 * <ul>
 *   <li>Hex strings: "#FF0000", "FF0000", "#F00", "F00"</li>
 *   <li>Hex with prefix: "0xFF0000"</li>
 *   <li>8-digit ARGB hex: "#AARRGGBB", "AARRGGBB"</li>
 *   <li>Minecraft color names: "red", "gold", "dark_blue"</li>
 *   <li>TextColor format strings</li>
 * </ul>
 * </p>
 */
public final class ColorParser {

    private ColorParser() {
        // Utility class - no instantiation
    }

    /**
     * Parse a color string to an ImmersiveColor (ARGB format).
     * <p>
     * Handles hex colors (6 or 8 digit), Minecraft formatting names,
     * and TextColor format strings. Returns null if parsing fails.
     * </p>
     *
     * @param value Color string to parse
     * @return ImmersiveColor or null if parsing fails
     */
    @Nullable
    public static ImmersiveColor parseImmersiveColor(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String v = value.trim();

        // Try hex parsing first
        Integer hexResult = parseHexColor(v);
        if (hexResult != null) {
            return new ImmersiveColor(hexResult);
        }

        // Try Minecraft ChatFormatting name
        ChatFormatting fmt = ChatFormatting.getByName(value);
        if (fmt != null && fmt.getColor() != null) {
            return new ImmersiveColor(0xFF000000 | fmt.getColor());
        }

        // Try TextColor parsing (handles various formats)
        // In MC 1.21.1, parseColor returns DataResult<TextColor>
        TextColor parsed = TextColor.parseColor(value).result().orElse(null);
        if (parsed != null) {
            int c = parsed.getValue();
            // Ensure alpha channel is set
            if ((c & 0xFF000000) == 0) {
                c |= 0xFF000000;
            }
            return new ImmersiveColor(c);
        }

        return null;
    }

    /**
     * Parse a color string to a TextColor.
     * <p>
     * Handles hex colors (6-digit RGB), Minecraft formatting names,
     * and TextColor format strings. Returns null if parsing fails.
     * </p>
     *
     * @param value Color string to parse
     * @return TextColor or null if parsing fails
     */
    @Nullable
    public static TextColor parseTextColor(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String v = value.trim();

        // Try Minecraft ChatFormatting name first
        ChatFormatting fmt = ChatFormatting.getByName(v);
        if (fmt != null) {
            return TextColor.fromLegacyFormat(fmt);
        }

        // Try flexible hex parsing (supports #RRGGBB, RRGGBB, #RGB, RGB, 0xRRGGBB, 0xAARRGGBB)
        Integer hex = parseHexColor(v);
        if (hex != null) {
            // TextColor only supports RGB; strip any alpha channel if present
            return TextColor.fromRgb(hex & 0xFFFFFF);
        }

        // Fallback to vanilla parser (keeps support for future formats)
        // In MC 1.21.1, parseColor returns DataResult<TextColor>
        return TextColor.parseColor(v).result().orElse(null);
    }

    /**
     * Parse a hex color string to RGB float array.
     * <p>
     * Accepts formats:
     * <ul>
     *   <li>"FF0000" - 6-digit hex without hash</li>
     *   <li>"#FF0000" - 6-digit hex with hash</li>
     *   <li>"F00" - 3-digit short form (expanded to FF0000)</li>
     *   <li>"#F00" - 3-digit short form with hash</li>
     * </ul>
     * </p>
     *
     * @param value Color string to parse
     * @return Optional containing RGB array [r, g, b] in range 0.0-1.0, or empty if parsing fails
     */
    @NotNull
    public static Optional<float[]> parseToRgbFloats(@Nullable String value) {
        if (value == null) {
            return Optional.empty();
        }

        String s = value.trim();

        // Remove leading # if present
        if (s.startsWith("#")) {
            s = s.substring(1);
        }
        // Remove 0x prefix if present
        if (s.startsWith("0x") || s.startsWith("0X")) {
            s = s.substring(2);
        }

        try {
            // Handle short form (F00 -> FF0000)
            if (s.length() == 3) {
                s = String.valueOf(s.charAt(0)) + s.charAt(0) +
                    s.charAt(1) + s.charAt(1) +
                    s.charAt(2) + s.charAt(2);
            }

            // Only accept 6-digit RGB for float conversion
            if (s.length() != 6) {
                return Optional.empty();
            }

            int val = Integer.parseInt(s, 16);

            float r = ((val >> 16) & 0xFF) / 255f;
            float g = ((val >> 8) & 0xFF) / 255f;
            float b = (val & 0xFF) / 255f;

            return Optional.of(new float[]{r, g, b});
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Parse a hex color string with optional alpha to packed ARGB integer.
     * <p>
     * Accepts formats:
     * <ul>
     *   <li>"FF0000" - 6-digit RGB (alpha defaults to 0xFF)</li>
     *   <li>"#FF0000" - 6-digit RGB with hash</li>
     *   <li>"AARRGGBB" - 8-digit ARGB</li>
     *   <li>"#AARRGGBB" - 8-digit ARGB with hash</li>
     *   <li>"0xRRGGBB" or "0xAARRGGBB" - with 0x prefix</li>
     * </ul>
     * </p>
     *
     * @param value Color string to parse
     * @return Packed ARGB integer, or null if parsing fails
     */
    @Nullable
    public static Integer parseHexColor(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String v = value.trim();

        // Remove leading # if present
        if (v.startsWith("#")) {
            v = v.substring(1);
        }
        // Remove 0x prefix if present
        if (v.startsWith("0x") || v.startsWith("0X")) {
            v = v.substring(2);
        }

        try {
            if (v.length() == 8) {
                // AARRGGBB format
                return (int) Long.parseLong(v, 16);
            } else if (v.length() == 6) {
                // RRGGBB format - add full alpha
                return 0xFF000000 | Integer.parseInt(v, 16);
            } else if (v.length() == 3) {
                // Short form RGB (F00 -> FF0000)
                String expanded = String.valueOf(v.charAt(0)) + v.charAt(0) +
                                  v.charAt(1) + v.charAt(1) +
                                  v.charAt(2) + v.charAt(2);
                return 0xFF000000 | Integer.parseInt(expanded, 16);
            }
        } catch (NumberFormatException e) {
            // Fall through to return null
        }

        return null;
    }

    /**
     * Convert packed RGB integer to float array.
     *
     * @param color Packed RGB integer (0xRRGGBB format)
     * @return RGB array [r, g, b] in range 0.0-1.0
     */
    @NotNull
    public static float[] intToRgbFloats(int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        return new float[]{r, g, b};
    }

    /**
     * Convert RGB floats to packed integer.
     *
     * @param r Red (0.0-1.0)
     * @param g Green (0.0-1.0)
     * @param b Blue (0.0-1.0)
     * @return Packed RGB integer (0xRRGGBB format)
     */
    public static int rgbFloatsToInt(float r, float g, float b) {
        int ri = (int) (Math.max(0f, Math.min(1f, r)) * 255);
        int gi = (int) (Math.max(0f, Math.min(1f, g)) * 255);
        int bi = (int) (Math.max(0f, Math.min(1f, b)) * 255);
        return (ri << 16) | (gi << 8) | bi;
    }

    /**
     * Convert RGBA floats to packed ARGB integer.
     *
     * @param r Red (0.0-1.0)
     * @param g Green (0.0-1.0)
     * @param b Blue (0.0-1.0)
     * @param a Alpha (0.0-1.0)
     * @return Packed ARGB integer (0xAARRGGBB format)
     */
    public static int rgbaFloatsToInt(float r, float g, float b, float a) {
        int ai = (int) (Math.max(0f, Math.min(1f, a)) * 255);
        int ri = (int) (Math.max(0f, Math.min(1f, r)) * 255);
        int gi = (int) (Math.max(0f, Math.min(1f, g)) * 255);
        int bi = (int) (Math.max(0f, Math.min(1f, b)) * 255);
        return (ai << 24) | (ri << 16) | (gi << 8) | bi;
    }
}
