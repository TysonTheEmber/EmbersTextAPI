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

    @Override
    public int getAnvilNameMaxLength() {
        return net.tysontheember.emberstextapi.config.ModConfig.getAnvilNameMaxLength();
    }

    @Override
    public int getMaxServerMessageDuration() {
        return net.tysontheember.emberstextapi.config.ModConfig.getMaxServerMessageDuration();
    }

    @Override
    public int getMaxServerActiveMessages() {
        return net.tysontheember.emberstextapi.config.ModConfig.getMaxServerActiveMessages();
    }

    @Override
    public int getMaxQueueSize() {
        return net.tysontheember.emberstextapi.config.ModConfig.getMaxQueueSize();
    }

    @Override
    public List<String> getAllowedEffects() {
        return net.tysontheember.emberstextapi.config.ModConfig.getAllowedEffects();
    }

    @Override
    public List<String> getDisallowedMarkupTags() {
        return net.tysontheember.emberstextapi.config.ModConfig.getDisallowedMarkupTags();
    }

    @Override
    public boolean isReduceMotionEnabled() {
        return net.tysontheember.emberstextapi.config.ModConfig.isReduceMotionEnabled();
    }

    @Override
    public int getMaxNeonQuality() {
        return net.tysontheember.emberstextapi.config.ModConfig.getMaxNeonQuality();
    }

    @Override
    public int getTextLayoutCacheSize() {
        return net.tysontheember.emberstextapi.config.ModConfig.getTextLayoutCacheSize();
    }

    @Override
    public boolean isSdfEnabled() {
        return net.tysontheember.emberstextapi.config.ModConfig.isSdfEnabled();
    }
}
