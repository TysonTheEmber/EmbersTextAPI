package net.tysontheember.emberstextapi.immersivemessages.effects.params;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Immutable, type-safe parameter storage for effects.
 * <p>
 * Stores parameters as an immutable map and provides typed accessors that
 * handle type conversion automatically. Parameters can be of type:
 * <ul>
 *   <li>Double (for numeric values)</li>
 *   <li>String (for text values like colors)</li>
 *   <li>Boolean (for flags)</li>
 * </ul>
 * </p>
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * // From markup: <glitch f=2.0 j=0.02 enabled>
 * Map<String, Object> params = ImmutableMap.of(
 *     "f", 2.0,
 *     "j", 0.02,
 *     "enabled", true
 * );
 * Params typed = new TypedParams(params);
 * }</pre>
 */
public class TypedParams implements Params {

    private final ImmutableMap<String, Object> params;

    /**
     * Creates a new TypedParams from a map of parameter values.
     *
     * @param params Immutable map of parameter keys to values
     */
    public TypedParams(@NotNull ImmutableMap<String, Object> params) {
        this.params = params;
    }

    /**
     * Creates a new TypedParams from a mutable map (will be copied to immutable).
     *
     * @param params Map of parameter keys to values
     */
    public TypedParams(@NotNull Map<String, Object> params) {
        this.params = ImmutableMap.copyOf(params);
    }

    @NotNull
    @Override
    public Optional<Double> getDouble(@NotNull String key) {
        Object value = params.get(key);
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof Number) {
            return Optional.of(((Number) value).doubleValue());
        }

        // Try to parse string as double
        if (value instanceof String) {
            try {
                return Optional.of(Double.parseDouble((String) value));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<String> getString(@NotNull String key) {
        Object value = params.get(key);
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof String) {
            return Optional.of((String) value);
        }

        // Convert other types to string
        return Optional.of(value.toString());
    }

    @NotNull
    @Override
    public Optional<Boolean> getBoolean(@NotNull String key) {
        Object value = params.get(key);
        if (value == null) {
            return Optional.empty();
        }

        if (value instanceof Boolean) {
            return Optional.of((Boolean) value);
        }

        // Try to parse string as boolean
        if (value instanceof String) {
            String str = (String) value;
            if ("true".equalsIgnoreCase(str)) {
                return Optional.of(true);
            } else if ("false".equalsIgnoreCase(str)) {
                return Optional.of(false);
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean has(@NotNull String key) {
        return params.containsKey(key);
    }

    @NotNull
    @Override
    public String serialize() {
        if (params.isEmpty()) {
            return "";
        }

        return params.entrySet().stream()
                .map(entry -> {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    // Boolean true values can be represented as just the key
                    if (value instanceof Boolean && (Boolean) value) {
                        return key;
                    }

                    // Format as key=value
                    return key + "=" + value;
                })
                .collect(Collectors.joining(" "));
    }

    @Override
    public String toString() {
        return "TypedParams[" + serialize() + "]";
    }

    /**
     * Get the raw parameter map (immutable).
     *
     * @return Immutable map of all parameters
     */
    @NotNull
    public ImmutableMap<String, Object> getRawParams() {
        return params;
    }
}
