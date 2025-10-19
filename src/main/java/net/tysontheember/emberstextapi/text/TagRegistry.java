package net.tysontheember.emberstextapi.text;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Registry for {@link TagHandler} instances keyed by their tag name.
 */
public final class TagRegistry {

    private final Map<String, TagHandler> handlers = new HashMap<>();

    /**
     * Registers a new handler for the provided tag name.
     *
     * @param name    unique tag name
     * @param handler tag handler implementation
     */
    public void register(String name, TagHandler handler) {
        handlers.put(normalize(name), Objects.requireNonNull(handler, "handler"));
    }

    /**
     * Retrieves the handler associated with the supplied tag name.
     *
     * @param name tag name
     * @return optional handler
     */
    public Optional<TagHandler> get(String name) {
        return Optional.ofNullable(handlers.get(normalize(name)));
    }

    /**
     * Validates the provided parameters using the handler for the supplied tag name, when present.
     *
     * @param name       tag name to validate
     * @param parameters parameters to validate
     */
    public void validate(String name, Map<String, Object> parameters) {
        get(name).ifPresent(handler -> handler.validateParameters(parameters));
    }

    /**
     * Provides an immutable view of the registered handlers.
     *
     * @return immutable handler map
     */
    public Map<String, TagHandler> getHandlers() {
        return Collections.unmodifiableMap(new HashMap<>(handlers));
    }

    private String normalize(String name) {
        return Objects.requireNonNull(name, "name").toLowerCase();
    }
}
