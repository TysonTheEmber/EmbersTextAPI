package net.tysontheember.emberstextapi.immersivemessages.effects;

import com.google.common.collect.ImmutableMap;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.EmptyParams;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.TypedParams;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.tysontheember.emberstextapi.platform.ConfigHelper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class EffectRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(EffectRegistry.class);

    private static final Map<String, Function<Params, Effect>> EFFECTS = new ConcurrentHashMap<>();

    private static final java.util.Set<String> BUILT_IN_EFFECTS = ConcurrentHashMap.newKeySet();

    private static volatile boolean initialized = false;

    private static volatile boolean locked = false;

    public static synchronized void register(@NotNull String name, @NotNull Function<Params, Effect> factory) {
        String normalizedName = name.toLowerCase();

        if (EFFECTS.containsKey(normalizedName)) {

            if (locked && BUILT_IN_EFFECTS.contains(normalizedName)) {
                LOGGER.error("Cannot overwrite built-in effect '{}' after registry is locked", normalizedName);
                throw new IllegalStateException("Cannot overwrite built-in effect: " + normalizedName);
            }
            LOGGER.warn("Overwriting existing effect registration: {}", normalizedName);
        }

        EFFECTS.put(normalizedName, factory);
        LOGGER.debug("Registered effect: {}", normalizedName);
    }

    private static void registerBuiltIn(@NotNull String name, @NotNull Function<Params, Effect> factory) {
        String normalizedName = name.toLowerCase();
        EFFECTS.put(normalizedName, factory);
        BUILT_IN_EFFECTS.add(normalizedName);
        LOGGER.debug("Registered built-in effect: {}", normalizedName);
    }

    @NotNull
    public static Effect create(@NotNull String name, @NotNull Params params) {
        String normalizedName = name.toLowerCase();

        try {
            if (ConfigHelper.getInstance().isEffectDisabled(normalizedName)) {
                LOGGER.debug("Effect '{}' is disabled via config, returning no-op", normalizedName);
                return new NoOpEffect(normalizedName);
            }
        } catch (Exception e) {

            LOGGER.trace("Could not check disabled effects for '{}': {}", normalizedName, e.getMessage());
        }

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

                    builder.put(kv[0], true);
                    continue;
                }

                String key = kv[0];
                String value = kv[1];

                if (value.length() >= 2) {
                    char first = value.charAt(0);
                    char last = value.charAt(value.length() - 1);
                    if ((first == '"' || first == '\'') && first == last) {
                        value = value.substring(1, value.length() - 1);
                    }
                }

                if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                    builder.put(key, Boolean.parseBoolean(value));
                } else {

                    try {
                        double numValue = Double.parseDouble(value);
                        builder.put(key, numValue);
                    } catch (NumberFormatException e) {

                        builder.put(key, value);
                    }
                }
            }

            params = new TypedParams(builder.build());
        }

        return create(name, params);
    }

    public static boolean isRegistered(@NotNull String name) {
        return EFFECTS.containsKey(name.toLowerCase());
    }

    @NotNull
    public static java.util.Set<String> getRegisteredEffects() {
        return java.util.Collections.unmodifiableSet(EFFECTS.keySet());
    }

    public static synchronized void initializeDefaultEffects() {
        if (initialized) {
            LOGGER.debug("Effect registry already initialized");
            return;
        }

        LOGGER.info("Initializing effect registry with default effects");

        registerBuiltIn("rainbow", net.tysontheember.emberstextapi.immersivemessages.effects.visual.RainbowEffect::new);
        registerBuiltIn("rainb", net.tysontheember.emberstextapi.immersivemessages.effects.visual.RainbowEffect::new);
        registerBuiltIn("grad", net.tysontheember.emberstextapi.immersivemessages.effects.visual.GradientEffect::new);
        registerBuiltIn("gradient", net.tysontheember.emberstextapi.immersivemessages.effects.visual.GradientEffect::new);
        registerBuiltIn("color", net.tysontheember.emberstextapi.immersivemessages.effects.visual.ColorEffect::new);
        registerBuiltIn("col", net.tysontheember.emberstextapi.immersivemessages.effects.visual.ColorEffect::new);
        registerBuiltIn("pulse", net.tysontheember.emberstextapi.immersivemessages.effects.visual.PulseEffect::new);
        registerBuiltIn("fade", net.tysontheember.emberstextapi.immersivemessages.effects.visual.FadeEffect::new);

        registerBuiltIn("wave", net.tysontheember.emberstextapi.immersivemessages.effects.visual.WaveEffect::new);
        registerBuiltIn("bounce", net.tysontheember.emberstextapi.immersivemessages.effects.visual.BounceEffect::new);
        registerBuiltIn("swing", net.tysontheember.emberstextapi.immersivemessages.effects.visual.SwingEffect::new);
        registerBuiltIn("turb", net.tysontheember.emberstextapi.immersivemessages.effects.visual.TurbulenceEffect::new);
        registerBuiltIn("turbulence", net.tysontheember.emberstextapi.immersivemessages.effects.visual.TurbulenceEffect::new);
        registerBuiltIn("shake", net.tysontheember.emberstextapi.immersivemessages.effects.visual.ShakeEffect::new);
        registerBuiltIn("circle", net.tysontheember.emberstextapi.immersivemessages.effects.visual.CircleEffect::new);
        registerBuiltIn("wiggle", net.tysontheember.emberstextapi.immersivemessages.effects.visual.WiggleEffect::new);
        registerBuiltIn("pend", net.tysontheember.emberstextapi.immersivemessages.effects.visual.PendulumEffect::new);
        registerBuiltIn("pendulum", net.tysontheember.emberstextapi.immersivemessages.effects.visual.PendulumEffect::new);
        registerBuiltIn("glitch", net.tysontheember.emberstextapi.immersivemessages.effects.visual.GlitchEffect::new);
        registerBuiltIn("neon", net.tysontheember.emberstextapi.immersivemessages.effects.visual.NeonEffect::new);
        registerBuiltIn("glow", net.tysontheember.emberstextapi.immersivemessages.effects.visual.NeonEffect::new);
        registerBuiltIn("shadow", net.tysontheember.emberstextapi.immersivemessages.effects.visual.ShadowEffect::new);
        registerBuiltIn("scroll", net.tysontheember.emberstextapi.immersivemessages.effects.visual.ScrollEffect::new);

        registerBuiltIn("typewriter", net.tysontheember.emberstextapi.immersivemessages.effects.animation.TypewriterEffect::new);
        registerBuiltIn("type", net.tysontheember.emberstextapi.immersivemessages.effects.animation.TypewriterEffect::new);

        registerBuiltIn("obfuscate", net.tysontheember.emberstextapi.immersivemessages.effects.animation.ObfuscateEffect::new);
        registerBuiltIn("obf", net.tysontheember.emberstextapi.immersivemessages.effects.animation.ObfuscateEffect::new);

        initialized = true;
        locked = true;
        LOGGER.info("Registered {} effects (registry locked)", EFFECTS.size());
    }

    public static boolean isLocked() {
        return locked;
    }

    public static boolean isBuiltIn(@NotNull String name) {
        return BUILT_IN_EFFECTS.contains(name.toLowerCase());
    }

    public static synchronized void clear() {
        EFFECTS.clear();
        BUILT_IN_EFFECTS.clear();
        initialized = false;
        locked = false;
        LOGGER.debug("Cleared effect registry");
    }
}
