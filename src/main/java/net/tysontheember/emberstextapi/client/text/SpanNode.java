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
}
