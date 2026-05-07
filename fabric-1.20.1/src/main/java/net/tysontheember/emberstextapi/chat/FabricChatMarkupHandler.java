package net.tysontheember.emberstextapi.chat;

import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import net.tysontheember.emberstextapi.util.MarkupStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FabricChatMarkupHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/Chat");

    public static void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            String content = message.signedContent();

            if (!MarkupStripper.containsMarkup(content)) {
                return true;
            }

            if (!ConfigHelper.getInstance().isPlayerAllowedMarkup(sender.getUUID())) {

                String stripped = MarkupStripper.stripMarkup(content);
                LOGGER.debug("Stripped markup from chat message by {} (UUID: {})", sender.getName().getString(), sender.getUUID());

                Component strippedComponent = Component.literal("<" + sender.getName().getString() + "> " + stripped);
                sender.server.getPlayerList().broadcastSystemMessage(strippedComponent, false);
                return false;
            }

            List<String> disallowed = ConfigHelper.getInstance().getDisallowedMarkupTags();
            if (!disallowed.isEmpty()) {
                String filtered = MarkupStripper.stripTags(content, disallowed);
                if (!filtered.equals(content)) {
                    LOGGER.debug("Stripped disallowed tags from chat message by {}", sender.getName().getString());
                    Component filteredComponent = Component.literal("<" + sender.getName().getString() + "> " + filtered);
                    sender.server.getPlayerList().broadcastSystemMessage(filteredComponent, false);
                    return false;
                }
            }

            return true;
        });
    }
}
