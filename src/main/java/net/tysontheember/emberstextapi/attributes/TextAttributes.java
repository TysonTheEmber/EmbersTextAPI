package net.tysontheember.emberstextapi.attributes;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry of markup attribute handlers.
 */
public final class TextAttributes {
    private static final Map<String, AttributeHandler> HANDLERS = new ConcurrentHashMap<>();

    private TextAttributes() {
    }

    public static void register(AttributeHandler handler) {
        HANDLERS.put(handler.name().toLowerCase(), handler);
    }

    public static AttributeHandler get(String name) {
        if (name == null) {
            return null;
        }
        return HANDLERS.get(name.toLowerCase());
    }
}
