package net.tysontheember.emberstextapi.text;

import java.util.Objects;

/**
 * Describes a contiguous segment of text with an associated {@link AttributeSet}.
 */
public final class Span {

    private final int start;
    private final int end;
    private final AttributeSet attributes;

    private Span(Builder builder) {
        this.start = builder.start;
        this.end = builder.end;
        this.attributes = builder.attributes;
    }

    /**
     * Creates a new builder for {@link Span}.
     *
     * @return the builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Inclusive start index of the span within the {@link AttributedText#getText()} string.
     *
     * @return zero-based start index
     */
    public int getStart() {
        return start;
    }

    /**
     * Exclusive end index of the span within the {@link AttributedText#getText()} string.
     *
     * @return zero-based end index
     */
    public int getEnd() {
        return end;
    }

    /**
     * Formatting attributes applied to this span.
     *
     * @return immutable attribute set
     */
    public AttributeSet getAttributes() {
        return attributes;
    }

    /**
     * Builder for {@link Span}.
     */
    public static final class Builder {

        private int start;
        private int end;
        private AttributeSet attributes = AttributeSet.builder().build();

        private Builder() {
        }

        /**
         * Sets the inclusive start index.
         *
         * @param start zero-based index
         * @return this builder for chaining
         */
        public Builder start(int start) {
            if (start < 0) {
                throw new IllegalArgumentException("start must be non-negative");
            }
            this.start = start;
            return this;
        }

        /**
         * Sets the exclusive end index.
         *
         * @param end zero-based index
         * @return this builder for chaining
         */
        public Builder end(int end) {
            if (end < 0) {
                throw new IllegalArgumentException("end must be non-negative");
            }
            this.end = end;
            return this;
        }

        /**
         * Configures the attribute set for this span.
         *
         * @param attributes attribute set to use
         * @return this builder for chaining
         */
        public Builder attributes(AttributeSet attributes) {
            this.attributes = Objects.requireNonNull(attributes, "attributes");
            return this;
        }

        /**
         * Builds the {@link Span} instance.
         *
         * @return immutable span
         */
        public Span build() {
            if (end < start) {
                throw new IllegalStateException("end must not be less than start");
            }
            Objects.requireNonNull(attributes, "attributes");
            return new Span(this);
        }
    }
}
