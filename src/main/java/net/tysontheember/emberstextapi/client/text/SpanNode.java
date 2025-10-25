package net.tysontheember.emberstextapi.client.text;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
    private final int[] wordBoundaries;

    public SpanNode(String name, int start, int end, Map<String, String> parameters, List<SpanNode> children) {
        this(name, start, end, parameters, children, null);
    }

    public SpanNode(String name, int start, int end, Map<String, String> parameters, List<SpanNode> children, int[] wordBoundaries) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.parameters = parameters == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(parameters));
        this.children = children == null ? Collections.emptyList() : Collections.unmodifiableList(children);
        if (wordBoundaries == null || wordBoundaries.length == 0) {
            this.wordBoundaries = new int[0];
        } else {
            this.wordBoundaries = wordBoundaries.clone();
        }
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

    public int[] getWordBoundaries() {
        return wordBoundaries;
    }

    public boolean hasParameter(String key) {
        return parameters.containsKey(key);
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = parameters.get(key);
        if (value == null) {
            return defaultValue;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return defaultValue;
        }
        return normalized.equals("true") || normalized.equals("1") || normalized.equals("yes") || normalized.equals("on");
    }

    public int getColor(String key) {
        return getColor(key, 0x000000);
    }

    public int getColor(String key, int defaultValue) {
        String value = parameters.get(key);
        if (value == null) {
            return defaultValue;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return defaultValue;
        }
        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1);
        } else if (trimmed.startsWith("0x") || trimmed.startsWith("0X")) {
            trimmed = trimmed.substring(2);
        }
        if (trimmed.length() == 3) {
            StringBuilder expanded = new StringBuilder(6);
            for (int i = 0; i < 3; i++) {
                char ch = trimmed.charAt(i);
                expanded.append(ch).append(ch);
            }
            trimmed = expanded.toString();
        }
        if (trimmed.length() > 6) {
            trimmed = trimmed.substring(trimmed.length() - 6);
        }
        try {
            return Integer.parseInt(trimmed, 16) & 0xFFFFFF;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public float getFloat(String key, float defaultValue) {
        String value = parameters.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    public SpanNode withChildren(List<SpanNode> newChildren) {
        if (newChildren == null || newChildren == this.children) {
            return this;
        }
        return new SpanNode(this.name, this.start, this.end, this.parameters, newChildren, this.wordBoundaries);
    }

    public SpanNode withWordBoundaries(int[] boundaries) {
        if (boundaries == null || boundaries.length == 0) {
            if (this.wordBoundaries.length == 0) {
                return this;
            }
            return new SpanNode(this.name, this.start, this.end, this.parameters, this.children, null);
        }
        if (this.wordBoundaries.length == boundaries.length) {
            boolean same = true;
            for (int i = 0; i < boundaries.length; i++) {
                if (this.wordBoundaries[i] != boundaries[i]) {
                    same = false;
                    break;
                }
            }
            if (same) {
                return this;
            }
        }
        return new SpanNode(this.name, this.start, this.end, this.parameters, this.children, boundaries);
    }
}
