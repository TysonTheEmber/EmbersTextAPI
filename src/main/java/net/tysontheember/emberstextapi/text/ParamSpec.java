package net.tysontheember.emberstextapi.text;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.*;

/**
 * Describes the accepted parameters for an attribute.
 */
public final class ParamSpec {
    private static final Logger LOGGER = LogUtils.getLogger();

    public enum ParamType {
        STRING,
        BOOLEAN,
        INTEGER,
        FLOAT,
        COLOR,
        ENUM
    }

    private final Map<String, Param> params;

    private ParamSpec(Map<String, Param> params) {
        this.params = Map.copyOf(params);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Params bind(ResourceLocation id, Map<String, Object> raw) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, Param> entry : params.entrySet()) {
            String key = entry.getKey();
            Param param = entry.getValue();
            Object value;
            if (raw != null && raw.containsKey(key)) {
                value = convert(id, key, param, raw.get(key));
            } else if (param.required) {
                LOGGER.debug("[{}] missing required parameter '{}', using default {}", id, key, param.defaultValue);
                value = param.defaultValue;
            } else {
                value = param.defaultValue;
            }
            values.put(key, value);
        }
        if (raw != null) {
            for (String key : raw.keySet()) {
                if (!params.containsKey(key)) {
                    LOGGER.debug("[{}] ignoring unknown parameter '{}'", id, key);
                }
            }
        }
        return new Params(values);
    }

    private Object convert(ResourceLocation id, String key, Param param, Object value) {
        try {
            return switch (param.type) {
                case STRING -> String.valueOf(value);
                case BOOLEAN -> toBoolean(value, param.defaultValue);
                case INTEGER -> toInt(value, param.defaultValue);
                case FLOAT -> toFloat(value, param.defaultValue);
                case COLOR -> toColor(value, param.defaultValue);
                case ENUM -> toEnum(id, key, param, value);
            };
        } catch (Exception ex) {
            LOGGER.debug("[{}] failed to parse parameter '{}' as {}: {}", id, key, param.type, ex.toString());
            return param.defaultValue;
        }
    }

    private Object toEnum(ResourceLocation id, String key, Param param, Object value) {
        String text = String.valueOf(value);
        if (param.options.contains(text.toLowerCase(Locale.ROOT))) {
            return text.toLowerCase(Locale.ROOT);
        }
        LOGGER.debug("[{}] invalid enum value '{}' for '{}', expected {}", id, text, key, param.options);
        return param.defaultValue;
    }

    private Object toBoolean(Object value, Object def) {
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.intValue() != 0;
        }
        String text = String.valueOf(value);
        return Boolean.parseBoolean(text);
    }

    private Object toInt(Object value, Object def) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = String.valueOf(value);
        if (text.startsWith("0x")) {
            return Integer.parseUnsignedInt(text.substring(2), 16);
        }
        return Integer.parseInt(text);
    }

    private Object toFloat(Object value, Object def) {
        if (value instanceof Number number) {
            return number.floatValue();
        }
        String text = String.valueOf(value);
        return Float.parseFloat(text);
    }

    private Object toColor(Object value, Object def) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = String.valueOf(value).trim();
        if (text.startsWith("#")) {
            text = text.substring(1);
        }
        if (text.startsWith("0x")) {
            text = text.substring(2);
        }
        long parsed = Long.parseUnsignedLong(text, 16);
        if (text.length() <= 6) {
            parsed |= 0xFF000000L;
        }
        return (int) parsed;
    }

    public static final class Builder {
        private final Map<String, Param> params = new LinkedHashMap<>();

        public Builder required(String name, ParamType type) {
            params.put(name, new Param(type, null, true, List.of()));
            return this;
        }

        public Builder optional(String name, ParamType type, Object defaultValue) {
            params.put(name, new Param(type, defaultValue, false, List.of()));
            return this;
        }

        public Builder optionalEnum(String name, String defaultValue, String... allowed) {
            params.put(name, new Param(ParamType.ENUM, defaultValue.toLowerCase(Locale.ROOT), false,
                Arrays.stream(allowed).map(s -> s.toLowerCase(Locale.ROOT)).toList()));
            return this;
        }

        public ParamSpec build() {
            return new ParamSpec(params);
        }
    }

    private record Param(ParamType type, Object defaultValue, boolean required, List<String> options) {
    }
}
