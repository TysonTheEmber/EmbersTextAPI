package net.tysontheember.emberstextapi.inline;

import java.util.Collections;
import java.util.Map;

public final class TagToken {
    private final String name;
    private final Map<String, String> attributes;

    public TagToken(String name, Map<String, String> attributes) {
        this.name = name;
        this.attributes = Collections.unmodifiableMap(attributes);
    }

    public String name() {
        return name;
    }

    public Map<String, String> attributes() {
        return attributes;
    }

    public float getFloat(String key, float defaultValue) {
        String value = attributes.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public int getInt(String key, int defaultValue) {
        String value = attributes.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public String getString(String key, String defaultValue) {
        return attributes.getOrDefault(key, defaultValue);
    }
}
