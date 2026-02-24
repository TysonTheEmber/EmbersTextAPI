package net.tysontheember.emberstextapi.chat;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import net.tysontheember.emberstextapi.util.MarkupStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strips markup tags from chat messages for players who are not allowed to use markup.
 * Uses Fabric's ServerMessageEvents to intercept chat messages.
 */
public class FabricChatMarkupHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/Chat");

    public static void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            String content = message.signedContent();

            if (!MarkupStripper.containsMarkup(content)) {
                return true;
            }

            if (!ConfigHelper.getInstance().isPlayerAllowedMarkup(sender.getUUID())) {
                // Cancel the signed message and send a plain system message instead
                String stripped = MarkupStripper.stripMarkup(content);
                LOGGER.debug("Stripped markup from chat message by {} (UUID: {})", sender.getName().getString(), sender.getUUID());

                // Broadcast the stripped message as a system message to all players
                Component strippedComponent = Component.literal("<" + sender.getName().getString() + "> " + stripped);
                sender.server.getPlayerList().broadcastSystemMessage(strippedComponent, false);
                return false; // Cancel original message
            }

            return true;
        });
    }
}
