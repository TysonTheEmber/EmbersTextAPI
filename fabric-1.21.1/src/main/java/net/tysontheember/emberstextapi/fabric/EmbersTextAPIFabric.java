package net.tysontheember.emberstextapi.fabric;

import net.fabricmc.api.ModInitializer;
import net.tysontheember.emberstextapi.commands.FabricCommands;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import net.tysontheember.emberstextapi.platform.NetworkHelper;
import net.tysontheember.emberstextapi.welcome.FabricPlayerJoinHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main mod initializer for Fabric.
 * Handles common setup and server-side initialization.
 */
public class EmbersTextAPIFabric implements ModInitializer {
    public static final String MODID = "emberstextapi";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing EmbersTextAPI for Fabric");

        // Register config
        ConfigHelper.getInstance().register();

        // Register networking
        NetworkHelper.getInstance().register();

        // Register commands
        FabricCommands.register();

        // Register welcome message handler
        FabricPlayerJoinHandler.register();

        LOGGER.info("EmbersTextAPI initialization complete");
    }
}
