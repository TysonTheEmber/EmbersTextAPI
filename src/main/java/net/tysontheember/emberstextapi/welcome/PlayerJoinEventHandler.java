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
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.api.TextAnchor;
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

        // Message duration in seconds
//        float duration = 8.0f;

//        // Showcase 1: Gradient + Typewriter
//        ImmersiveMessage msg1 = ImmersiveMessage.fromMarkup(
//            duration,
//            "<gradient from=\"#FFaa00\" to=\"#FF5500\"><typewriter speed=\"50\">Gradient + Typewriter Effect</typewriter></gradient>"
//        );
//        msg1.anchor(TextAnchor.TOP_CENTER);
//        msg1.offset(0, 80f);
//        msg1.background(true);
//        msg1.scale(1.3f);
//        EmbersTextAPI.sendMessage(player, msg1);
//
//        // Showcase 2: Shake + Fade
//        ImmersiveMessage msg2 = ImmersiveMessage.fromMarkup(
//            duration,
//            "<fade><shake>Shake + Fade Effect!</shake></fade>"
//        );
//        msg2.anchor(TextAnchor.TOP_CENTER);
//        msg2.offset(0, 110f);
//        msg2.background(true);
//        msg2.scale(1.2f);
//        EmbersTextAPI.sendMessage(player, msg2);
//
//        // Showcase 3: Progressive Obfuscation
//        ImmersiveMessage msg3 = ImmersiveMessage.fromMarkup(
//            duration,
//            "<obfuscate mode=\"progressive\" speed=\"0.05\">Progressive De-obfuscation</obfuscate>"
//        );
//        msg3.anchor(TextAnchor.TOP_CENTER);
//        msg3.offset(0, 135f);
//        msg3.background(true);
//        msg3.scale(1.1f);
//        EmbersTextAPI.sendMessage(player, msg3);
//
//        // Showcase 4: Rainbow Gradient
//        ImmersiveMessage msg4 = ImmersiveMessage.fromMarkup(
//            duration,
//            "<gradient from=\"#FF0000\" via=\"#FFFF00,#00FF00,#00FFFF\" to=\"#FF00FF\">Rainbow Gradient</gradient>"
//        );
//        msg4.anchor(TextAnchor.TOP_CENTER);
//        msg4.offset(0, 160f);
//        msg4.background(true);
//        msg4.scale(1.2f);
//        EmbersTextAPI.sendMessage(player, msg4);
//
//        // Showcase 5: Multiple effects combined
//        ImmersiveMessage msg5 = ImmersiveMessage.fromMarkup(
//            duration,
//            "<gradient from=\"#55FF55\" to=\"#00FF88\"><typewriter speed=\"40\"><shake intensity=\"0.5\">Combine Multiple Effects!</shake></typewriter></gradient>"
//        );
//        msg5.anchor(TextAnchor.TOP_CENTER);
//        msg5.offset(0, 185f);
//        msg5.background(true);
//        msg5.scale(1.3f);
//        EmbersTextAPI.sendMessage(player, msg5);

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
