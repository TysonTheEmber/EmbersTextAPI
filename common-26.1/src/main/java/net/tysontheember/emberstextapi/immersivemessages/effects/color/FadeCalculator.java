package net.tysontheember.emberstextapi.immersivemessages.effects.color;

import net.minecraft.util.Mth;

public class FadeCalculator {

    public static float computeFadeAlpha(float sampleAge, float fadeInTicks, float duration, float fadeOutTicks) {
        float visibleEnd = fadeInTicks + duration;
        float total = visibleEnd + fadeOutTicks;

        float alpha;
        if (fadeInTicks > 0f && sampleAge <= fadeInTicks) {

            alpha = Math.max(0f, sampleAge) / Math.max(1f, fadeInTicks);
        } else if (sampleAge < visibleEnd || (duration <= 0f && fadeInTicks == 0f && fadeOutTicks == 0f)) {

            alpha = 1f;
        } else if (fadeOutTicks > 0f && sampleAge < total) {

            float fadeProgress = sampleAge - visibleEnd;
            alpha = 1f - (fadeProgress / Math.max(1f, fadeOutTicks));
        } else {

            alpha = 0f;
        }

        return Mth.clamp(alpha, 0f, 1f);
    }

    public static float computeSpanFadeAlpha(float sampleAge,
                                              Integer spanFadeInTicks, Integer spanFadeOutTicks,
                                              float duration,
                                              float globalFadeInTicks, float globalFadeOutTicks) {

        if (spanFadeInTicks == null && spanFadeOutTicks == null) {
            return computeFadeAlpha(sampleAge, globalFadeInTicks, duration, globalFadeOutTicks);
        }

        float fadeIn = spanFadeInTicks != null ? spanFadeInTicks : 0f;
        float fadeOut = spanFadeOutTicks != null ? spanFadeOutTicks : 0f;
        float spanAlpha = computeFadeAlpha(sampleAge, fadeIn, duration, fadeOut);

        float globalAlpha = computeFadeAlpha(sampleAge, globalFadeInTicks, duration, globalFadeOutTicks);
        return Mth.clamp(spanAlpha * globalAlpha, 0f, 1f);
    }
}
