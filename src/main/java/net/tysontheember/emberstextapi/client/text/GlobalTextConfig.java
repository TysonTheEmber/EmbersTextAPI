package net.tysontheember.emberstextapi.client.text;

/**
 * Simple toggle to gate global markup parsing hooks.
 */
public final class GlobalTextConfig {
    private static volatile boolean markupEnabled = true;
    private static volatile boolean typewriterGatingEnabled = true;
    private static volatile ETAOptions options = ETAOptions.DEFAULT;

    private GlobalTextConfig() {
    }

    public static boolean isMarkupEnabled() {
        return markupEnabled;
    }

    public static void setMarkupEnabled(boolean enabled) {
        markupEnabled = enabled;
    }

    public static boolean isTypewriterGatingEnabled() {
        return typewriterGatingEnabled;
    }

    public static void setTypewriterGatingEnabled(boolean enabled) {
        typewriterGatingEnabled = enabled;
    }

    public static ETAOptions getOptions() {
        return options;
    }

    public static void setOptions(ETAOptions newOptions) {
        options = newOptions != null ? newOptions : ETAOptions.DEFAULT;
    }
}
