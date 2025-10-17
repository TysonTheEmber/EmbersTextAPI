package net.tysontheember.emberstextapi.inline;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Span of attributes applied to a substring.
 */
public final class AttributeSpan {
    public static final Comparator<AttributeSpan> ORDER = Comparator
            .comparingInt(AttributeSpan::start)
            .thenComparingInt(AttributeSpan::end);

    private final int start;
    private final int end;
    private final List<TagAttribute> attributes;

    public AttributeSpan(int start, int end, List<TagAttribute> attributes) {
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("Invalid span: " + start + "-" + end);
        }
        this.start = start;
        this.end = end;
        this.attributes = List.copyOf(Objects.requireNonNull(attributes, "attributes"));
    }

    public int start() {
        return start;
    }

    public int end() {
        return end;
    }

    public List<TagAttribute> attributes() {
        return attributes;
    }
}
