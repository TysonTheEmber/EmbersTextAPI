package net.tysontheember.emberstextapi.platform;

import java.nio.file.Path;

public interface PlatformHelper {

    static PlatformHelper getInstance() {
        return PlatformHelperImpl.INSTANCE;
    }

    boolean isClient();

    boolean isServer();

    String getModVersion();

    Path getConfigDir();

    String getModId();
}
