package net.tysontheember.emberstextapi.platform;

import java.util.ServiceLoader;

final class ConfigHelperImpl {
    static final ConfigHelper INSTANCE;

    static {
        ConfigHelper instance = ServiceLoader.load(ConfigHelper.class)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No ConfigHelper implementation found! Ensure your loader module provides one."));
        INSTANCE = instance;
    }

    private ConfigHelperImpl() {
    }
}
