package net.tysontheember.emberstextapi.immersivemessages.effects.message;

import com.google.common.collect.ImmutableMap;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.EmptyParams;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.TypedParams;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MessageEffectRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageEffectRegistry.class);

    private static final Map<String, Function<Params, MessageEffect>> EFFECTS = new ConcurrentHashMap<>();
    private static final Set<String> BUILT_IN_EFFECTS = ConcurrentHashMap.newKeySet();

    private static volatile boolean initialized = false;
    private static volatile boolean locked = false;

    public static synchronized void register(@NotNull String name, @NotNull Function<Params, MessageEffect> factory) {
        String normalized = name.toLowerCase();
        if (EFFECTS.containsKey(normalized)) {
            if (locked && BUILT_IN_EFFECTS.contains(normalized)) {
                LOGGER.error("Cannot overwrite built-in message effect '{}' after registry is locked", normalized);
                throw new IllegalStateException("Cannot overwrite built-in message effect: " + normalized);
            }
            LOGGER.warn("Overwriting existing message effect registration: {}", normalized);
        }
        EFFECTS.put(normalized, factory);
    }

    private static void registerBuiltIn(@NotNull String name, @NotNull Function<Params, MessageEffect> factory) {
        String normalized = name.toLowerCase();
        EFFECTS.put(normalized, factory);
        BUILT_IN_EFFECTS.add(normalized);
    }

    @NotNull
    public static MessageEffect create(@NotNull String name, @NotNull Params params) {
        String normalized = name.toLowerCase();
        Function<Params, MessageEffect> factory = EFFECTS.get(normalized);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown message effect: " + name
                    + " (available: " + String.join(", ", EFFECTS.keySet()) + ")");
        }
        try {
            return factory.apply(params);
        } catch (Exception e) {
            LOGGER.error("Failed to create message effect: {} with params: {}", name, params, e);
            throw new IllegalArgumentException("Failed to create message effect: " + name, e);
        }
    }

    @NotNull
    public static MessageEffect parseTag(@NotNull String tagContent) {
        String[] split = StringUtils.split(tagContent.trim(), ' ');
        if (split.length == 0) {
            throw new IllegalArgumentException("Empty message-effect tag content");
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
                        builder.put(key, Double.parseDouble(value));
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
    public static Set<String> getRegisteredEffects() {
        return java.util.Collections.unmodifiableSet(EFFECTS.keySet());
    }

    public static synchronized void initializeDefaultEffects() {
        if (initialized) {
            return;
        }

        registerBuiltIn("rock", RockMessageEffect::new);
        registerBuiltIn("breathe", BreatheMessageEffect::new);

        initialized = true;
        locked = true;
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
    }
}
