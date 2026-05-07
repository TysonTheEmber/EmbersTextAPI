package net.tysontheember.emberstextapi.immersivemessages.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class ColorParser {

    private ColorParser() {

    }

    @Nullable
    public static ImmersiveColor parseImmersiveColor(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String v = value.trim();

        Integer hexResult = parseHexColor(v);
        if (hexResult != null) {
            return new ImmersiveColor(hexResult);
        }

        ChatFormatting fmt = ChatFormatting.getByName(v);
        if (fmt != null && fmt.getColor() != null) {
            return new ImmersiveColor(0xFF000000 | fmt.getColor());
        }

        TextColor parsed = TextColor.parseColor(v);
        if (parsed != null) {
            int c = parsed.getValue();

            if ((c & 0xFF000000) == 0) {
                c |= 0xFF000000;
            }
            return new ImmersiveColor(c);
        }

        return null;
    }

    @Nullable
    public static TextColor parseTextColor(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String v = value.trim();

        ChatFormatting fmt = ChatFormatting.getByName(v);
        if (fmt != null) {
            return TextColor.fromLegacyFormat(fmt);
        }

        Integer hex = parseHexColor(v);
        if (hex != null) {

            return TextColor.fromRgb(hex & 0xFFFFFF);
        }

        return TextColor.parseColor(v);
    }

    @NotNull
    public static Optional<float[]> parseToRgbFloats(@Nullable String value) {
        if (value == null) {
            return Optional.empty();
        }

        String s = value.trim();

        if (s.startsWith("#")) {
            s = s.substring(1);
        }

        if (s.startsWith("0x") || s.startsWith("0X")) {
            s = s.substring(2);
        }

        try {

            if (s.length() == 3) {
                s = String.valueOf(s.charAt(0)) + s.charAt(0) +
                    s.charAt(1) + s.charAt(1) +
                    s.charAt(2) + s.charAt(2);
            }

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

    @NotNull
    public static Optional<float[]> parseToRgbaFloats(@Nullable String value) {
        if (value == null) {
            return Optional.empty();
        }

        String s = value.trim();

        if (s.startsWith("#")) {
            s = s.substring(1);
        }

        if (s.startsWith("0x") || s.startsWith("0X")) {
            s = s.substring(2);
        }

        try {
            if (s.length() == 3) {
                s = String.valueOf(s.charAt(0)) + s.charAt(0) +
                    s.charAt(1) + s.charAt(1) +
                    s.charAt(2) + s.charAt(2);
            }

            if (s.length() == 6) {
                int val = Integer.parseInt(s, 16);
                float r = ((val >> 16) & 0xFF) / 255f;
                float g = ((val >> 8) & 0xFF) / 255f;
                float b = (val & 0xFF) / 255f;
                return Optional.of(new float[]{r, g, b, 1.0f});
            }

            if (s.length() == 8) {
                long val = Long.parseLong(s, 16);
                float r = ((val >> 24) & 0xFF) / 255f;
                float g = ((val >> 16) & 0xFF) / 255f;
                float b = ((val >> 8) & 0xFF) / 255f;
                float a = (val & 0xFF) / 255f;
                return Optional.of(new float[]{r, g, b, a});
            }

            return Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Nullable
    public static Integer parseHexColor(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String v = value.trim();

        if (v.startsWith("#")) {
            v = v.substring(1);
        }

        if (v.startsWith("0x") || v.startsWith("0X")) {
            v = v.substring(2);
        }

        try {
            if (v.length() == 8) {

                return (int) Long.parseLong(v, 16);
            } else if (v.length() == 6) {

                return 0xFF000000 | Integer.parseInt(v, 16);
            } else if (v.length() == 3) {

                String expanded = String.valueOf(v.charAt(0)) + v.charAt(0) +
                                  v.charAt(1) + v.charAt(1) +
                                  v.charAt(2) + v.charAt(2);
                return 0xFF000000 | Integer.parseInt(expanded, 16);
            }
        } catch (NumberFormatException e) {

        }

        return null;
    }

    @NotNull
    public static float[] intToRgbFloats(int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;
        return new float[]{r, g, b};
    }

    public static int rgbFloatsToInt(float r, float g, float b) {
        int ri = (int) (Math.max(0f, Math.min(1f, r)) * 255);
        int gi = (int) (Math.max(0f, Math.min(1f, g)) * 255);
        int bi = (int) (Math.max(0f, Math.min(1f, b)) * 255);
        return (ri << 16) | (gi << 8) | bi;
    }

    public static int rgbaFloatsToInt(float r, float g, float b, float a) {
        int ai = (int) (Math.max(0f, Math.min(1f, a)) * 255);
        int ri = (int) (Math.max(0f, Math.min(1f, r)) * 255);
        int gi = (int) (Math.max(0f, Math.min(1f, g)) * 255);
        int bi = (int) (Math.max(0f, Math.min(1f, b)) * 255);
        return (ai << 24) | (ri << 16) | (gi << 8) | bi;
    }
}
