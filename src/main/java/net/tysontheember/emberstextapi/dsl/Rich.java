package net.tysontheember.emberstextapi.dsl;

import net.tysontheember.emberstextapi.markup.RNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fluent builder API mirroring Ember Markup spans.
 */
public final class Rich {
    private Rich() {
    }

    public static Builder text(String text) {
        return new Builder().text(text);
    }

    public static SpanBuilder span(String tag) {
        return new Builder().span(tag);
    }

    public static final class Builder {
        private final List<RNode> children = new ArrayList<>();

        public Builder text(String text) {
            if (text != null && !text.isEmpty()) {
                children.add(new RNode.RText(text));
            }
            return this;
        }

        public Builder node(RNode node) {
            if (node != null) {
                children.add(node);
            }
            return this;
        }

        public SpanBuilder span(String tag) {
            return new SpanBuilder(this, null, tag);
        }

        public Builder then(RNode node) {
            return node(node);
        }

        public Builder then(Builder other) {
            if (other != null) {
                children.addAll(other.children);
            }
            return this;
        }

        public RNode build() {
            if (children.isEmpty()) {
                return new RNode.RText("");
            }
            if (children.size() == 1) {
                return children.get(0);
            }
            return new RNode.RSpan("root", Map.of(), List.copyOf(children));
        }
    }

    public static final class SpanBuilder {
        private final Builder parentBuilder;
        private final SpanBuilder parentSpan;
        private final String tag;
        private final Map<String, String> attrs = new LinkedHashMap<>();
        private final List<RNode> children = new ArrayList<>();

        private SpanBuilder(Builder parent, SpanBuilder parentSpan, String tag) {
            this.parentBuilder = parent;
            this.parentSpan = parentSpan;
            this.tag = tag == null ? "" : tag;
        }

        public SpanBuilder attr(String key, String value) {
            if (key != null && value != null) {
                attrs.put(key.toLowerCase(), value);
            }
            return this;
        }

        public SpanBuilder text(String text) {
            if (text != null && !text.isEmpty()) {
                children.add(new RNode.RText(text));
            }
            return this;
        }

        public SpanBuilder node(RNode node) {
            if (node != null) {
                children.add(node);
            }
            return this;
        }

        public SpanBuilder span(String tag) {
            return new SpanBuilder(null, this, tag);
        }

        private void addChild(RNode node) {
            if (node != null) {
                children.add(node);
            }
        }

        public Builder close() {
            if (parentBuilder == null) {
                throw new IllegalStateException("Span is nested; use end() to close");
            }
            parentBuilder.children.add(new RNode.RSpan(tag, attrs, List.copyOf(children)));
            return parentBuilder;
        }

        public SpanBuilder end() {
            if (parentSpan != null) {
                parentSpan.addChild(new RNode.RSpan(tag, attrs, List.copyOf(children)));
                return parentSpan;
            }
            throw new IllegalStateException("Cannot end a root span");
        }

        public Builder closeAll() {
            SpanBuilder current = this;
            while (current.parentSpan != null) {
                current = current.end();
            }
            if (current.parentBuilder != null) {
                return current.close();
            }
            throw new IllegalStateException("Root span cannot be closed without a builder");
        }
    }
}
