package net.tysontheember.emberstextapi.fabric;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.level.ServerPlayer;
import net.tysontheember.emberstextapi.commands.FabricCommands;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
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

        // Register player join handler for welcome messages
        FabricPlayerJoinHandler.register();

        LOGGER.info("EmbersTextAPI initialization complete");
    }

    /**
     * Convenience method for sending an immersive message to a player.
     * Wraps NetworkHelper for API consistency with Forge's EmbersTextAPI.sendMessage().
     */
    public static void sendMessage(ServerPlayer player, ImmersiveMessage message) {
        NetworkHelper.getInstance().sendMessage(player, message);
    }
}
