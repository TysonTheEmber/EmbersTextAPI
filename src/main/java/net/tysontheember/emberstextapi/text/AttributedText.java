package net.tysontheember.emberstextapi.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable span-based text model composed of the source string and associated {@link Span}s.
 */
public final class AttributedText {

    private final String text;
    private final List<Span> spans;

    private AttributedText(Builder builder) {
        this.text = builder.text;
        this.spans = Collections.unmodifiableList(new ArrayList<>(builder.spans));
    }

    /**
     * Creates a new builder instance.
     *
     * @return the builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * The raw text backing this attributed text instance.
     *
     * @return immutable string value
     */
    public String getText() {
        return text;
    }

    /**
     * Ordered span list describing the formatting attributes applied to {@link #getText()}.
     *
     * @return immutable list of spans
     */
    public List<Span> getSpans() {
        return spans;
    }

    /**
     * Builder for {@link AttributedText}.
     */
    public static final class Builder {

        private String text = "";
        private final List<Span> spans = new ArrayList<>();

        private Builder() {
        }

        /**
         * Sets the source text.
         *
         * @param text raw text value
         * @return this builder for chaining
         */
        public Builder text(String text) {
            this.text = Objects.requireNonNull(text, "text");
            return this;
        }

        /**
         * Adds a span to the text.
         *
         * @param span span configuration to add
         * @return this builder for chaining
         */
        public Builder addSpan(Span span) {
            this.spans.add(Objects.requireNonNull(span, "span"));
            return this;
        }

        /**
         * Adds every span from the provided collection.
         *
         * @param spans spans to add
         * @return this builder for chaining
         */
        public Builder addSpans(List<Span> spans) {
            Objects.requireNonNull(spans, "spans");
            for (Span span : spans) {
                addSpan(span);
            }
            return this;
        }

        /**
         * Constructs the {@link AttributedText} instance.
         *
         * @return immutable attributed text
         */
        public AttributedText build() {
            Objects.requireNonNull(text, "text");
            return new AttributedText(this);
        }
    }
}
