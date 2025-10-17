package net.tysontheember.emberstextapi.text;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a validation issue encountered while processing parameter maps.
 */
public record ParamError(String message, @Nullable Throwable cause) {
    public ParamError {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("message may not be blank");
        }
    }
}
