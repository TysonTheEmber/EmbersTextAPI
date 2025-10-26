package net.tysontheember.emberstextapi.client;

/**
 * Lightweight accessors for frequently queried client configuration flags.
 */
public final class GlobalSwitches {
    private GlobalSwitches() {
    }

    public static boolean enabled() {
        return ETAClientConfig.globalSpansEnabled();
    }

    public static boolean typewriterEnabled() {
        return ETAClientConfig.typewriterEnabled();
    }

    public static void setGlobalSpansEnabled(boolean enabled) {
        ETAClientConfig.setCachedGlobalSpans(enabled);
        if (ETAClientConfig.isLoaded()) {
            ETAClientConfig.ENABLE_GLOBAL_SPANS.set(enabled);
            ETAClientConfig.save();
        }
    }

    public static void setTypewriterEnabled(boolean enabled) {
        ETAClientConfig.setCachedTypewriter(enabled);
        if (ETAClientConfig.isLoaded()) {
            ETAClientConfig.ENABLE_TYPEWRITER.set(enabled);
            ETAClientConfig.save();
        }
    }
}
