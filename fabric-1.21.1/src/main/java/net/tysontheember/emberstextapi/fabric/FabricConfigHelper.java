package net.tysontheember.emberstextapi.fabric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import net.tysontheember.emberstextapi.platform.PlatformHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
                // Load existing config
                String json = Files.readString(configPath);
                config = GSON.fromJson(json, Config.class);
            } else {
                // Create default config
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

    /**
     * Sets whether the welcome message is enabled and saves the config to disk.
     */
    public void setWelcomeMessageEnabled(boolean enabled) {
        if (config == null) {
            config = new Config();
        }
        config.welcomeMessageEnabled = enabled;
        save();
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
    }
}
