package net.tysontheember.emberstextapi.inline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable representation of text decorated with attribute spans.
 */
public final class AttributedText {
    private final String raw;
    private final List<AttributeSpan> spans;

    AttributedText(String raw, List<AttributeSpan> spans) {
        this.raw = Objects.requireNonNull(raw, "raw");
        this.spans = Collections.unmodifiableList(new ArrayList<>(spans));
    }

    public static AttributedText of(String raw) {
        return new AttributedText(raw, List.of());
    }

    public static Builder builder(String raw) {
        return new Builder(raw);
    }

    public String raw() {
        return raw;
    }

    public List<AttributeSpan> spans() {
        return spans;
    }

    public Builder toBuilder() {
        Builder builder = new Builder(raw);
        spans.forEach(builder::addSpan);
        return builder;
    }

    public static final class Builder {
        private final String raw;
        private final List<AttributeSpan> spans = new ArrayList<>();

        private Builder(String raw) {
            this.raw = Objects.requireNonNull(raw, "raw");
        }

        public SpanBuilder span(int start, int end) {
            if (start < 0 || end < start || end > raw.length()) {
                throw new IllegalArgumentException("Invalid span: " + start + "-" + end);
            }
            return new SpanBuilder(this, start, end);
        }

        private void addSpan(AttributeSpan span) {
            spans.add(span);
        }

        public AttributedText build() {
            spans.sort(AttributeSpan.ORDER);
            return new AttributedText(raw, spans);
        }

        public final class SpanBuilder {
            private final Builder parent;
            private final int start;
            private final int end;
            private final List<TagAttribute> attributes = new ArrayList<>();

            private SpanBuilder(Builder parent, int start, int end) {
                this.parent = parent;
                this.start = start;
                this.end = end;
            }

            public SpanBuilder add(TagAttribute attribute) {
                attributes.add(Objects.requireNonNull(attribute, "attribute"));
                return this;
            }

            public Builder up() {
                parent.addSpan(new AttributeSpan(start, end, List.copyOf(attributes)));
                return parent;
            }
        }
    }
}
