package net.tysontheember.emberstextapi.integrations;

import net.minecraft.network.chat.Component;
import net.tysontheember.emberstextapi.markup.EmberMarkup;
import net.tysontheember.emberstextapi.markup.RNode;

/**
 * Helper used by the mod's chat mixins (installed elsewhere) to parse markup.
 */
public final class ChatInterceptor {
    private ChatInterceptor() {
    }

    public static Component parseOutgoing(String message) {
        RNode node = EmberMarkup.parse(message);
        return EmberMarkup.toComponent(node);
    }
}
