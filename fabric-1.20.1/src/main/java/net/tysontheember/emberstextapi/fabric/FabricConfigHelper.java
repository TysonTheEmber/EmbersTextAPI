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

/**
 * Fabric implementation of ConfigHelper.
 * Uses simple JSON file for configuration.
 */
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
                // Re-save to pick up any new fields with defaults
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
    public boolean isWelcomeMessageEnabled() {
        return config != null && config.welcomeMessageEnabled;
    }

    public void setWelcomeMessageEnabled(boolean enabled) {
        if (config == null) {
            config = new Config();
        }
        config.welcomeMessageEnabled = enabled;
        save();
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

    /**
     * Config data class.
     */
    private static class Config {
        public boolean welcomeMessageEnabled = true;
        public boolean immersiveMessagesEnabled = true;
        public List<String> disabledEffects = new ArrayList<>();
        public String markupPermissionMode = "NONE";
        public List<String> markupPlayerList = new ArrayList<>();
        public int maxMessageDuration = 0;
        public int maxActiveMessages = 0;
    }
}
