package net.tysontheember.emberstextapi.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import net.tysontheember.emberstextapi.platform.PlatformHelper;

import java.nio.file.Path;

/**
 * Forge implementation of PlatformHelper.
 */
public class ForgePlatformHelper implements PlatformHelper {
    @Override
    public boolean isClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }

    @Override
    public boolean isServer() {
        return FMLEnvironment.dist == Dist.DEDICATED_SERVER;
    }

    @Override
    public String getModVersion() {
        return ModList.get().getModFileById("emberstextapi")
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
