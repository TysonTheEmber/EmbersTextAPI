package net.tysontheember.emberstextapi.immersivemessages.effects;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Context manager for applying multiple effects to character rendering.
 * <p>
 * This class handles the application of effect stacks, managing the order
 * of effect execution and accumulation of sibling layers for multi-layer effects.
 * </p>
 *
 * <h3>Effect Stacking Order:</h3>
 * <p>
 * Effects are applied in the order they appear in the list. Each effect
 * can modify the results of previous effects, allowing for complex compositions.
 * </p>
 * <p>
 * Example: {@code [rainbow, wave]} will first apply rainbow coloring, then wave motion.
 * </p>
 *
 * <h3>Sibling Handling:</h3>
 * <p>
 * Some effects (like glitch) create additional rendering layers called "siblings".
 * The context accumulates all siblings from all effects in the stack for batch rendering.
 * </p>
 */
public class EffectContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(EffectContext.class);

    /**
     * Apply a stack of effects to the given settings.
     * <p>
     * Effects are applied in order. Any siblings created by effects are left
     * in the settings.siblings list for the caller to render.
     * </p>
     * <p>
     * If any effect throws an exception, it is logged but not propagated.
     * This ensures that one broken effect doesn't break all rendering.
     * </p>
     *
     * @param effects List of effects to apply
     * @param settings Mutable settings to modify
     */
    public static void applyEffects(@NotNull List<Effect> effects, @NotNull EffectSettings settings) {
        if (effects.isEmpty()) {
            return;
        }

        for (Effect effect : effects) {
            try {
                effect.apply(settings);
            } catch (Exception e) {
                LOGGER.error("Effect '{}' threw exception during application", effect.getName(), e);
                // Continue with other effects even if one fails
            }
        }
    }

    /**
     * Apply effects and recursively apply them to all generated siblings.
     * <p>
     * This version applies effects to the main settings, then recursively
     * applies them to any siblings that were generated. This ensures that
     * effects like rainbow affect both the main character and glitch slices.
     * </p>
     * <p>
     * Use this when you want effects to apply to sibling layers as well.
     * </p>
     *
     * @param effects List of effects to apply
     * @param settings Mutable settings to modify
     */
    public static void applyEffectsRecursive(@NotNull List<Effect> effects, @NotNull EffectSettings settings) {
        if (effects.isEmpty()) {
            return;
        }

        // Apply to main settings
        applyEffects(effects, settings);

        // Apply to all siblings (they may generate more siblings)
        // Use getSiblingsOrEmpty() to avoid creating list if no siblings exist
        List<EffectSettings> siblings = settings.getSiblingsOrEmpty();
        int siblingCount = siblings.size();
        for (int i = 0; i < siblingCount; i++) {
            EffectSettings sibling = siblings.get(i);
            applyEffects(effects, sibling);
        }
    }

    /**
     * Apply effects to a single character with the given context.
     * <p>
     * This is a convenience method that creates a new EffectSettings,
     * initializes it with the given values, applies effects, and returns it.
     * </p>
     *
     * @param effects List of effects to apply
     * @param x Initial X position
     * @param y Initial Y position
     * @param r Initial red channel
     * @param g Initial green channel
     * @param b Initial blue channel
     * @param a Initial alpha channel
     * @param index Character index
     * @param codepoint Character codepoint
     * @param isShadow Whether this is a shadow layer
     * @return New EffectSettings with effects applied
     */
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
