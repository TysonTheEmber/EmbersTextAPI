package net.tysontheember.emberstextapi.immersivemessages.effects.message.attr;

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

public class MessageAttributeRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageAttributeRegistry.class);

    private static final Map<String, Function<Params, MessageAttribute>> ATTRIBUTES = new ConcurrentHashMap<>();
    private static final Set<String> BUILT_IN_ATTRIBUTES = ConcurrentHashMap.newKeySet();

    private static volatile boolean initialized = false;
    private static volatile boolean locked = false;

    public static synchronized void register(@NotNull String name, @NotNull Function<Params, MessageAttribute> factory) {
        String normalized = name.toLowerCase();
        if (ATTRIBUTES.containsKey(normalized)) {
            if (locked && BUILT_IN_ATTRIBUTES.contains(normalized)) {
                LOGGER.error("Cannot overwrite built-in message attribute '{}' after registry is locked", normalized);
                throw new IllegalStateException("Cannot overwrite built-in message attribute: " + normalized);
            }
            LOGGER.warn("Overwriting existing message attribute registration: {}", normalized);
        }
        ATTRIBUTES.put(normalized, factory);
    }

    private static void registerBuiltIn(@NotNull String name, @NotNull Function<Params, MessageAttribute> factory) {
        String normalized = name.toLowerCase();
        ATTRIBUTES.put(normalized, factory);
        BUILT_IN_ATTRIBUTES.add(normalized);
    }

    @NotNull
    public static MessageAttribute create(@NotNull String name, @NotNull Params params) {
        String normalized = name.toLowerCase();
        Function<Params, MessageAttribute> factory = ATTRIBUTES.get(normalized);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown message attribute: " + name
                    + " (available: " + String.join(", ", ATTRIBUTES.keySet()) + ")");
        }
        try {
            return factory.apply(params);
        } catch (Exception e) {
            LOGGER.error("Failed to create message attribute: {} with params: {}", name, params, e);
            throw new IllegalArgumentException("Failed to create message attribute: " + name, e);
        }
    }

    @NotNull
    public static MessageAttribute parseTag(@NotNull String tagContent) {
        String[] split = StringUtils.split(tagContent.trim(), ' ');
        if (split.length == 0) {
            throw new IllegalArgumentException("Empty message-attribute tag content");
        }

        String name = split[0];
        Params params = EmptyParams.INSTANCE;

        if (split.length > 1) {
            ImmutableMap.Builder<String, Object> builder = ImmutableMap.builderWithExpectedSize(split.length - 1);
            for (int i = 1; i < split.length; i++) {
                String[] kv = StringUtils.split(split[i], "=", 2);
                if (kv.length == 1) {
                    if (i == 1) {
                        builder.put("value", coerce(kv[0]));
                    } else {
                        builder.put(kv[0], true);
                    }
                    continue;
                }
                String key = kv[0];
                String value = kv[1];
                if (value.length() >= 2) {
                    char fc = value.charAt(0), lc = value.charAt(value.length() - 1);
                    if ((fc == '"' || fc == '\'') && fc == lc) {
                        value = value.substring(1, value.length() - 1);
                    }
                }
                builder.put(key, coerce(value));
            }
            params = new TypedParams(builder.build());
        }

        return create(name, params);
    }

    private static Object coerce(String value) {
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.parseBoolean(value);
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return value;
        }
    }

    public static boolean isRegistered(@NotNull String name) {
        return ATTRIBUTES.containsKey(name.toLowerCase());
    }

    @NotNull
    public static Set<String> getRegisteredAttributes() {
        return java.util.Collections.unmodifiableSet(ATTRIBUTES.keySet());
    }

    public static synchronized void initializeDefaultAttributes() {
        if (initialized) {
            return;
        }

        registerBuiltIn("bg",         BackgroundAttribute::new);
        registerBuiltIn("background", BackgroundAttribute::new);
        registerBuiltIn("scale",      ScaleAttribute::new);
        registerBuiltIn("offset",     OffsetAttribute::new);
        registerBuiltIn("anchor",     AnchorAttribute::new);
        registerBuiltIn("align",      AlignAttribute::new);
        registerBuiltIn("shadow",     ShadowAttribute::new);
        registerBuiltIn("fade",       FadeAttribute::new);

        initialized = true;
        locked = true;
    }

    public static boolean isLocked() {
        return locked;
    }

    public static boolean isBuiltIn(@NotNull String name) {
        return BUILT_IN_ATTRIBUTES.contains(name.toLowerCase());
    }

    public static synchronized void clear() {
        ATTRIBUTES.clear();
        BUILT_IN_ATTRIBUTES.clear();
        initialized = false;
        locked = false;
    }
}
