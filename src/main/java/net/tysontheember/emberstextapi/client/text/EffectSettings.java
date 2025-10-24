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
    }
}
