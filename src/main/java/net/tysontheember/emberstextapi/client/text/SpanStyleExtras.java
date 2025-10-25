package net.tysontheember.emberstextapi.client.text;

/**
 * Marker interface that will be implemented by styles carrying additional data.
 */
public interface SpanStyleExtras {
    /**
     * Returns the associated span graph if present.
     *
     * @return span graph stub or {@code null}
     */
    default SpanGraph eta$getSpanGraph() {
        return null;
    }

    /**
     * Factory producing a stub extras implementation.
     *
     * @return stub extras instance
     */
    static SpanStyleExtras empty() {
        return new SpanStyleExtras() {
        };
    }
}
