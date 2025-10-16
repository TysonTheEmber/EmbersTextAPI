package net.tysontheember.emberstextapi.text;

/**
 * Provides monotonic timing information for text animations.
 */
public final class TextAnimationClock {
    private static final long START_NANO_TIME = System.nanoTime();

    private TextAnimationClock() {
    }

    public static float now() {
        long nanos = System.nanoTime() - START_NANO_TIME;
        return nanos / 1_000_000_000f;
    }
}
