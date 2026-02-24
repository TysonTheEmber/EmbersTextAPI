package net.tysontheember.emberstextapi.forge;

import net.tysontheember.emberstextapi.config.ModConfig;
import net.tysontheember.emberstextapi.platform.ConfigHelper;

import java.util.List;

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

    @Override
    public boolean isImmersiveMessagesEnabled() {
        return ModConfig.isImmersiveMessagesEnabled();
    }

    @Override
    public List<String> getDisabledEffects() {
        return ModConfig.getDisabledEffects();
    }

    @Override
    public String getMarkupPermissionMode() {
        return ModConfig.getMarkupPermissionMode();
    }

    @Override
    public List<String> getMarkupPlayerList() {
        return ModConfig.getMarkupPlayerList();
    }

    @Override
    public int getMaxMessageDuration() {
        return ModConfig.getMaxMessageDuration();
    }

    @Override
    public int getMaxActiveMessages() {
        return ModConfig.getMaxActiveMessages();
    }
}
