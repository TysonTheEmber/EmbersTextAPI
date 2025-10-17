package net.tysontheember.emberstextapi.text;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a single text attribute applied to a span within an
 * {@link AttributedText}. Attributes are identified by an {@link EmbersKey}
 * and may carry arbitrary typed parameters.
 */
public final class Attribute {
    private final EmbersKey id;
    private final Params params;

    public Attribute(EmbersKey id, Params params) {
        this.id = Objects.requireNonNull(id, "id");
        this.params = params == null ? Params.of(Map.of()) : params;
    }

    public EmbersKey id() {
        return id;
    }

    public Params params() {
        return params;
    }

    @Override
    public String toString() {
        return "Attribute{" + id + ", params=" + params + "}";
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
}
