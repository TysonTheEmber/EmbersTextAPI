package net.tysontheember.emberstextapi.client.text;

/**
 * Snapshot of per-glyph rendering state used by span effects.
 */
public final class EffectSettings {
    private int codePoint;
    private int baseRgba;
    private boolean shadow;
    private int glyphIndex;
    private boolean bold;
    private boolean italic;
    private float x;
    private float y;
    private float partialTicks;
    private float red;
    private float green;
    private float blue;
    private float alpha;

    public int getCodePoint() {
        return codePoint;
    }

    public void setCodePoint(int codePoint) {
        this.codePoint = codePoint;
    }

    public int getBaseRgba() {
        return baseRgba;
    }

    public void setBaseRgba(int baseRgba) {
        this.baseRgba = baseRgba;
        this.red = ((baseRgba >>> 16) & 0xFF) / 255.0F;
        this.green = ((baseRgba >>> 8) & 0xFF) / 255.0F;
        this.blue = (baseRgba & 0xFF) / 255.0F;
        this.alpha = ((baseRgba >>> 24) & 0xFF) / 255.0F;
    }

    public boolean isShadow() {
        return shadow;
    }

    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    public int getGlyphIndex() {
        return glyphIndex;
    }

    public void setGlyphIndex(int glyphIndex) {
        this.glyphIndex = glyphIndex;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getRed() {
        return red;
    }

    public void setRed(float red) {
        this.red = red;
        this.baseRgba = (this.baseRgba & 0xFF00FFFF) | (Math.max(0, Math.min(255, Math.round(red * 255.0F))) << 16);
    }

    public float getGreen() {
        return green;
    }

    public void setGreen(float green) {
        this.green = green;
        this.baseRgba = (this.baseRgba & 0xFFFF00FF) | (Math.max(0, Math.min(255, Math.round(green * 255.0F))) << 8);
    }

    public float getBlue() {
        return blue;
    }

    public void setBlue(float blue) {
        this.blue = blue;
        this.baseRgba = (this.baseRgba & 0xFFFFFF00) | Math.max(0, Math.min(255, Math.round(blue * 255.0F)));
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        this.baseRgba = (this.baseRgba & 0x00FFFFFF) | (Math.max(0, Math.min(255, Math.round(alpha * 255.0F))) << 24);
    }

    public void setColor(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        int r = Math.max(0, Math.min(255, Math.round(red * 255.0F)));
        int g = Math.max(0, Math.min(255, Math.round(green * 255.0F)));
        int b = Math.max(0, Math.min(255, Math.round(blue * 255.0F)));
        int a = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
        this.baseRgba = (a << 24) | (r << 16) | (g << 8) | b;
    }

    public int toPackedColor() {
        return (Math.max(0, Math.min(255, Math.round(this.alpha * 255.0F))) << 24)
                | (Math.max(0, Math.min(255, Math.round(this.red * 255.0F))) << 16)
                | (Math.max(0, Math.min(255, Math.round(this.green * 255.0F))) << 8)
                | Math.max(0, Math.min(255, Math.round(this.blue * 255.0F)));
    }

    public void reset() {
        this.codePoint = 0;
        this.baseRgba = 0;
        this.shadow = false;
        this.glyphIndex = 0;
        this.bold = false;
        this.italic = false;
        this.x = 0.0F;
        this.y = 0.0F;
        this.partialTicks = 0.0F;
        this.red = 0.0F;
        this.green = 0.0F;
        this.blue = 0.0F;
        this.alpha = 0.0F;
    }
}
