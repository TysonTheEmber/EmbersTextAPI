package net.tysontheember.emberstextapi.platform;

import java.util.ServiceLoader;

/**
 * Internal helper to load PlatformHelper implementation via ServiceLoader.
 */
final class PlatformHelperImpl {
    static final PlatformHelper INSTANCE;

    static {
        PlatformHelper instance = ServiceLoader.load(PlatformHelper.class)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No PlatformHelper implementation found! Ensure your loader module provides one."));
        INSTANCE = instance;
    }

    private PlatformHelperImpl() {
    }
}
