package net.tysontheember.emberstextapi.immersivemessages.effects.params;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Type-safe parameter access interface for effect configuration.
 * <p>
 * Params objects are created from markup tag parsing and provide typed access
 * to effect parameters. All accessors return Optional to handle missing parameters
 * gracefully, allowing effects to use default values.
 * </p>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * // From markup: <bounce a=2.0 f=1.5 w=0.5>
 * float amplitude = params.getDouble("a").orElse(1.0);
 * float frequency = params.getDouble("f").orElse(1.0);
 * float wave = params.getDouble("w").orElse(1.0);
 * }</pre>
 *
 * @see TypedParams
 * @see EmptyParams
 */
public interface Params {

    /**
     * Get a double/float parameter value.
     *
     * @param key The parameter key (e.g., "a", "f", "w")
     * @return Optional containing the value if present, empty otherwise
     */
    @NotNull
    Optional<Double> getDouble(@NotNull String key);

    /**
     * Get a string parameter value.
     *
     * @param key The parameter key
     * @return Optional containing the value if present, empty otherwise
     */
    @NotNull
    Optional<String> getString(@NotNull String key);

    /**
     * Get a boolean parameter value.
     * <p>
     * Boolean parameters can be specified in markup as:
     * <ul>
     *   <li>{@code param=true} or {@code param=false} - explicit boolean</li>
     *   <li>{@code param} - presence flag (treated as true)</li>
     * </ul>
     * </p>
     *
     * @param key The parameter key
     * @return Optional containing the value if present, empty otherwise
     */
    @NotNull
    Optional<Boolean> getBoolean(@NotNull String key);

    /**
     * Check if a parameter is present (regardless of value).
     *
     * @param key The parameter key
     * @return true if the parameter was specified, false otherwise
     */
    boolean has(@NotNull String key);

    /**
     * Serialize parameters back to string format for network transmission.
     * <p>
     * Format: {@code param1=value1 param2=value2}
     * </p>
     *
     * @return Serialized parameter string, or empty string if no parameters
     */
    @NotNull
    String serialize();
}
