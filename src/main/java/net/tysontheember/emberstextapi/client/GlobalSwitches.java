package net.tysontheember.emberstextapi.client;

/**
 * Lightweight accessors for frequently queried client configuration flags.
 */
public final class GlobalSwitches {
    private GlobalSwitches() {
    }

    public static boolean enabled() {
        return ETAClientConfig.ENABLE_GLOBAL_SPANS.get();
    }

    public static boolean typewriterEnabled() {
        return ETAClientConfig.ENABLE_TYPEWRITER.get();
    }

    public static void setGlobalSpansEnabled(boolean enabled) {
        ETAClientConfig.ENABLE_GLOBAL_SPANS.set(enabled);
        ETAClientConfig.save();
    }

    public static void setTypewriterEnabled(boolean enabled) {
        ETAClientConfig.ENABLE_TYPEWRITER.set(enabled);
        ETAClientConfig.save();
    }
}
