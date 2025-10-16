package net.tysontheember.emberstextapi.text;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Describes the parameters accepted by a {@link TextAttributeFactory}. Each
 * parameter entry declares the expected {@link ParamType}, optional range
 * information and a default value. The {@link #validate(Map, BiConsumer)}
 * method converts a loosely typed input map into a strongly typed {@link Params}
 * instance.
 */
public final class ParamSpec {
    private final Map<String, Entry> entries;
    private final Map<String, String> aliases;

    private ParamSpec(Builder builder) {
        this.entries = ImmutableMap.copyOf(builder.entries);
        this.aliases = ImmutableMap.copyOf(builder.aliases);
    }

    public Params validate(Map<String, Object> raw, BiConsumer<String, Throwable> warningSink) {
        Objects.requireNonNull(raw, "raw");
        Map<String, Object> validated = new LinkedHashMap<>();

        raw.forEach((key, value) -> {
            String normalized = key.toLowerCase(Locale.ROOT);
            String target = normalized;
            if (!entries.containsKey(normalized)) {
                target = aliases.get(normalized);
            }
            if (target == null || !entries.containsKey(target)) {
                if (warningSink != null) {
                    warningSink.accept("Unknown parameter '" + key + "'", null);
                }
                return;
            }
            Entry entry = entries.get(target);
            Object converted;
            try {
                converted = entry.type.convert(value);
            } catch (Exception ex) {
                if (warningSink != null) {
                    warningSink.accept("Failed to parse parameter '" + key + "' as " + entry.type.name().toLowerCase(Locale.ROOT), ex);
                }
                return;
            }
            if (converted == null) {
                converted = entry.defaultValue;
            }
            if (entry.hasRange && converted instanceof Number number) {
                double d = number.doubleValue();
                if (d < entry.min || d > entry.max) {
                    double clamped = Math.min(entry.max, Math.max(entry.min, d));
                    if (warningSink != null) {
                        warningSink.accept("Parameter '" + key + "' clamped to range [" + entry.min + ", " + entry.max + "]", null);
                    }
                    d = clamped;
                }
                if (converted instanceof Integer) {
                    converted = (int) Math.round(d);
                } else if (converted instanceof Float) {
                    converted = (float) d;
                } else {
                    converted = d;
                }
            }
            validated.put(target, converted);
        });

        for (Map.Entry<String, Entry> entry : entries.entrySet()) {
            if (!validated.containsKey(entry.getKey())) {
                validated.put(entry.getKey(), entry.getValue().defaultValue);
            }
        }

        return new Params(Collections.unmodifiableMap(validated));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, Entry> entries = new LinkedHashMap<>();
        private final Map<String, String> aliases = new LinkedHashMap<>();

        public Builder add(String name, ParamType type) {
            return add(name, type, null);
        }

        public Builder add(String name, ParamType type, Object defaultValue) {
            String normalized = name.toLowerCase(Locale.ROOT);
            if (entries.containsKey(normalized)) {
                throw new IllegalArgumentException("Parameter already defined: " + name);
            }
            entries.put(normalized, new Entry(normalized, type, defaultValue, false, 0, 0));
            return this;
        }

        public Builder range(String name, double min, double max) {
            String normalized = name.toLowerCase(Locale.ROOT);
            Entry entry = entries.get(normalized);
            if (entry == null) {
                throw new IllegalArgumentException("Unknown parameter: " + name);
            }
            entries.put(normalized, entry.withRange(min, max));
            return this;
        }

        public Builder alias(String existing, String alias) {
            String normalized = existing.toLowerCase(Locale.ROOT);
            if (!entries.containsKey(normalized)) {
                throw new IllegalArgumentException("Cannot register alias for unknown parameter: " + existing);
            }
            aliases.put(alias.toLowerCase(Locale.ROOT), normalized);
            return this;
        }

        public ParamSpec build() {
            return new ParamSpec(this);
        }
    }

    private record Entry(String name, ParamType type, Object defaultValue, boolean hasRange, double min, double max) {
        Entry withRange(double min, double max) {
            if (min > max) {
                throw new IllegalArgumentException("min > max");
            }
            return new Entry(name, type, defaultValue, true, min, max);
        }
    }
}
