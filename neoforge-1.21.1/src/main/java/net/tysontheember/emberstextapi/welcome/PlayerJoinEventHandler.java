package net.tysontheember.emberstextapi.welcome;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.tysontheember.emberstextapi.config.ModConfig;
import org.slf4j.Logger;

/**
 * Handles player join events to display an info message on first join.
 * NeoForge port of Forge PlayerJoinEventHandler.
 *
 * Tracks which players have already received the info message using
 * persistent player data (NBT). The message informs players that
 * EmbersTextAPI is installed and provides usage information.
 */
@EventBusSubscriber(modid = "emberstextapi", bus = EventBusSubscriber.Bus.GAME)
public class PlayerJoinEventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String NBT_KEY = "emberstextapi_welcomed";

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Check if welcome message is enabled
        if (!ModConfig.isWelcomeMessageEnabled()) {
            return;
        }

        // Check if player has already been welcomed
        CompoundTag persistentData = player.getPersistentData();
        if (persistentData.getBoolean(NBT_KEY)) {
            return;
        }

        // Mark player as welcomed
        persistentData.putBoolean(NBT_KEY, true);

        // Send info message with a slight delay to ensure client is ready
        player.getServer().execute(() -> {
            try {
                Thread.sleep(1000);
                sendInfoMessage(player);
            } catch (InterruptedException e) {
                LOGGER.error("Info message delay interrupted", e);
                Thread.currentThread().interrupt();
            }
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
