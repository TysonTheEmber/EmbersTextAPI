package net.tysontheember.emberstextapi.text;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single attribute applied to a span of characters.
 */
public final class Attribute {
    private final ResourceLocation id;
    private final Map<String, Object> params;

    public Attribute(ResourceLocation id, Map<String, Object> params) {
        this.id = Objects.requireNonNull(id, "id");
        if (params == null || params.isEmpty()) {
            this.params = Collections.emptyMap();
        } else {
            this.params = Collections.unmodifiableMap(new LinkedHashMap<>(params));
        }
    }

    public ResourceLocation id() {
        return id;
    }

    public Map<String, Object> params() {
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attribute attribute)) return false;
        return id.equals(attribute.id) && params.equals(attribute.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, params);
    }

    @Override
    public String toString() {
        return "Attribute{" +
            "id=" + id +
            ", params=" + params +
            '}';
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final ResourceLocation id;
        private final Map<String, Object> params = new LinkedHashMap<>();

        private Builder(ResourceLocation id) {
            this.id = Objects.requireNonNull(id, "id");
        }

        public Builder param(String key, Object value) {
            if (value != null) {
                params.put(key, value);
            }
            return this;
        }

        public Attribute build() {
            return new Attribute(id, params);
        }
    }
}
