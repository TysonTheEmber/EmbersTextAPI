package net.tysontheember.emberstextapi.immersivemessages.effects;

import com.google.common.collect.ImmutableMap;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.EmptyParams;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.TypedParams;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Central registry for all text rendering effects.
 * <p>
 * This class manages effect registration and creation. Effects are registered
 * by name with a factory function that creates instances from parameters.
 * </p>
 * <p>
 * The registry is thread-safe for reads after initialization but should only
 * be modified (registered) during mod initialization.
 * </p>
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * // Registration (in mod init)
 * EffectRegistry.register("rainbow", RainbowEffect::new);
 * EffectRegistry.register("glitch", GlitchEffect::new);
 *
 * // Creation from markup
 * Effect effect = EffectRegistry.create("rainbow", params);
 *
 * // Parsing from tag content
 * Effect effect = EffectRegistry.parseTag("rainbow f=2.0 w=0.5");
 * }</pre>
 */
public class EffectRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(EffectRegistry.class);

    /**
     * Map of effect names to factory functions.
     * Thread-safe for concurrent reads.
     */
    private static final Map<String, Function<Params, Effect>> EFFECTS = new HashMap<>();

    /**
     * Whether the registry has been initialized with default effects.
     */
    private static boolean initialized = false;

    /**
     * Register an effect factory function.
     * <p>
     * Effect names are case-insensitive and will be normalized to lowercase.
     * Duplicate registrations will overwrite previous registrations with a warning.
     * </p>
     *
     * @param name The effect name (e.g., "rainbow", "glitch")
     * @param factory Factory function that creates effect instances from parameters
     */
    public static synchronized void register(@NotNull String name, @NotNull Function<Params, Effect> factory) {
        String normalizedName = name.toLowerCase();

        if (EFFECTS.containsKey(normalizedName)) {
            LOGGER.warn("Overwriting existing effect registration: {}", normalizedName);
        }

        EFFECTS.put(normalizedName, factory);
        LOGGER.debug("Registered effect: {}", normalizedName);
    }

    /**
     * Create an effect instance from a name and parameters.
     *
     * @param name Effect name
     * @param params Effect parameters
     * @return New effect instance
     * @throws IllegalArgumentException if the effect name is not registered
     */
    @NotNull
    public static Effect create(@NotNull String name, @NotNull Params params) {
        String normalizedName = name.toLowerCase();
        Function<Params, Effect> factory = EFFECTS.get(normalizedName);

        if (factory == null) {
            throw new IllegalArgumentException("Unknown effect: " + name + " (available: " + String.join(", ", EFFECTS.keySet()) + ")");
        }

        try {
            Effect effect = factory.apply(params);
            LOGGER.trace("Created effect: {} with params: {}", name, params);
            return effect;
        } catch (Exception e) {
            LOGGER.error("Failed to create effect: {} with params: {}", name, params, e);
            throw new IllegalArgumentException("Failed to create effect: " + name, e);
        }
    }

    /**
     * Parse and create an effect from a tag content string.
     * <p>
     * Format: {@code effectName param1=value1 param2=value2 param3}
     * </p>
     * <p>
     * Parameter parsing rules:
     * <ul>
     *   <li>{@code param=value} - Key-value pair</li>
     *   <li>{@code param} - Boolean flag (true)</li>
     *   <li>{@code param=true} or {@code param=false} - Explicit boolean</li>
     *   <li>Numeric values are parsed as Double</li>
     *   <li>Everything else is treated as String</li>
     * </ul>
     * </p>
     *
     * @param tagContent Tag content (e.g., "rainbow f=2.0 w=0.5")
     * @return New effect instance
     * @throws IllegalArgumentException if tag content is empty or effect is unknown
     */
    @NotNull
    public static Effect parseTag(@NotNull String tagContent) {
        String[] split = StringUtils.split(tagContent.trim(), ' ');

        if (split.length == 0) {
            throw new IllegalArgumentException("Empty tag content");
        }

        String name = split[0];
        Params params = EmptyParams.INSTANCE;

        if (split.length > 1) {
            ImmutableMap.Builder<String, Object> builder = ImmutableMap.builderWithExpectedSize(split.length - 1);

            for (int i = 1; i < split.length; i++) {
                String[] kv = StringUtils.split(split[i], "=", 2);

                if (kv.length == 1) {
                    // Boolean flag (presence = true)
                    builder.put(kv[0], true);
                    continue;
                }

                // Try to determine type
                String key = kv[0];
                String value = kv[1];

                // Check for explicit boolean
                if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                    builder.put(key, Boolean.parseBoolean(value));
                } else {
                    // Try to parse as number
                    try {
                        double numValue = Double.parseDouble(value);
                        builder.put(key, numValue);
                    } catch (NumberFormatException e) {
                        // Not a number, treat as string
                        builder.put(key, value);
                    }
                }
            }

            params = new TypedParams(builder.build());
        }

        return create(name, params);
    }

    /**
     * Check if an effect is registered.
     *
     * @param name Effect name
     * @return true if registered, false otherwise
     */
    public static boolean isRegistered(@NotNull String name) {
        return EFFECTS.containsKey(name.toLowerCase());
    }

    /**
     * Get all registered effect names.
     *
     * @return Immutable set of effect names
     */
    @NotNull
    public static java.util.Set<String> getRegisteredEffects() {
        return java.util.Collections.unmodifiableSet(EFFECTS.keySet());
    }

    /**
     * Initialize the registry with all built-in effects.
     * <p>
     * This method should be called during mod initialization.
     * It is safe to call multiple times (subsequent calls are ignored).
     * </p>
     */
    public static synchronized void initializeDefaultEffects() {
        if (initialized) {
            LOGGER.debug("Effect registry already initialized");
            return;
        }

        LOGGER.info("Initializing effect registry with default effects");

        // === Color Effects ===
        register("rainbow", net.tysontheember.emberstextapi.immersivemessages.effects.visual.RainbowEffect::new);
        register("rainb", net.tysontheember.emberstextapi.immersivemessages.effects.visual.RainbowEffect::new); // Alias
        register("grad", net.tysontheember.emberstextapi.immersivemessages.effects.visual.GradientEffect::new);
        register("gradient", net.tysontheember.emberstextapi.immersivemessages.effects.visual.GradientEffect::new); // Alias
        register("pulse", net.tysontheember.emberstextapi.immersivemessages.effects.visual.PulseEffect::new);
        register("fade", net.tysontheember.emberstextapi.immersivemessages.effects.visual.FadeEffect::new);

        // === Motion Effects ===
        register("wave", net.tysontheember.emberstextapi.immersivemessages.effects.visual.WaveEffect::new);
        register("bounce", net.tysontheember.emberstextapi.immersivemessages.effects.visual.BounceEffect::new);
        register("swing", net.tysontheember.emberstextapi.immersivemessages.effects.visual.SwingEffect::new);
        register("turb", net.tysontheember.emberstextapi.immersivemessages.effects.visual.TurbulenceEffect::new);
        register("turbulence", net.tysontheember.emberstextapi.immersivemessages.effects.visual.TurbulenceEffect::new); // Alias
        register("shake", net.tysontheember.emberstextapi.immersivemessages.effects.visual.ShakeEffect::new);
        register("circle", net.tysontheember.emberstextapi.immersivemessages.effects.visual.CircleEffect::new);
        register("wiggle", net.tysontheember.emberstextapi.immersivemessages.effects.visual.WiggleEffect::new);
        register("pend", net.tysontheember.emberstextapi.immersivemessages.effects.visual.PendulumEffect::new);
        register("pendulum", net.tysontheember.emberstextapi.immersivemessages.effects.visual.PendulumEffect::new); // Alias
        register("scroll", net.tysontheember.emberstextapi.immersivemessages.effects.visual.ScrollEffect::new);

        // === Special Effects ===
        register("glitch", net.tysontheember.emberstextapi.immersivemessages.effects.visual.GlitchEffect::new);
        register("neon", net.tysontheember.emberstextapi.immersivemessages.effects.visual.NeonEffect::new);
        register("shadow", net.tysontheember.emberstextapi.immersivemessages.effects.visual.ShadowEffect::new);

        // === Animation Effects ===
        register("typewriter", net.tysontheember.emberstextapi.immersivemessages.effects.visual.TypewriterEffect::new);
        register("type", net.tysontheember.emberstextapi.immersivemessages.effects.visual.TypewriterEffect::new); // Alias

        initialized = true;
        LOGGER.info("Registered {} effects", EFFECTS.size());
    }

    /**
     * Clear all registrations (primarily for testing).
     */
    public static synchronized void clear() {
        EFFECTS.clear();
        initialized = false;
        LOGGER.debug("Cleared effect registry");
    }
}
