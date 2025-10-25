package net.tysontheember.emberstextapi.mixin.client;

final class GlyphContextHolder {
    static final ThreadLocal<GlyphContext> CONTEXT = ThreadLocal.withInitial(GlyphContext::new);

    private GlyphContextHolder() {
    }

    static final class GlyphContext {
        int index;
        int codePoint;

        void clear() {
            this.index = 0;
            this.codePoint = 0;
        }
    }
}
