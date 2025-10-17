package net.tysontheember.emberstextapi.text;

import net.minecraft.Util;

/**
 * Simple monotonic clock used when animating glyph effects.
 */
public final class TextAnimationClock {
    private final long startNanos;

    public TextAnimationClock() {
        this(Util.getNanos());
    }

    public TextAnimationClock(long startNanos) {
        this.startNanos = startNanos;
    }

    public float nowSeconds() {
        long now = Util.getNanos();
        return (now - startNanos) / 1_000_000_000f;
    }

    public static TextAnimationClock create() {
        return new TextAnimationClock();
    }
}
