package net.tysontheember.emberstextapi.client.text;

/**
 * Simple toggle to gate global markup parsing hooks.
 */
public final class GlobalTextConfig {
    private static volatile boolean markupEnabled = true;

    private GlobalTextConfig() {
    }

    public static boolean isMarkupEnabled() {
        return markupEnabled;
    }

    public static void setMarkupEnabled(boolean enabled) {
        markupEnabled = enabled;
    }
}
