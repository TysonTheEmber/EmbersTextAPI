package net.tysontheember.emberstextapi.client.spans;

import java.util.Objects;

/**
 * Read-only view describing the location and styling attributes for a parsed span.
 */
public final class TextSpanView {
    private final int start;
    private final int end;
    private final SpanAttr attr;

    public TextSpanView(int start, int end, SpanAttr attr) {
        if (start < 0) {
            throw new IllegalArgumentException("start must be non-negative");
        }
        if (end < start) {
            throw new IllegalArgumentException("end must be greater than or equal to start");
        }
        this.start = start;
        this.end = end;
        this.attr = Objects.requireNonNull(attr, "attr");
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    public SpanAttr attr() {
        return attr;
    }
}
