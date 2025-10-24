package net.tysontheember.emberstextapi.client.text;

import java.util.List;

import net.tysontheember.emberstextapi.duck.ETAStyle;

/**
 * Placeholder registry that will run span effects in a later phase.
 */
public final class SpanEffectRegistry {
    private SpanEffectRegistry() {
    }

    public static void applyEffects(EffectContext context, EffectSettings settings, List<SpanEffect> effects, ETAStyle etaStyle) {
        // Phase D1 stub: intentionally left blank. Later phases will mutate settings or render siblings.
    }
}
