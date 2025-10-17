package net.tysontheember.emberstextapi.text;

import java.util.Objects;

/**
 * Provides monotonic timing information for text animations.
 */
public final class TextAnimationClock {
    private static TimeProvider provider = new SystemTimeProvider();

    private TextAnimationClock() {
    }

    public static float now() {
        return provider.now();
    }

    public static void setProvider(TimeProvider provider) {
        TextAnimationClock.provider = Objects.requireNonNull(provider, "provider");
    }

    public interface TimeProvider {
        float now();
    }

    private static final class SystemTimeProvider implements TimeProvider {
        private final long start = System.nanoTime();

        @Override
        public float now() {
            long nanos = System.nanoTime() - start;
            return nanos / 1_000_000_000f;
        }
    }
}
