package net.tysontheember.emberstextapi.immersivemessages.effects.params;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ValidationHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationHelper.class);

    private ValidationHelper() {

    }

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

    public static float clampMin(String effectName, String paramName, float value, float min) {
        if (value < min) {
            LOGGER.warn("Effect '{}': param '{}' value {} below minimum, using {}",
                    effectName, paramName, value, min);
            return min;
        }
        return value;
    }

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
