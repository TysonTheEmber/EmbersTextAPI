package net.tysontheember.emberstextapi.integrations;

import net.minecraft.network.chat.Component;
import net.tysontheember.emberstextapi.markup.EmberMarkup;

/**
 * Utility hooks for chat integration.
 */
public final class ChatInterceptor {
    private ChatInterceptor() {
    }

    public static Component parseOutgoing(String message) {
        return EmberMarkup.toComponent(message);
    }
}
