package net.tysontheember.emberstextapi.inline;

/**
 * Runtime configuration for inline tag parsing.
 */
public final class InlineConfig {
    private static boolean enabled = true;
    private static boolean logUnknown = false;
    private static int maxDepth = 16;
    private static int maxLength = 16_384;

    private InlineConfig() {
    }

    public static boolean enabled() {
        return enabled;
    }

    public static boolean logUnknown() {
        return logUnknown;
    }

    public static int maxDepth() {
        return maxDepth;
    }

    public static int maxLength() {
        return maxLength;
    }

    public static void apply(boolean enabled, boolean logUnknown, int maxDepth, int maxLength) {
        InlineConfig.enabled = enabled;
        InlineConfig.logUnknown = logUnknown;
        InlineConfig.maxDepth = Math.max(1, maxDepth);
        InlineConfig.maxLength = Math.max(1, maxLength);
    }
}
