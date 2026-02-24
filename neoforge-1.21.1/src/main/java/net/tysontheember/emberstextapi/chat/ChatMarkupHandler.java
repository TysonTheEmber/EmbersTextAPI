package net.tysontheember.emberstextapi.chat;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import net.tysontheember.emberstextapi.util.MarkupStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Strips markup tags from chat messages for players who are not allowed to use markup.
 */
@EventBusSubscriber(modid = "emberstextapi")
public class ChatMarkupHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/Chat");

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getMessage().getString();

        if (!MarkupStripper.containsMarkup(message)) {
            return;
        }

        if (!ConfigHelper.getInstance().isPlayerAllowedMarkup(player.getUUID())) {
            String stripped = MarkupStripper.stripMarkup(message);
            LOGGER.debug("Stripped markup from chat message by {} (UUID: {})", player.getName().getString(), player.getUUID());
            event.setMessage(Component.literal(stripped));
        }
    }
}
