package net.tysontheember.emberstextapi.chat;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import net.tysontheember.emberstextapi.util.MarkupStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Strips markup tags from chat messages for players who are not allowed to use markup.
 */
@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID)
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
            return;
        }

        // Strip disallowed tags (even for players who can use markup)
        List<String> disallowed = ConfigHelper.getInstance().getDisallowedMarkupTags();
        if (!disallowed.isEmpty()) {
            String filtered = MarkupStripper.stripTags(message, disallowed);
            if (!filtered.equals(message)) {
                LOGGER.debug("Stripped disallowed tags from chat message by {}", player.getName().getString());
                event.setMessage(Component.literal(filtered));
            }
        }
    }
}
