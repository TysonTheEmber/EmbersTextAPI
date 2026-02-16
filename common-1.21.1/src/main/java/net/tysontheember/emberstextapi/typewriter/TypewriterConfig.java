package net.tysontheember.emberstextapi.typewriter;

/**
 * Global configuration for the typewriter effect system.
 * <p>
 * Provides a global toggle to enable/disable all typewriter effects
 * and a default speed setting used when no speed parameter is specified.
 * </p>
 *
 * @deprecated This class is part of the legacy typewriter API.
 *             Use {@link net.tysontheember.emberstextapi.immersivemessages.effects.animation.TypewriterEffect} instead.
 *             This class will be removed in version 3.0.0.
 */
@Deprecated(forRemoval = true, since = "2.0.0")
public class TypewriterConfig {

    /** Global toggle for typewriter effects. When false, all text is shown immediately. */
    private static volatile boolean enabled = true;

    /** Default milliseconds per character when no speed parameter is specified. */
    private static volatile int defaultSpeedMs = 20;

    /**
     * Default max play count. -1 = infinite, 1 = play once, N = play N times.
     * This can be overridden per-effect with the repeat parameter.
     */
    private static volatile int defaultMaxPlays = -1;

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

    /**
     * Get the default max play count.
     *
     * @return -1 for infinite, or a positive number for max plays
     */
    public static int getDefaultMaxPlays() {
        return defaultMaxPlays;
    }

    /**
     * Set the default max play count for typewriter effects.
     * <p>
     * This value is used when a {@code <typewriter>} tag doesn't specify
     * a repeat parameter.
     * </p>
     * <ul>
     *   <li>-1 = infinite repeats (animation restarts when text reappears)</li>
     *   <li>1 = play once (stays revealed after completion)</li>
     *   <li>N = play N times before staying revealed</li>
     * </ul>
     *
     * @param maxPlays -1 for infinite, or a positive number
     */
    public static void setDefaultMaxPlays(int maxPlays) {
        defaultMaxPlays = maxPlays < 0 ? -1 : Math.max(1, maxPlays);
    }
}
