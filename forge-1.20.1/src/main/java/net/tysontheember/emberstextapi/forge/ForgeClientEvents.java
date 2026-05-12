package net.tysontheember.emberstextapi.forge;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.client.ClientMessageManager;

@Mod.EventBusSubscriber(modid = "emberstextapi", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeClientEvents {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientMessageManager.tick(Minecraft.getInstance());
    }

    @Mod.EventBusSubscriber(modid = "emberstextapi", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ForgeClientOverlays {
        @SubscribeEvent
        public static void register(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.CHAT_PANEL.id(), "immersive_messages", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0f, 0f, 200f);
                ClientMessageManager.render(guiGraphics, partialTick);
                guiGraphics.pose().popPose();
            });
        }
    }
}
