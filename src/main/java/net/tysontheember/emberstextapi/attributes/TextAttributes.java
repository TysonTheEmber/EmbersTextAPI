package net.tysontheember.emberstextapi.attributes;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global registry for {@link AttributeHandler} instances.
 */
public final class TextAttributes {
    private static final Map<String, AttributeHandler> REGISTRY = new ConcurrentHashMap<>();

    private TextAttributes() {
    }

    public static void register(AttributeHandler handler) {
        REGISTRY.put(handler.name(), handler);
    }

    public static AttributeHandler get(String name) {
        return REGISTRY.get(name);
    }

    public static Collection<AttributeHandler> all() {
        return REGISTRY.values();
    }
}
