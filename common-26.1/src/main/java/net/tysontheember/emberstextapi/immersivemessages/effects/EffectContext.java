package net.tysontheember.emberstextapi.immersivemessages.effects;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EffectContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(EffectContext.class);

    public static void applyEffects(@NotNull List<Effect> effects, @NotNull EffectSettings settings) {
        if (effects.isEmpty()) {
            return;
        }

        for (Effect effect : effects) {
            try {
                effect.apply(settings);
            } catch (Exception e) {
                LOGGER.error("Effect '{}' threw exception during application", effect.getName(), e);

            }
        }
    }

    public static void applyEffectsRecursive(@NotNull List<Effect> effects, @NotNull EffectSettings settings) {
        if (effects.isEmpty()) {
            return;
        }

        applyEffects(effects, settings);

        List<EffectSettings> siblings = settings.getSiblingsOrEmpty();
        int siblingCount = siblings.size();
        for (int i = 0; i < siblingCount; i++) {
            EffectSettings sibling = siblings.get(i);
            applyEffects(effects, sibling);
        }
    }

    @NotNull
    public static EffectSettings createAndApply(@NotNull List<Effect> effects,
                                                 float x, float y,
                                                 float r, float g, float b, float a,
                                                 int index, int codepoint, boolean isShadow) {
        EffectSettings settings = new EffectSettings(x, y, r, g, b, a, index, codepoint, isShadow);
        applyEffects(effects, settings);
        return settings;
    }
}
