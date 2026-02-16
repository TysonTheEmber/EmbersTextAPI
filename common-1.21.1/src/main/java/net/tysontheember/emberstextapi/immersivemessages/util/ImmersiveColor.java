package net.tysontheember.emberstextapi.immersivemessages.util;

/** Simple ARGB colour container with a few helpers. */
public class ImmersiveColor {
    private int argb;

    public static final ImmersiveColor WHITE = new ImmersiveColor(0xFFFFFFFF);
    public static final ImmersiveColor BLACK = new ImmersiveColor(0xFF000000);

    public ImmersiveColor(int argb) {
        this.argb = argb;
    }

    public ImmersiveColor(int r, int g, int b, int a) {
        this.argb = (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    public int getARGB() {
        return argb;
    }

    public int getRGB() {
        return argb & 0x00FFFFFF;
    }

    public ImmersiveColor mixWith(ImmersiveColor other, float amount) {
        int r = (int) (getRed() * (1 - amount) + other.getRed() * amount);
        int g = (int) (getGreen() * (1 - amount) + other.getGreen() * amount);
        int b = (int) (getBlue() * (1 - amount) + other.getBlue() * amount);
        int a = (int) (getAlpha() * (1 - amount) + other.getAlpha() * amount);
        return new ImmersiveColor(r, g, b, a);
    }

    public ImmersiveColor copy() {
        return new ImmersiveColor(argb);
    }

    public int getRed() { return (argb >> 16) & 0xFF; }
    public int getGreen() { return (argb >> 8) & 0xFF; }
    public int getBlue() { return argb & 0xFF; }
    public int getAlpha() { return (argb >> 24) & 0xFF; }
}
