package net.tysontheember.emberstextapi.markup;

import java.util.List;
import java.util.Map;

/**
 * Root representation for parsed Ember Markup nodes.
 */
public sealed interface RNode permits RNode.RText, RNode.RSpan {
    /**
     * Simple text run with no additional styling metadata.
     *
     * @param text literal string contents
     */
    record RText(String text) implements RNode {
        public RText {
            text = text == null ? "" : text;
        }
    }

    /**
     * A span that wraps child nodes with tag and attribute metadata.
     *
     * @param tag      markup tag name (e.g. {@code bold}, {@code gradient})
     * @param attrs    parsed attribute values
     * @param children nested nodes contained inside the span
     */
    record RSpan(String tag, Map<String, String> attrs, List<RNode> children) implements RNode {
        public RSpan {
            tag = tag == null ? "" : tag;
            attrs = attrs == null ? Map.of() : Map.copyOf(attrs);
            children = children == null ? List.of() : List.copyOf(children);
        }
    }
}
