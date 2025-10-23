package net.tysontheember.emberstextapi.client.spans;

import net.minecraft.network.chat.TextColor;

import java.util.Objects;
import java.util.Random;

/**
 * Converts immutable span attributes into runtime deltas that can be consumed by render hooks.
 */
public final class EffectAdapter {
    private static final float TWO_PI = (float) (Math.PI * 2.0);
    private static final StyleDelta NO_DELTA = new StyleDelta(0f, 0f, null, null);

    private EffectAdapter() {
    }

    public static StyleDelta apply(int charIndex, int lineIndex, long timeMs, long seed, SpanAttr attr) {
        if (attr == null) {
            return NO_DELTA;
        }
        SpanAttr.EffectSpec spec = attr.effect();
        if (spec == null) {
            return NO_DELTA;
        }

        float dx = 0f;
        float dy = 0f;
        TextColor colour = null;
        SpanAttr.StyleFlags styleOverride = null;

        float ticks = timeMs / 50f; // Approximate 20 TPS timing

        if (spec.shake() != null) {
            float[] shake = computeShake(spec.shake(), ticks, 0f, seed);
            dx += shake[0];
            dy += shake[1];
        }

        if (spec.charShake() != null) {
            float indexOffset = charIndex + lineIndex * 32f;
            float[] shake = computeShake(spec.charShake(), ticks, indexOffset, mixSeed(seed, charIndex, lineIndex, timeMs));
            dx += shake[0];
            dy += shake[1];
        }

        return new StyleDelta(dx, dy, colour, styleOverride);
    }

    private static float[] computeShake(SpanAttr.EffectSpec.Shake shake, float ticks, float indexOffset, long seed) {
        Objects.requireNonNull(shake, "shake");
        float amplitude = valueOrDefault(shake.amplitude(), 0f);
        if (amplitude == 0f || shake.type() == null) {
            return new float[]{0f, 0f};
        }

        float speed = valueOrDefault(shake.speed(), 1f);
        float wavelength = valueOrDefault(shake.wavelength(), 1f);
        float phase = ticks * 0.05f * speed + indexOffset * 0.1f;

        return switch (shake.type()) {
            case WAVE -> new float[]{0f, (float) Math.sin(phase * TWO_PI / Math.max(0.0001f, wavelength)) * amplitude};
            case CIRCLE -> new float[]{
                (float) Math.cos(phase) * amplitude,
                (float) Math.sin(phase) * amplitude
            };
            case RANDOM -> sampleRandomShake(seed, amplitude);
        };
    }

    private static float[] sampleRandomShake(long seed, float amplitude) {
        Random random = new Random(seed);
        float sx = (random.nextFloat() - 0.5f) * 2f * amplitude;
        float sy = (random.nextFloat() - 0.5f) * 2f * amplitude;
        return new float[]{sx, sy};
    }

    private static long mixSeed(long base, int charIndex, int lineIndex, long timeMs) {
        long timeBucket = timeMs / 50L; // coarse bucket to keep movement predictable
        long mixed = base;
        mixed ^= (long) charIndex * 0x9E3779B97F4A7C15L;
        mixed ^= (long) lineIndex * 0xC6BC279692B5C323L;
        mixed ^= timeBucket * 0x4F1BBCDCBFA54001L;
        return mixed;
    }

    private static float valueOrDefault(Float value, float fallback) {
        return value != null ? value : fallback;
    }

    public record StyleDelta(float dx, float dy, TextColor colorOverride, SpanAttr.StyleFlags styleOverride) {
        public static final StyleDelta NONE = new StyleDelta(0f, 0f, null, null);
    }
}
