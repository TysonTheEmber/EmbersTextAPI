package net.tysontheember.emberstextapi.platform;

/**
 * Platform abstraction for configuration.
 */
public interface ConfigHelper {
    /**
     * Get the singleton instance.
     */
    static ConfigHelper getInstance() {
        return ConfigHelperImpl.INSTANCE;
    }

    /**
     * Register the mod configuration.
     * Called during mod initialization.
     */
    void register();

    /**
     * Check if the welcome message is enabled.
     */
    boolean isWelcomeMessageEnabled();
}
