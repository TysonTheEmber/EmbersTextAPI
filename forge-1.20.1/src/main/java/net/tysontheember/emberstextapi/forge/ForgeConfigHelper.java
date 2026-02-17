package net.tysontheember.emberstextapi.forge;

import net.tysontheember.emberstextapi.config.ModConfig;
import net.tysontheember.emberstextapi.platform.ConfigHelper;

/**
 * Forge implementation of ConfigHelper.
 */
public class ForgeConfigHelper implements ConfigHelper {
    @Override
    public void register() {
        ModConfig.register();
    }

    @Override
    public boolean isWelcomeMessageEnabled() {
        return ModConfig.isWelcomeMessageEnabled();
    }
}
