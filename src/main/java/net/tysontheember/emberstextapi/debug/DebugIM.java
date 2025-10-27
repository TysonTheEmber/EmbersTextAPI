package net.tysontheember.emberstextapi.debug;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;

/**
 * Helpers for invoking the Immersive Message overlay in debug flows.
 */
public final class DebugIM {
    private static final float DEFAULT_DURATION = 120f;

    private DebugIM() {
    }

    public static void render(ServerPlayer player, Component component) {
        if (player == null || component == null) {
            return;
        }

        String markup = component.getString();
        if (markup.isEmpty()) {
            return;
        }

        try {
            ImmersiveMessage message = ImmersiveMessage.fromMarkup(DEFAULT_DURATION, markup);
            EmbersTextAPI.sendMessage(player, message);
        } catch (Exception ex) {
            player.sendSystemMessage(Component.literal("Failed to render Immersive Message: " + ex.getMessage()));
        }
    }
}
