package net.tysontheember.emberstextapi.markup;

import java.util.List;
import java.util.Map;

/**
 * Root node type emitted by {@link EmberMarkup#parse(String)}.
 */
public sealed interface RNode permits RText, RSpan {

    /**
     * A text node containing literal characters.
     */
    record RText(String text) implements RNode {
        public RText {
            text = text == null ? "" : text;
        }
    }

    /**
     * A span node wrapping zero or more child nodes under a tag name with attributes.
     */
    record RSpan(String tag, Map<String, String> attrs, List<RNode> children) implements RNode {
        public RSpan {
            tag = tag == null ? "" : tag;
            attrs = attrs == null ? Map.of() : Map.copyOf(attrs);
            children = children == null ? List.of() : List.copyOf(children);
        }

        public String attr(String name) {
            return attrs.get(name);
        }
    }
}
