package net.tysontheember.emberstextapi.welcome;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Handles player join events to display an info message on first join.
 * Fabric port of Forge PlayerJoinEventHandler.
 *
 * Uses in-memory tracking (resets on server restart) since Fabric lacks
 * Forge's Entity.getPersistentData(). Shows once per server session.
 */
public class FabricPlayerJoinHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/Welcome");
    private static final Set<UUID> welcomedPlayers = new HashSet<>();

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();

            // Check if welcome message is enabled
            if (!ConfigHelper.getInstance().isWelcomeMessageEnabled()) {
                return;
            }

            // Check if player has already been welcomed this session
            if (!welcomedPlayers.add(player.getUUID())) {
                return;
            }

            // Send info message with a slight delay to ensure client is ready
            server.execute(() -> {
                try {
                    Thread.sleep(1000);
                    sendInfoMessage(player);
                } catch (InterruptedException e) {
                    LOGGER.error("Info message delay interrupted", e);
                    Thread.currentThread().interrupt();
                }
            });
        });
    }

    private static void sendInfoMessage(ServerPlayer player) {
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("[EmbersTextAPI] ")
            .withStyle(style -> style.withColor(0xFFAA00).withBold(true))
            .append(Component.literal("A powerful text effects API for Minecraft")
                .withStyle(style -> style.withColor(0xAAAAAA).withBold(false))));
        player.sendSystemMessage(Component.literal("Adds immersive, animated text to tooltips, GUIs, and more!")
            .withStyle(style -> style.withColor(0xCCCCCC)));
        player.sendSystemMessage(Component.literal(""));

        player.sendSystemMessage(Component.literal("Type ")
            .withStyle(style -> style.withColor(0xAAAAAA))
            .append(Component.literal("/eta help")
                .withStyle(style -> style.withColor(0x55FF55).withBold(true)))
            .append(Component.literal(" for more info and useful links")
                .withStyle(style -> style.withColor(0xAAAAAA))));
        player.sendSystemMessage(Component.literal(""));

        LOGGER.info("EmbersTextAPI info message sent to player: {}", player.getName().getString());
    }
}
