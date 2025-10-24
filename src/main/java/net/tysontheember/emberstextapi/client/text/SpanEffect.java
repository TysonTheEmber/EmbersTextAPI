package net.tysontheember.emberstextapi.client.text;

import java.util.Map;
import java.util.Objects;

/**
 * Lightweight description of an effect span pushed by Embers Text API markup.
 * Instances are immutable and safe to reuse across threads.
 */
public final class SpanEffect {
    private final String id;
    private final Map<String, String> parameters;

    public SpanEffect(String id, Map<String, String> parameters) {
        this.id = Objects.requireNonNull(id, "id");
        this.parameters = parameters == null || parameters.isEmpty() ? Map.of() : Map.copyOf(parameters);
    }

    public static SpanEffect of(String id) {
        return new SpanEffect(id, Map.of());
    }

    public String id() {
        return this.id;
    }

    public Map<String, String> parameters() {
        return this.parameters;
    }

    public boolean hasParameters() {
        return !this.parameters.isEmpty();
    }

    @Override
    public String toString() {
        return "SpanEffect{" + "id='" + this.id + '\'' + ", parameters=" + this.parameters + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.parameters);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SpanEffect spanEffect)) {
            return false;
        }
        return this.id.equals(spanEffect.id) && this.parameters.equals(spanEffect.parameters);
    }
}
