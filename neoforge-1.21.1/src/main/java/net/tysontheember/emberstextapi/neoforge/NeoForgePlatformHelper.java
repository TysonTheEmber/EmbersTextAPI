package net.tysontheember.emberstextapi.neoforge;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.tysontheember.emberstextapi.platform.PlatformHelper;

import java.nio.file.Path;

public class NeoForgePlatformHelper implements PlatformHelper {

    @Override
    public boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    public boolean isServer() {
        return FMLEnvironment.dist.isDedicatedServer();
    }

    @Override
    public String getModVersion() {
        return ModList.get()
            .getModFileById("emberstextapi")
            .versionString();
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public String getModId() {
        return "emberstextapi";
    }
}
