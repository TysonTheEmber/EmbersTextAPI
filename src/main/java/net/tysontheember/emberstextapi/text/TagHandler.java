package net.tysontheember.emberstextapi.text;

import java.util.Map;

/**
 * Contract for validating tag parameters used to build attributed text spans.
 */
@FunctionalInterface
public interface TagHandler {

    /**
     * Validates the provided parameters and throws an {@link IllegalArgumentException} when invalid.
     *
     * @param parameters parameters to validate
     */
    void validateParameters(Map<String, Object> parameters);
}
