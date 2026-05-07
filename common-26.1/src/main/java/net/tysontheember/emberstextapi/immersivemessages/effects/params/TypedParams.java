package net.tysontheember.emberstextapi.immersivemessages.effects.params;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TypedParams implements Params {

    private final ImmutableMap<String, Object> params;

    public TypedParams(@NotNull ImmutableMap<String, Object> params) {
        this.params = params;
    }

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

                    if (value instanceof Boolean && (Boolean) value) {
                        return key;
                    }

                    return key + "=" + value;
                })
                .collect(Collectors.joining(" "));
    }

    @Override
    public String toString() {
        return "TypedParams[" + serialize() + "]";
    }

    @NotNull
    public ImmutableMap<String, Object> getRawParams() {
        return params;
    }
}
