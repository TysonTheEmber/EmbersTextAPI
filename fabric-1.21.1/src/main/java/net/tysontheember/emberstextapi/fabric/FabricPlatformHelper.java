package net.tysontheember.emberstextapi.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.tysontheember.emberstextapi.platform.PlatformHelper;

import java.nio.file.Path;

/**
 * Fabric implementation of PlatformHelper.
 */
public class FabricPlatformHelper implements PlatformHelper {
    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public boolean isServer() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
    }

    @Override
    public String getModVersion() {
        return FabricLoader.getInstance()
            .getModContainer("emberstextapi")
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public String getModId() {
        return "emberstextapi";
    }
}
