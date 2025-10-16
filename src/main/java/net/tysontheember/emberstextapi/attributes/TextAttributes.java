package net.tysontheember.emberstextapi.attributes;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global registry for {@link AttributeHandler} implementations.
 */
public final class TextAttributes {
    private static final Map<String, AttributeHandler> HANDLERS = new LinkedHashMap<>();

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

    public static Map<String, AttributeHandler> all() {
        return Collections.unmodifiableMap(HANDLERS);
    }
}
