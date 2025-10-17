package net.tysontheember.emberstextapi.text;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Validated attribute parameters.
 */
public final class Params {
    private final Map<String, Object> values;

    Params(Map<String, Object> values) {
        this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
    }

    public boolean has(String key) {
        return values.containsKey(key);
    }

    public Object raw(String key) {
        return values.get(key);
    }

    public int getInt(String key, int fallback) {
        Object v = values.get(key);
        if (v instanceof Number number) {
            return number.intValue();
        }
        return fallback;
    }

    public float getFloat(String key, float fallback) {
        Object v = values.get(key);
        if (v instanceof Number number) {
            return number.floatValue();
        }
        return fallback;
    }

    public double getDouble(String key, double fallback) {
        Object v = values.get(key);
        if (v instanceof Number number) {
            return number.doubleValue();
        }
        return fallback;
    }

    public boolean getBoolean(String key, boolean fallback) {
        Object v = values.get(key);
        if (v instanceof Boolean bool) {
            return bool;
        }
        if (v instanceof Number number) {
            return number.intValue() != 0;
        }
        if (v instanceof String text) {
            return Boolean.parseBoolean(text);
        }
        return fallback;
    }

    public String getString(String key, String fallback) {
        Object v = values.get(key);
        return v != null ? Objects.toString(v) : fallback;
    }

    public int getColor(String key, int fallback) {
        Object v = values.get(key);
        if (v instanceof Number number) {
            return number.intValue();
        }
        if (v instanceof String text) {
            String trimmed = text.trim();
            if (trimmed.startsWith("#")) {
                trimmed = trimmed.substring(1);
            }
            if (trimmed.startsWith("0x")) {
                trimmed = trimmed.substring(2);
            }
            long parsed = Long.parseUnsignedLong(trimmed, 16);
            if (trimmed.length() <= 6) {
                parsed |= 0xFF000000L;
            }
            return (int) parsed;
        }
        return fallback;
    }

    public Map<String, Object> asMap() {
        return values;
    }
}
