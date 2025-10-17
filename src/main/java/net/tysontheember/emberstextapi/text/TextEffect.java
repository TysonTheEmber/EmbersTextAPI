package net.tysontheember.emberstextapi.text;

/**
 * Runtime effect applied per glyph.
 */
public interface TextEffect {
    void apply(GlyphCtx glyph, SpanCtx span, float time);

    default void begin(SpanCtx span, float time) {
    }

    default void end(SpanCtx span, float time) {
    }

    /**
     * Mutable glyph state provided to effects.
     */
    class GlyphCtx {
        public float xOffset;
        public float yOffset;
        public float scale = 1f;
        public float alpha = 1f;
        public int color;
        public boolean shadow;
        public int glyphIndex;
        public int indexInSpan;

        public GlyphCtx copy() {
            GlyphCtx ctx = new GlyphCtx();
            ctx.xOffset = xOffset;
            ctx.yOffset = yOffset;
            ctx.scale = scale;
            ctx.alpha = alpha;
            ctx.color = color;
            ctx.shadow = shadow;
            ctx.glyphIndex = glyphIndex;
            ctx.indexInSpan = indexInSpan;
            return ctx;
        }
    }

    /**
     * Span level context for compiled effects.
     */
    class SpanCtx {
        private final Params params;
        private final Attribute attribute;
        private final int startIndex;
        private final int endIndex;

        public SpanCtx(Attribute attribute, Params params, int startIndex, int endIndex) {
            this.attribute = attribute;
            this.params = params;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        public Attribute attribute() {
            return attribute;
        }

        public Params params() {
            return params;
        }

        public int startIndex() {
            return startIndex;
        }

        public int endIndex() {
            return endIndex;
        }

        public int length() {
            return Math.max(0, endIndex - startIndex);
        }
    }
}
