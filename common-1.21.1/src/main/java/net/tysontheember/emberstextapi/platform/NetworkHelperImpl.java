package net.tysontheember.emberstextapi.platform;

import java.util.ServiceLoader;

final class NetworkHelperImpl {
    static final NetworkHelper INSTANCE;

    static {
        NetworkHelper instance = ServiceLoader.load(NetworkHelper.class)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No NetworkHelper implementation found! Ensure your loader module provides one."));
        INSTANCE = instance;
    }

    private NetworkHelperImpl() {
    }
}
