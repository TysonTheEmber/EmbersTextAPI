package net.tysontheember.emberstextapi.dsl;

import net.tysontheember.emberstextapi.markup.RNode;
import net.tysontheember.emberstextapi.markup.RSpan;
import net.tysontheember.emberstextapi.markup.RText;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fluent DSL for building {@link RNode} hierarchies programmatically.
 */
public final class Rich {
    private Rich() {
    }

    public static Builder text(String text) {
        Builder builder = new Builder();
        builder.text(text);
        return builder;
    }

    public static SpanBuilder<RSpan> span(String tag) {
        return new SpanBuilder<>(tag, span -> span);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends NodeCollector<Builder> {
        Builder() {
            super();
        }

        @Override
        Builder self() {
            return this;
        }

        public Builder then(RNode node) {
            if (node != null) {
                children.add(node);
            }
            return this;
        }

        public Builder then(Builder builder) {
            if (builder != null) {
                children.addAll(builder.children);
            }
            return this;
        }

        public RNode build() {
            if (children.size() == 1) {
                return children.get(0);
            }
            return new RSpan("root", Map.of(), List.copyOf(children));
        }
    }

    public static final class SpanBuilder<T> extends NodeCollector<SpanBuilder<T>> {
        private final String tag;
        private final Map<String, String> attrs = new LinkedHashMap<>();
        private final java.util.function.Function<RSpan, T> closer;

        SpanBuilder(String tag, java.util.function.Function<RSpan, T> closer) {
            this.tag = tag;
            this.closer = closer;
        }

        @Override
        SpanBuilder<T> self() {
            return this;
        }

        public SpanBuilder<T> attr(String key, String value) {
            if (key != null && value != null) {
                attrs.put(key.toLowerCase(), value);
            }
            return this;
        }

        public SpanBuilder<SpanBuilder<T>> span(String tag) {
            SpanBuilder<SpanBuilder<T>> nested = new SpanBuilder<>(tag, span -> {
                this.children.add(span);
                return this;
            });
            return nested;
        }

        public T close() {
            return closer.apply(new RSpan(tag, Map.copyOf(attrs), List.copyOf(children)));
        }
    }

    private abstract static class NodeCollector<T extends NodeCollector<T>> {
        protected final List<RNode> children = new ArrayList<>();

        NodeCollector() {
        }

        abstract T self();

        public T text(String text) {
            children.add(new RText(text));
            return self();
        }

        public T then(RNode node) {
            if (node != null) {
                children.add(node);
            }
            return self();
        }

        public SpanBuilder<T> span(String tag) {
            return new SpanBuilder<>(tag, span -> {
                this.children.add(span);
                return self();
            });
        }
    }
}
