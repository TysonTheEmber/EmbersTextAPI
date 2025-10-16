package net.tysontheember.emberstextapi.text;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Runtime animation hook applied to individual glyphs. Implementations mutate
 * the {@link GlyphState} in response to the current render time.
 */
public interface TextEffect {
    /**
     * Applies the effect to the supplied glyph state.
     *
     * @param context per-glyph context information.
     * @param state   mutable glyph state.
     */
    void apply(GlyphContext context, GlyphState state);

    /**
     * Compile time metadata provided to {@link TextAttributeFactory} instances.
     */
    record CompileContext(AttributedText text, Span span, DrawStyle style,
                          BiConsumer<String, Throwable> warningSink) {
        public CompileContext {
            Objects.requireNonNull(text, "text");
            Objects.requireNonNull(span, "span");
            Objects.requireNonNull(style, "style");
        }
    }

    /**
     * Per-glyph context used while applying effects.
     */
    record GlyphContext(int glyphIndex, int spanIndex, int spanLocalIndex, int spanLength,
                        char glyph, float timeSeconds, long seed, AttributedText text, Span span) {
        public float spanProgress() {
            if (spanLength <= 1) return 0f;
            return Math.max(0f, Math.min(1f, spanLocalIndex / (float) (spanLength - 1)));
        }
    }

    /**
     * Mutable state for the glyph being drawn.
     */
    final class GlyphState {
        private int color;
        private float alpha;
        private float offsetX;
        private float offsetY;
        private float scale;
        private boolean shadow;
        private float shadowOffsetX;
        private float shadowOffsetY;
        private float shadowAlpha;
        private boolean bold;
        private boolean italic;
        private boolean visible;

        public GlyphState(int color, boolean shadow) {
            this.color = color;
            this.alpha = 1f;
            this.scale = 1f;
            this.shadow = shadow;
            this.shadowOffsetX = 1f;
            this.shadowOffsetY = 1f;
            this.shadowAlpha = 1f;
            this.visible = true;
        }

        public int color() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public float alpha() {
            return alpha;
        }

        public void setAlpha(float alpha) {
            this.alpha = Math.max(0f, Math.min(1f, alpha));
        }

        public float offsetX() {
            return offsetX;
        }

        public void setOffsetX(float offsetX) {
            this.offsetX = offsetX;
        }

        public float offsetY() {
            return offsetY;
        }

        public void setOffsetY(float offsetY) {
            this.offsetY = offsetY;
        }

        public float scale() {
            return scale;
        }

        public void setScale(float scale) {
            this.scale = Math.max(0f, scale);
        }

        public boolean shadow() {
            return shadow;
        }

        public void setShadow(boolean shadow) {
            this.shadow = shadow;
        }

        public float shadowOffsetX() {
            return shadowOffsetX;
        }

        public void setShadowOffset(float offsetX, float offsetY) {
            this.shadowOffsetX = offsetX;
            this.shadowOffsetY = offsetY;
        }

        public float shadowOffsetY() {
            return shadowOffsetY;
        }

        public float shadowAlpha() {
            return shadowAlpha;
        }

        public void setShadowAlpha(float shadowAlpha) {
            this.shadowAlpha = Math.max(0f, Math.min(1f, shadowAlpha));
        }

        public boolean bold() {
            return bold;
        }

        public void setBold(boolean bold) {
            this.bold = bold;
        }

        public boolean italic() {
            return italic;
        }

        public void setItalic(boolean italic) {
            this.italic = italic;
        }

        public boolean visible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public int resolvedColor() {
            int a = (color >> 24) & 0xFF;
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = color & 0xFF;
            a = Math.round(a * alpha);
            return (a << 24) | (r << 16) | (g << 8) | b;
        }
    }
}
