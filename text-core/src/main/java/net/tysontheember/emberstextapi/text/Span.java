package net.tysontheember.emberstextapi.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a contiguous range within an {@link AttributedText} that shares
 * the same set of {@link Attribute attributes}. Spans are stored in ascending
 * order by {@link #start()} and do not overlap.
 */
public final class Span {
    private final int start;
    private final int end;
    private final List<Attribute> attributes;

    public Span(int start, int end, List<Attribute> attributes) {
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("Invalid span range " + start + ".." + end);
        }
        this.start = start;
        this.end = end;
        this.attributes = attributes == null ? new ArrayList<>() : new ArrayList<>(attributes);
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    public List<Attribute> attributes() {
        return Collections.unmodifiableList(attributes);
    }

    public boolean isEmptyAttributes() {
        return attributes.isEmpty();
    }

    public Span withEnd(int newEnd) {
        return new Span(start, newEnd, attributes);
    }

    public Span withStart(int newStart) {
        return new Span(newStart, end, attributes);
    }

    public Span withAttributes(List<Attribute> newAttributes) {
        return new Span(start, end, newAttributes);
    }

    @Override
    public String toString() {
        return "Span{" + start + ".." + end + ", attributes=" + attributes + "}";
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
}
