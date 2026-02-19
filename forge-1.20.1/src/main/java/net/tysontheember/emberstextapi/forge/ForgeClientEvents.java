package net.tysontheember.emberstextapi.forge;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.client.ClientMessageManager;

/**
 * Forge client-side event handler.
 * Bridges Forge events to platform-agnostic ClientMessageManager.
 */
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
                // Explicitly order above vanilla chat so immersive text is never hidden by chat background/glyphs.
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableDepthTest();
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                guiGraphics.setColor(1f, 1f, 1f, 1f);

                // Keep HUD-style rendering and alpha behavior while guaranteeing front-most GUI depth.
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0f, 0f, 200f);
                ClientMessageManager.render(guiGraphics, partialTick);
                guiGraphics.pose().popPose();
                guiGraphics.setColor(1f, 1f, 1f, 1f);
            });
        }
    }
}
