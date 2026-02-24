package net.tysontheember.emberstextapi.neoforge;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.tysontheember.emberstextapi.platform.ConfigHelper;

import java.util.List;

public class NeoForgeConfigHelper implements ConfigHelper {

    @Override
    public void register() {
        ModLoadingContext.get().getActiveContainer().registerConfig(
            ModConfig.Type.COMMON,
            net.tysontheember.emberstextapi.config.ModConfig.COMMON_SPEC,
            "emberstextapi-common.toml"
        );
        ModLoadingContext.get().getActiveContainer().registerConfig(
            ModConfig.Type.CLIENT,
            net.tysontheember.emberstextapi.config.ModConfig.CLIENT_SPEC,
            "emberstextapi-client.toml"
        );
    }

    @Override
    public boolean isWelcomeMessageEnabled() {
        return net.tysontheember.emberstextapi.config.ModConfig.isWelcomeMessageEnabled();
    }

    @Override
    public boolean isImmersiveMessagesEnabled() {
        return net.tysontheember.emberstextapi.config.ModConfig.isImmersiveMessagesEnabled();
    }

    @Override
    public List<String> getDisabledEffects() {
        return net.tysontheember.emberstextapi.config.ModConfig.getDisabledEffects();
    }

    @Override
    public String getMarkupPermissionMode() {
        return net.tysontheember.emberstextapi.config.ModConfig.getMarkupPermissionMode();
    }

    @Override
    public List<String> getMarkupPlayerList() {
        return net.tysontheember.emberstextapi.config.ModConfig.getMarkupPlayerList();
    }

    @Override
    public int getMaxMessageDuration() {
        return net.tysontheember.emberstextapi.config.ModConfig.getMaxMessageDuration();
    }

    @Override
    public int getMaxActiveMessages() {
        return net.tysontheember.emberstextapi.config.ModConfig.getMaxActiveMessages();
    }
}
