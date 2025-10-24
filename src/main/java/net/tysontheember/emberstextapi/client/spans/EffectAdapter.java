package net.tysontheember.emberstextapi.client.spans;

import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.client.spans.effects.ObfuscateEffect;
import net.tysontheember.emberstextapi.client.spans.effects.ShakeEffect;
import net.tysontheember.emberstextapi.client.spans.effects.TypewriterEffect;

/**
 * Converts immutable span attributes into runtime deltas that can be consumed by render hooks.
 */
public final class EffectAdapter {
    private static final StyleDelta NO_DELTA = new StyleDelta(true, 0f, 0f, null, null, null, null);
    private static final StyleDelta INVISIBLE = new StyleDelta(false, 0f, 0f, null, null, null, null);

    private EffectAdapter() {
    }

    public static StyleDelta apply(SpanifiedSequence.SpanEntry entry, SpanifiedSequence.EvalContext context) {
        if (entry == null || entry.attr == null) {
            return NO_DELTA;
        }

        SpanAttr attr = entry.attr;
        SpanAttr.EffectSpec spec = attr.effect();
        if (spec == null) {
            return NO_DELTA;
        }

        float elapsedTicks = context.elapsedTicks();
        boolean visible = true;
        Boolean obfuscatedOverride = null;

        if (entry.state != null) {
            if (entry.state.typewriter != null) {
                visible = TypewriterEffect.isVisible(entry.state.typewriter, elapsedTicks, entry.offset);
            }
            if (visible && entry.state.obfuscate != null) {
                boolean revealed = ObfuscateEffect.isRevealed(entry.state.obfuscate, elapsedTicks, entry.offset);
                if (!revealed) {
                    obfuscatedOverride = Boolean.TRUE;
                }
            }
        }

        if (!visible) {
            return INVISIBLE;
        }

        float dx = 0f;
        float dy = 0f;
        TextColor colour = null;
        SpanAttr.StyleFlags styleOverride = null;

        if (spec.shake() != null) {
            float[] shake = ShakeEffect.compute(spec.shake(), elapsedTicks, 0f, context.seed());
            dx += shake[0];
            dy += shake[1];
        }

        if (spec.charShake() != null) {
            float indexOffset = entry.offset + entry.lineIndex * 32f;
            long mixedSeed = ShakeEffect.mixSeed(context.seed(), entry.globalIndex, entry.lineIndex, context.elapsedMs());
            float[] shake = ShakeEffect.compute(spec.charShake(), elapsedTicks, indexOffset, mixedSeed);
            dx += shake[0];
            dy += shake[1];
        }

        return new StyleDelta(true, dx, dy, colour, styleOverride, obfuscatedOverride, null);
    }

    public record StyleDelta(boolean visible,
                             float dx,
                             float dy,
                             TextColor colorOverride,
                             SpanAttr.StyleFlags styleOverride,
                             Boolean obfuscatedOverride,
                             Float alphaMultiplier) {
        public static final StyleDelta NONE = new StyleDelta(true, 0f, 0f, null, null, null, null);
    }
}
