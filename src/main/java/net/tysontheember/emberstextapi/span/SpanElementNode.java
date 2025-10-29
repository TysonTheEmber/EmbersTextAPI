package net.tysontheember.emberstextapi.span;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a parsed span element with optional attributes and child nodes.
 */
public final class SpanElementNode extends SpanNode {
    private final String tagName;
    private final Map<String, String> attributes;
    private final List<SpanNode> children;

    public SpanElementNode(String tagName, Map<String, String> attributes, List<SpanNode> children) {
        this.tagName = tagName;
        this.attributes = attributes == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
        this.children = children == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(children));
    }

    public String getTagName() {
        return tagName;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public List<SpanNode> getChildren() {
        return children;
    }

    @Override
    void appendPlainText(StringBuilder builder) {
        for (SpanNode child : children) {
            child.appendPlainText(builder);
        }
    }

    @Override
    public String toString() {
        return "<" + tagName + attributes + ">" + children + "</" + tagName + ">";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SpanElementNode other)) {
            return false;
        }
        return Objects.equals(tagName, other.tagName)
                && Objects.equals(attributes, other.attributes)
                && Objects.equals(children, other.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagName, attributes, children);
    }
}
