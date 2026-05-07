package net.tysontheember.emberstextapi.immersivemessages.effects.util;

import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;

public class ColorUtil {

    public static int applyAlpha(ImmersiveColor color, float alphaMultiplier) {
        int a = Math.min(255, (int)(color.getAlpha() * alphaMultiplier));
        return (a << 24) | color.getRGB();
    }

    public static int combineWithAlpha(int rgbColor, float alpha) {
        int a = (int)(alpha * 255);
        return (a << 24) | (rgbColor & 0xFFFFFF);
    }

    public static int extractRGB(int color) {
        return color & 0xFFFFFF;
    }

    public static int extractAlpha(int color) {
        return (color >> 24) & 0xFF;
    }

    public static int alphaFloatToInt(float alpha) {
        return Math.min(255, Math.max(0, (int)(alpha * 255)));
    }
}
