package net.tysontheember.emberstextapi.client.text;

/**
 * Marker interface for span-based text effects.
 */
public interface SpanEffect {
    /**
     * Creates a no-op span effect placeholder.
     *
     * @return new span effect stub
     */
    static SpanEffect noop() {
        return new SpanEffect() {
        };
    }
}
