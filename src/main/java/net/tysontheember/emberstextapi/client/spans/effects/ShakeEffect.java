package net.tysontheember.emberstextapi.client.spans.effects;

import net.tysontheember.emberstextapi.client.spans.SpanAttr;

import java.util.Objects;
import java.util.Random;

/**
 * Reusable shake effect math shared between immersive messages and inline spans.
 */
public final class ShakeEffect {
    private static final float TWO_PI = (float) (Math.PI * 2.0);

    private ShakeEffect() {
    }

    public static float[] compute(SpanAttr.EffectSpec.Shake shake, float elapsedTicks, float indexOffset, long seed) {
        Objects.requireNonNull(shake, "shake");
        float amplitude = valueOrDefault(shake.amplitude(), 0f);
        if (amplitude == 0f || shake.type() == null) {
            return ZERO;
        }

        float speed = valueOrDefault(shake.speed(), 1f);
        float wavelength = valueOrDefault(shake.wavelength(), 1f);
        float phase = elapsedTicks * 0.05f * speed + indexOffset * 0.1f;

        return switch (shake.type()) {
            case WAVE -> new float[]{0f, (float) Math.sin(phase * TWO_PI / Math.max(0.0001f, wavelength)) * amplitude};
            case CIRCLE -> new float[]{
                (float) Math.cos(phase) * amplitude,
                (float) Math.sin(phase) * amplitude
            };
            case RANDOM -> sampleRandomShake(seed, amplitude);
        };
    }

    public static long mixSeed(long base, int charIndex, int lineIndex, long elapsedMs) {
        long timeBucket = elapsedMs / 50L;
        long mixed = base;
        mixed ^= (long) charIndex * 0x9E3779B97F4A7C15L;
        mixed ^= (long) lineIndex * 0xC6BC279692B5C323L;
        mixed ^= timeBucket * 0x4F1BBCDCBFA54001L;
        return mixed;
    }

    private static float[] sampleRandomShake(long seed, float amplitude) {
        Random random = new Random(seed);
        float sx = (random.nextFloat() - 0.5f) * 2f * amplitude;
        float sy = (random.nextFloat() - 0.5f) * 2f * amplitude;
        return new float[]{sx, sy};
    }

    private static float valueOrDefault(Float value, float fallback) {
        return value != null ? value : fallback;
    }

    private static final float[] ZERO = new float[]{0f, 0f};
}
