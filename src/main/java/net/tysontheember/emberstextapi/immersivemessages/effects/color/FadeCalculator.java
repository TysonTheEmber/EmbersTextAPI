package net.tysontheember.emberstextapi.immersivemessages.effects.color;

import net.minecraft.util.Mth;

/**
 * Utility class for calculating fade (alpha transparency) effects.
 * Supports both global message fade and per-span fade calculations.
 */
public class FadeCalculator {

    /**
     * Computes the alpha value for a fade effect given timing parameters.
     *
     * @param sampleAge Current age of the message (in ticks, interpolated with partialTick)
     * @param fadeInTicks Number of ticks to fade in from 0 to full opacity
     * @param duration Visible duration at full opacity
     * @param fadeOutTicks Number of ticks to fade out from full opacity to 0
     * @return Alpha value in range [0, 1] representing opacity
     */
    public static float computeFadeAlpha(float sampleAge, float fadeInTicks, float duration, float fadeOutTicks) {
        float visibleEnd = fadeInTicks + duration;
        float total = visibleEnd + fadeOutTicks;

        float alpha;
        if (fadeInTicks > 0f && sampleAge <= fadeInTicks) {
            // Fade in phase - ensure we start at exactly 0 alpha when sampleAge is 0
            alpha = Math.max(0f, sampleAge) / Math.max(1f, fadeInTicks);
        } else if (sampleAge < visibleEnd || (duration <= 0f && fadeInTicks == 0f && fadeOutTicks == 0f)) {
            // Fully visible phase
            alpha = 1f;
        } else if (fadeOutTicks > 0f && sampleAge < total) {
            // Fade out phase
            float fadeProgress = sampleAge - visibleEnd;
            alpha = 1f - (fadeProgress / Math.max(1f, fadeOutTicks));
        } else {
            // Fully faded out
            alpha = 0f;
        }

        return Mth.clamp(alpha, 0f, 1f);
    }

    /**
     * Computes alpha for a span with per-span fade effects.
     * If the span has no fade settings, returns the global alpha.
     * Otherwise, computes span-specific alpha and multiplies by global alpha.
     *
     * @param sampleAge Current age of the message
     * @param spanFadeInTicks Span-specific fade in ticks (null to use global)
     * @param spanFadeOutTicks Span-specific fade out ticks (null to use global)
     * @param duration Message duration
     * @param globalFadeInTicks Global message fade in ticks
     * @param globalFadeOutTicks Global message fade out ticks
     * @return Combined alpha value [0, 1]
     */
    public static float computeSpanFadeAlpha(float sampleAge,
                                              Integer spanFadeInTicks, Integer spanFadeOutTicks,
                                              float duration,
                                              float globalFadeInTicks, float globalFadeOutTicks) {

        // If span has no fade effects, use global alpha only
        if (spanFadeInTicks == null && spanFadeOutTicks == null) {
            return computeFadeAlpha(sampleAge, globalFadeInTicks, duration, globalFadeOutTicks);
        }

        // Use span-specific fade timing
        float fadeIn = spanFadeInTicks != null ? spanFadeInTicks : 0f;
        float fadeOut = spanFadeOutTicks != null ? spanFadeOutTicks : 0f;
        float spanAlpha = computeFadeAlpha(sampleAge, fadeIn, duration, fadeOut);

        // Combine with global alpha (for overall message fade)
        float globalAlpha = computeFadeAlpha(sampleAge, globalFadeInTicks, duration, globalFadeOutTicks);
        return Mth.clamp(spanAlpha * globalAlpha, 0f, 1f);
    }
}
