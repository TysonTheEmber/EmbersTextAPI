package net.tysontheember.emberstextapi.welcome;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.config.ModConfig;
import org.slf4j.Logger;

/**
 * Handles player join events to display an info message on first join.
 * <p>
 * Tracks which players have already received the info message using
 * persistent player data (NBT). The message informs players that
 * EmbersTextAPI is installed and provides usage information.
 * </p>
 */
@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID)
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
            return; // Player has already received the info message
        }

        // Mark player as welcomed
        persistentData.putBoolean(NBT_KEY, true);

        // Send info message with a slight delay to ensure client is ready
        player.getServer().execute(() -> {
            try {
                Thread.sleep(1000); // 1 second delay
                sendInfoMessage(player);
            } catch (InterruptedException e) {
                LOGGER.error("Info message delay interrupted", e);
                Thread.currentThread().interrupt();
            }
        });
    }

    /**
     * Sends showcase messages to demonstrate the mod's effects.
     */
    private static void sendInfoMessage(ServerPlayer player) {
        // Send basic chat info first
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("[EmbersTextAPI] ")
            .withStyle(style -> style.withColor(0xFFAA00).withBold(true))
            .append(Component.literal("A powerful text effects API for Minecraft")
                .withStyle(style -> style.withColor(0xAAAAAA).withBold(false))));
        player.sendSystemMessage(Component.literal("Adds immersive, animated text to tooltips, GUIs, and more!")
            .withStyle(style -> style.withColor(0xCCCCCC)));
        player.sendSystemMessage(Component.literal(""));

        // Send command info in chat
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
