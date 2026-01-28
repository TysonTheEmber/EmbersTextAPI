package net.tysontheember.emberstextapi.immersivemessages.effects;

import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

/**
 * Core interface for all text rendering effects in the new effect system.
 * <p>
 * Effects are composable transformations that modify character rendering parameters
 * such as position, rotation, color, and alpha. Multiple effects can be stacked
 * and will be applied in order to create complex visual results.
 * </p>
 * <p>
 * Effects operate on a per-character basis through the {@link EffectSettings} object,
 * which contains mutable state that effects modify in-place.
 * </p>
 *
 * @see EffectSettings
 * @see BaseEffect
 * @see EffectRegistry
 */
public interface Effect {

    /**
     * Apply this effect's transformation to the given character rendering settings.
     * <p>
     * Effects modify the EffectSettings object in-place. Common modifications include:
     * <ul>
     *   <li>Position offsets (x, y)</li>
     *   <li>Rotation (rot)</li>
     *   <li>Color channels (r, g, b)</li>
     *   <li>Alpha transparency (a)</li>
     *   <li>Adding sibling layers for multi-layer effects (siblings list)</li>
     * </ul>
     * </p>
     *
     * @param settings The mutable effect settings to modify
     */
    void apply(@NotNull EffectSettings settings);

    /**
     * Get the unique name of this effect.
     * <p>
     * This name is used for:
     * <ul>
     *   <li>Effect registration in {@link EffectRegistry}</li>
     *   <li>Markup tag parsing (e.g., "rainbow", "glitch")</li>
     *   <li>Effect serialization/deserialization</li>
     * </ul>
     * </p>
     *
     * @return The effect name (e.g., "rainbow", "glitch", "bounce")
     */
    @NotNull
    String getName();

    /**
     * Serialize this effect to a string representation for network transmission or storage.
     * <p>
     * The default implementation returns just the effect name. Effects with parameters
     * should override this to include parameter values in the format:
     * {@code effectName param1=value1 param2=value2}
     * </p>
     *
     * @return Serialized effect string
     */
    @NotNull
    default String serialize() {
        return getName();
    }

    /**
     * Create an effect instance from parsed markup parameters.
     * <p>
     * This factory method is used by the markup parser to create effects from
     * tag content like: {@code <glitch f=2.0 j=0.02>}
     * </p>
     *
     * @param name The effect name to create
     * @param params The parsed parameters
     * @return A new effect instance
     * @throws IllegalArgumentException if the effect name is not registered
     */
    @NotNull
    static Effect create(@NotNull String name, @NotNull Params params) {
        return EffectRegistry.create(name, params);
    }
}
