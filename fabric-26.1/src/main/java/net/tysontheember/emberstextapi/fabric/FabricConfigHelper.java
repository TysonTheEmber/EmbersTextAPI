package net.tysontheember.emberstextapi.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import net.tysontheember.emberstextapi.platform.PlatformHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FabricConfigHelper implements ConfigHelper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "emberstextapi.json";

    private Config config;

    @Override
    public void register() {
        Path configPath = PlatformHelper.getInstance().getConfigDir().resolve(CONFIG_FILE);

        try {
            if (Files.exists(configPath)) {
                String json = Files.readString(configPath);
                config = GSON.fromJson(json, Config.class);
                if (config == null) {
                    config = new Config();
                }

                save();
            } else {
                config = new Config();
                String json = GSON.toJson(config);
                Files.createDirectories(configPath.getParent());
                Files.writeString(configPath, json);
            }
        } catch (IOException e) {
            EmbersTextAPIFabric.LOGGER.error("Failed to load config, using defaults", e);
            config = new Config();
        }
    }

    @Override
    public boolean isImmersiveMessagesEnabled() {
        return config != null && config.immersiveMessagesEnabled;
    }

    @Override
    public List<String> getDisabledEffects() {
        return config != null && config.disabledEffects != null ? config.disabledEffects : new ArrayList<>();
    }

    @Override
    public String getMarkupPermissionMode() {
        return config != null && config.markupPermissionMode != null ? config.markupPermissionMode : "NONE";
    }

    @Override
    public List<String> getMarkupPlayerList() {
        return config != null && config.markupPlayerList != null ? config.markupPlayerList : new ArrayList<>();
    }

    @Override
    public int getMaxMessageDuration() {
        return config != null ? config.maxMessageDuration : 0;
    }

    @Override
    public int getMaxActiveMessages() {
        return config != null ? config.maxActiveMessages : 0;
    }

    @Override
    public int getAnvilNameMaxLength() {
        return config != null ? config.anvilNameMaxLength : 50;
    }

    private void save() {
        Path configPath = PlatformHelper.getInstance().getConfigDir().resolve(CONFIG_FILE);
        try {
            String json = GSON.toJson(config);
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, json);
        } catch (IOException e) {
            EmbersTextAPIFabric.LOGGER.error("Failed to save config", e);
        }
    }

    @Override
    public int getMaxServerMessageDuration() {
        return config != null ? config.maxServerMessageDuration : 1200;
    }

    @Override
    public int getMaxServerActiveMessages() {
        return config != null ? config.maxServerActiveMessages : 10;
    }

    @Override
    public int getMaxQueueSize() {
        return config != null ? config.maxQueueSize : 50;
    }

    @Override
    public List<String> getAllowedEffects() {
        return config != null && config.allowedEffects != null ? config.allowedEffects : new ArrayList<>();
    }

    @Override
    public List<String> getDisallowedMarkupTags() {
        return config != null && config.disallowedMarkupTags != null ? config.disallowedMarkupTags : new ArrayList<>();
    }

    @Override
    public boolean isReduceMotionEnabled() {
        return config != null && config.reduceMotion;
    }

    @Override
    public int getMaxNeonQuality() {
        return config != null ? config.maxNeonQuality : 3;
    }

    @Override
    public int getTextLayoutCacheSize() {
        return config != null ? config.textLayoutCacheSize : 256;
    }

    @Override
    public boolean isSdfEnabled() {
        return config == null || config.sdfEnabled;
    }

    private static class Config {
        public boolean immersiveMessagesEnabled = true;
        public List<String> disabledEffects = new ArrayList<>();
        public String markupPermissionMode = "NONE";
        public List<String> markupPlayerList = new ArrayList<>();
        public int maxMessageDuration = 0;
        public int maxActiveMessages = 0;
        public int anvilNameMaxLength = 50;

        public int maxServerMessageDuration = 1200;
        public int maxServerActiveMessages = 10;
        public int maxQueueSize = 50;
        public List<String> allowedEffects = new ArrayList<>();

        public List<String> disallowedMarkupTags = new ArrayList<>();

        public boolean reduceMotion = false;
        public int maxNeonQuality = 3;

        public int textLayoutCacheSize = 256;
        public boolean sdfEnabled = true;
    }
}
