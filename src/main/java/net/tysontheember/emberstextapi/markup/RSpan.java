package net.tysontheember.emberstextapi.markup;

import java.util.List;
import java.util.Map;

/**
 * Span node containing tag metadata and child nodes.
 */
public record RSpan(String tag, Map<String, String> attrs, List<RNode> children) implements RNode {
    public String attr(String key) {
        if (key == null || attrs == null) {
            return null;
        }
        return attrs.get(key.toLowerCase());
    }
}
