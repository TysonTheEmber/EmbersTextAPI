package net.tysontheember.emberstextapi.integrations;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.config.ClientConfig;
import net.tysontheember.emberstextapi.markup.EmberMarkup;
import net.tysontheember.emberstextapi.overlay.Markers;

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
            event.setMessage(component.getString());
        }
    }

    public static void displayIncoming(Component component) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Component display = component;
        if (ClientConfig.STRIP_INSERTION_IN_CHAT.get()) {
            display = Markers.stripInsertions(component);
        }
        player.sendSystemMessage(display);
    }
}
