package net.tysontheember.emberstextapi.dsl;

import net.tysontheember.emberstextapi.markup.RNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.tysontheember.emberstextapi.markup.RNode.RSpan;
import static net.tysontheember.emberstextapi.markup.RNode.RText;

/**
 * Small fluent DSL for programmatic construction of markup trees.
 */
public final class Rich {
    private Rich() {
    }

    public static Builder text(String literal) {
        Builder builder = new Builder();
        builder.text(literal);
        return builder;
    }

    public static StandaloneSpanBuilder span(String tag) {
        return new StandaloneSpanBuilder(tag);
    }

    public static final class Builder {
        private final List<RNode> nodes = new ArrayList<>();

        public Builder then(RNode node) {
            nodes.add(node);
            return this;
        }

        public Builder text(String literal) {
            nodes.add(new RText(literal));
            return this;
        }

        public InlineSpanBuilder span(String tag) {
            return new InlineSpanBuilder(this, tag);
        }

        public RSpan build() {
            return new RSpan("root", Map.of(), List.copyOf(nodes));
        }
    }

    public abstract static class SpanBuilderBase<T extends SpanBuilderBase<T>> {
        protected final String tag;
        protected final Map<String, String> attrs = new LinkedHashMap<>();
        protected final List<RNode> children = new ArrayList<>();

        protected SpanBuilderBase(String tag) {
            this.tag = tag;
        }

        public T attr(String key, String value) {
            attrs.put(key, value);
            return self();
        }

        public T text(String literal) {
            children.add(new RText(literal));
            return self();
        }

        public NestedSpanBuilder<T> span(String tag) {
            return new NestedSpanBuilder<>(this, tag);
        }

        public T then(RNode node) {
            children.add(node);
            return self();
        }

        protected abstract T self();

        protected RSpan finish() {
            return new RSpan(tag, Map.copyOf(attrs), List.copyOf(children));
        }

        private void addChild(RNode node) {
            children.add(node);
        }
    }

    public static final class NestedSpanBuilder<P extends SpanBuilderBase<?>> extends SpanBuilderBase<NestedSpanBuilder<P>> {
        private final P parent;

        private NestedSpanBuilder(P parent, String tag) {
            super(tag);
            this.parent = parent;
        }

        @Override
        protected NestedSpanBuilder<P> self() {
            return this;
        }

        public P close() {
            parent.addChild(finish());
            return parent;
        }
    }

    public static final class StandaloneSpanBuilder extends SpanBuilderBase<StandaloneSpanBuilder> {
        private StandaloneSpanBuilder(String tag) {
            super(tag);
        }

        @Override
        protected StandaloneSpanBuilder self() {
            return this;
        }

        public RSpan close() {
            return finish();
        }
    }

    public static final class InlineSpanBuilder extends SpanBuilderBase<InlineSpanBuilder> {
        private final Builder parent;

        private InlineSpanBuilder(Builder parent, String tag) {
            super(tag);
            this.parent = parent;
        }

        @Override
        protected InlineSpanBuilder self() {
            return this;
        }

        public Builder close() {
            parent.then(finish());
            return parent;
        }
    }
}
