package net.tysontheember.emberstextapi.platform;

import java.nio.file.Path;

/**
 * Platform abstraction for loader-specific functionality.
 * Implementations exist in each loader module.
 */
public interface PlatformHelper {
    /**
     * Get the singleton instance.
     * Implementation loaded via ServiceLoader.
     */
    static PlatformHelper getInstance() {
        return PlatformHelperImpl.INSTANCE;
    }

    /**
     * Check if the current side is client.
     */
    boolean isClient();

    /**
     * Check if the current side is server.
     */
    boolean isServer();

    /**
     * Get the mod version string.
     */
    String getModVersion();

    /**
     * Get the config directory path.
     */
    Path getConfigDir();

    /**
     * Get the mod ID.
     */
    String getModId();
}
