package net.tysontheember.emberstextapi.typewriter;

/**
 * Global configuration for the typewriter effect system.
 * <p>
 * Provides a global toggle to enable/disable all typewriter effects
 * and a default speed setting used when no speed parameter is specified.
 * </p>
 */
public class TypewriterConfig {

    /** Global toggle for typewriter effects. When false, all text is shown immediately. */
    private static boolean enabled = true;

    /** Default milliseconds per character when no speed parameter is specified. */
    private static int defaultSpeedMs = 20;

    /**
     * Check if typewriter effects are globally enabled.
     *
     * @return true if typewriter effects should animate, false to show all text immediately
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable all typewriter effects globally.
     * <p>
     * When disabled, all text wrapped in {@code <typewriter>} tags is shown immediately
     * without animation.
     * </p>
     *
     * @param value true to enable animations, false to disable
     */
    public static void setEnabled(boolean value) {
        enabled = value;
    }

    /**
     * Get the default speed in milliseconds per character.
     *
     * @return milliseconds between each character reveal
     */
    public static int getDefaultSpeedMs() {
        return defaultSpeedMs;
    }

    /**
     * Set the default speed for typewriter effects.
     * <p>
     * This value is used when a {@code <typewriter>} tag doesn't specify
     * a speed parameter.
     * </p>
     *
     * @param ms milliseconds per character (minimum 1)
     */
    public static void setDefaultSpeedMs(int ms) {
        defaultSpeedMs = Math.max(1, ms);
    }
}
