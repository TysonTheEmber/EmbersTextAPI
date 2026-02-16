package net.tysontheember.emberstextapi.immersivemessages.effects.params;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for validating and clamping effect parameters with logging.
 * <p>
 * When a parameter value is outside the valid range, it will be clamped to the
 * nearest valid value and a warning will be logged to help users identify
 * configuration issues.
 * </p>
 */
public final class ValidationHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationHelper.class);

    private ValidationHelper() {
        // Utility class
    }

    /**
     * Clamp a float value to a range, logging a warning if clamping occurs.
     *
     * @param effectName Name of the effect (for logging)
     * @param paramName  Name of the parameter (for logging)
     * @param value      The value to clamp
     * @param min        Minimum allowed value
     * @param max        Maximum allowed value
     * @return Clamped value within [min, max]
     */
    public static float clamp(String effectName, String paramName, float value, float min, float max) {
        if (value < min) {
            LOGGER.warn("Effect '{}': param '{}' value {} below minimum, using {}",
                    effectName, paramName, value, min);
            return min;
        }
        if (value > max) {
            LOGGER.warn("Effect '{}': param '{}' value {} above maximum, using {}",
                    effectName, paramName, value, max);
            return max;
        }
        return value;
    }

    /**
     * Clamp a float value with only a minimum bound, logging a warning if clamping occurs.
     *
     * @param effectName Name of the effect (for logging)
     * @param paramName  Name of the parameter (for logging)
     * @param value      The value to clamp
     * @param min        Minimum allowed value
     * @return Clamped value >= min
     */
    public static float clampMin(String effectName, String paramName, float value, float min) {
        if (value < min) {
            LOGGER.warn("Effect '{}': param '{}' value {} below minimum, using {}",
                    effectName, paramName, value, min);
            return min;
        }
        return value;
    }

    /**
     * Clamp an int value to a range, logging a warning if clamping occurs.
     *
     * @param effectName Name of the effect (for logging)
     * @param paramName  Name of the parameter (for logging)
     * @param value      The value to clamp
     * @param min        Minimum allowed value
     * @param max        Maximum allowed value
     * @return Clamped value within [min, max]
     */
    public static int clamp(String effectName, String paramName, int value, int min, int max) {
        if (value < min) {
            LOGGER.warn("Effect '{}': param '{}' value {} below minimum, using {}",
                    effectName, paramName, value, min);
            return min;
        }
        if (value > max) {
            LOGGER.warn("Effect '{}': param '{}' value {} above maximum, using {}",
                    effectName, paramName, value, max);
            return max;
        }
        return value;
    }

    /**
     * Clamp a double value to a range, logging a warning if clamping occurs.
     *
     * @param effectName Name of the effect (for logging)
     * @param paramName  Name of the parameter (for logging)
     * @param value      The value to clamp
     * @param min        Minimum allowed value
     * @param max        Maximum allowed value
     * @return Clamped value within [min, max]
     */
    public static double clamp(String effectName, String paramName, double value, double min, double max) {
        if (value < min) {
            LOGGER.warn("Effect '{}': param '{}' value {} below minimum, using {}",
                    effectName, paramName, value, min);
            return min;
        }
        if (value > max) {
            LOGGER.warn("Effect '{}': param '{}' value {} above maximum, using {}",
                    effectName, paramName, value, max);
            return max;
        }
        return value;
    }
}
