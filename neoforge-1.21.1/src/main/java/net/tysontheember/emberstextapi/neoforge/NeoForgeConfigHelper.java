package net.tysontheember.emberstextapi.neoforge;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.tysontheember.emberstextapi.platform.ConfigHelper;

public class NeoForgeConfigHelper implements ConfigHelper {

    @Override
    public void register() {
        ModLoadingContext.get().getActiveContainer().registerConfig(
            ModConfig.Type.COMMON,
            net.tysontheember.emberstextapi.config.ModConfig.SPEC,
            "emberstextapi-common.toml"
        );
    }

    @Override
    public boolean isWelcomeMessageEnabled() {
        return net.tysontheember.emberstextapi.config.ModConfig.isWelcomeMessageEnabled();
    }
}
