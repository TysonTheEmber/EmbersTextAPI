package net.tysontheember.emberstextapi.client.text;

import net.tysontheember.emberstextapi.client.text.options.ETAOptions;

/**
 * Simple toggle to gate global markup parsing hooks.
 */
public final class GlobalTextConfig {
    private static volatile boolean markupEnabled = true;
    private static volatile boolean typewriterGatingEnabled = true;
    private static volatile ETAOptions.Snapshot options = ETAOptions.Snapshot.DEFAULT;
    private static volatile ETAOptions clientOptions;

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

    public static ETAOptions.Snapshot getOptions() {
        return options;
    }

    public static void setOptions(ETAOptions.Snapshot newOptions) {
        options = newOptions != null ? newOptions : ETAOptions.Snapshot.DEFAULT;
    }

    public static ETAOptions getClientOptions() {
        return clientOptions;
    }

    public static void setClientOptions(ETAOptions optionsInstance) {
        clientOptions = optionsInstance;
    }
}
