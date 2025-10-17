package net.tysontheember.emberstextapi.text;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * A strongly typed parameter map produced by {@link ParamSpec#validate(Map, java.util.function.Consumer)}.
 */
public final class Params {
    private final Map<String, Object> values;

    Params(Map<String, Object> values) {
        this.values = values;
    }

    public static Params of(Map<String, Object> values) {
        return new Params(Collections.unmodifiableMap(new LinkedHashMap<>(values)));
    }

    public Map<String, Object> raw() {
        return values;
    }

    public boolean contains(String key) {
        return values.containsKey(normalize(key));
    }

    public String getString(String key, String fallback) {
        Object value = values.get(normalize(key));
        return value instanceof String s ? s : fallback;
    }

    public boolean getBoolean(String key, boolean fallback) {
        Object value = values.get(normalize(key));
        if (value instanceof Boolean b) {
            return b;
        }
        if (value instanceof Number n) {
            return n.intValue() != 0;
        }
        return fallback;
    }

    public int getInt(String key, int fallback) {
        Object value = values.get(normalize(key));
        if (value instanceof Number n) {
            return n.intValue();
        }
        return fallback;
    }

    public float getFloat(String key, float fallback) {
        Object value = values.get(normalize(key));
        if (value instanceof Number n) {
            return n.floatValue();
        }
        return fallback;
    }

    public double getDouble(String key, double fallback) {
        Object value = values.get(normalize(key));
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return fallback;
    }

    public int getColor(String key, int fallback) {
        Object value = values.get(normalize(key));
        if (value instanceof Number n) {
            return n.intValue();
        }
        return fallback;
    }

    @Nullable
    public EmbersKey getKey(String key) {
        Object value = values.get(normalize(key));
        if (value instanceof EmbersKey id) {
            return id;
        }
        if (value instanceof String s && !s.isBlank()) {
            return EmbersKey.parse(s);
        }
        return null;
    }

    private String normalize(String key) {
        Objects.requireNonNull(key, "key");
        return key.toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return "Params" + values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Params params)) return false;
        return Objects.equals(values, params.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }
}
