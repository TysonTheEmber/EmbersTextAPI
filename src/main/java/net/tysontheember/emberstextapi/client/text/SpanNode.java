package net.tysontheember.emberstextapi.client.text;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes a single node in a span graph.
 */
public final class SpanNode {
    private final String name;
    private final int start;
    private final int end;
    private final Map<String, String> parameters;
    private final List<SpanNode> children;

    public SpanNode(String name, int start, int end, Map<String, String> parameters, List<SpanNode> children) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.parameters = parameters == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(parameters));
        this.children = children == null ? Collections.emptyList() : Collections.unmodifiableList(children);
    }

    public String getName() {
        return name;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public List<SpanNode> getChildren() {
        return children;
    }
}
