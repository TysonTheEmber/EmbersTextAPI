package net.tysontheember.emberstextapi.client.spans.effects;

import net.tysontheember.emberstextapi.client.spans.SpanAttr;

/**
 * Shared typewriter effect logic reused by immersive overlays and spanified sequences.
 */
public final class TypewriterEffect {
    private TypewriterEffect() {
    }

    public static State create(SpanAttr.EffectSpec.Typewriter spec, int length) {
        if (spec == null || length <= 0) {
            return null;
        }
        float speed = spec.speed() != null ? spec.speed() : 0f;
        return new State(Math.max(0f, speed), spec.center(), Math.max(0, length));
    }

    public static boolean isVisible(State state, float elapsedTicks, int offset) {
        if (state == null) {
            return true;
        }
        int visibleChars = state.visibleCharacters(elapsedTicks);
        return offset < visibleChars;
    }

    public record State(float speed, boolean center, int length) {
        public int visibleCharacters(float elapsedTicks) {
            int reveal = (int) Math.floor(Math.max(0f, elapsedTicks) * speed);
            return Math.min(length, Math.max(0, reveal));
        }
    }
}
