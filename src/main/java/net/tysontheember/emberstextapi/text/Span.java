package net.tysontheember.emberstextapi.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a half-open interval of characters that share a set of attributes.
 */
public final class Span {
    private final int start;
    private final int end;
    private final List<Attribute> attributes;

    public Span(int start, int end, List<Attribute> attributes) {
        if (start < 0) {
            throw new IllegalArgumentException("start must be >= 0");
        }
        if (end < start) {
            throw new IllegalArgumentException("end must be >= start");
        }
        this.start = start;
        this.end = end;
        if (attributes == null || attributes.isEmpty()) {
            this.attributes = Collections.emptyList();
        } else {
            this.attributes = Collections.unmodifiableList(new ArrayList<>(attributes));
        }
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    public List<Attribute> attributes() {
        return attributes;
    }

    public Span withBounds(int newStart, int newEnd) {
        return new Span(newStart, newEnd, attributes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Span span)) return false;
        return start == span.start && end == span.end && attributes.equals(span.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, attributes);
    }

    @Override
    public String toString() {
        return "Span{" +
            "start=" + start +
            ", end=" + end +
            ", attributes=" + attributes +
            '}';
    }
}
