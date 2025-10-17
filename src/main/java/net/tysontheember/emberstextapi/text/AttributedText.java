package net.tysontheember.emberstextapi.text;

import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.text.parse.TagParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Immutable representation of a string with attributed spans.
 */
public final class AttributedText {
    private final String raw;
    private final List<Span> spans;

    private AttributedText(String raw, List<Span> spans) {
        this.raw = Objects.requireNonNull(raw, "raw");
        if (spans == null || spans.isEmpty()) {
            this.spans = Collections.emptyList();
        } else {
            this.spans = Collections.unmodifiableList(new ArrayList<>(spans));
        }
    }

    public String raw() {
        return raw;
    }

    public List<Span> spans() {
        return spans;
    }

    public static AttributedText of(String raw) {
        return new AttributedText(raw, List.of());
    }

    public AttributedText apply(Attribute attribute, int start, int end) {
        Objects.requireNonNull(attribute, "attribute");
        Span span = new Span(start, end, List.of(attribute));
        List<Span> newSpans = new ArrayList<>(spans);
        newSpans.add(span);
        return new AttributedText(raw, newSpans);
    }

    public static AttributedText parse(String tagged) {
        if (tagged == null || tagged.isEmpty()) {
            return AttributedText.of(tagged == null ? "" : tagged);
        }
        return new TagParser().parse(tagged);
    }

    public Builder toBuilder() {
        return new Builder(raw, spans);
    }

    public static Builder builder(String raw) {
        return new Builder(raw, List.of());
    }

    public static FluentBuilder at(String text) {
        return new FluentBuilder(text);
    }

    public static final class Builder {
        private String raw;
        private final List<Span> spans = new ArrayList<>();

        private Builder(String raw, List<Span> copy) {
            this.raw = Objects.requireNonNull(raw, "raw");
            spans.addAll(copy);
        }

        public Builder raw(String raw) {
            this.raw = Objects.requireNonNull(raw, "raw");
            return this;
        }

        public Builder addSpan(Span span) {
            spans.add(Objects.requireNonNull(span));
            return this;
        }

        public Builder span(int start, int end, Attribute attribute) {
            spans.add(new Span(start, end, List.of(attribute)));
            return this;
        }

        public AttributedText build() {
            return new AttributedText(raw, spans);
        }
    }

    /**
     * Fluent DSL for programmatic construction of attributed text.
     */
    public static final class FluentBuilder {
        private final Builder delegate;
        private final String text;
        private final List<Attribute> pending = new ArrayList<>();

        private FluentBuilder(String text) {
            this.text = Objects.requireNonNull(text, "text");
            this.delegate = AttributedText.builder(text);
        }

        public FluentBuilder apply(Attribute attribute) {
            pending.add(Objects.requireNonNull(attribute));
            return this;
        }

        private FluentBuilder applyBuiltin(String id, Consumer<Attribute.Builder> params) {
            ResourceLocation rl = Attributes.resolve(id);
            Attribute.Builder builder = Attribute.builder(rl);
            if (params != null) {
                params.accept(builder);
            }
            return apply(builder.build());
        }

        public FluentBuilder bold() {
            return applyBuiltin("bold", b -> {});
        }

        public FluentBuilder italic() {
            return applyBuiltin("italic", b -> {});
        }

        public FluentBuilder color(int rgb) {
            return applyBuiltin("color", b -> b.param("value", rgb));
        }

        public FluentBuilder grad(String from, String to) {
            return applyBuiltin("grad", b -> {
                b.param("from", from);
                b.param("to", to);
            });
        }

        public FluentBuilder typewriter(float speed) {
            return applyBuiltin("typewriter", b -> b.param("speed", speed));
        }

        public FluentBuilder wiggle(float amplitude, float frequency) {
            return applyBuiltin("wiggle", b -> {
                b.param("a", amplitude);
                b.param("f", frequency);
            });
        }

        public AttributedText done() {
            int end = text.length();
            for (Attribute attribute : pending) {
                delegate.span(0, end, attribute);
            }
            return delegate.build();
        }
    }
}
