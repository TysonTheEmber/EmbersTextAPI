package net.tysontheember.emberstextapi.client.text;

import java.util.Collections;
import java.util.List;

/**
 * Container representing the parsed span graph for a string.
 */
public final class SpanGraph {
    private static final SpanGraph EMPTY = new SpanGraph(Collections.emptyList(), 0, null);

    private final List<SpanNode> roots;
    private final int sanitizedLength;
    private final String signature;

    public SpanGraph(List<SpanNode> roots, int sanitizedLength, String signature) {
        this.roots = roots == null ? Collections.emptyList() : Collections.unmodifiableList(roots);
        this.sanitizedLength = sanitizedLength;
        this.signature = signature;
    }

    public static SpanGraph empty() {
        return EMPTY;
    }

    public List<SpanNode> getRoots() {
        return roots;
    }

    public int getSanitizedLength() {
        return sanitizedLength;
    }

    public String getSignature() {
        return signature;
    }

    public boolean isEmpty() {
        return roots.isEmpty();
    }
}
