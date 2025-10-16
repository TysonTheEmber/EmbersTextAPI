package net.tysontheember.emberstextapi.integrations;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.markup.EmberMarkup;

/**
 * Simple client chat interception that runs incoming/outgoing strings through
 * the markup parser.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "emberstextapi")
public final class ChatInterceptor {
    private ChatInterceptor() {
    }

    @SubscribeEvent
    public static void onSend(ClientChatEvent event) {
        String message = event.getMessage();
        if (message.contains("<")) {
            Component component = EmberMarkup.toComponent(message);
            event.setComponent(component);
        }
    }

    public static void displayIncoming(Component component) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.sendSystemMessage(component);
    }
}
