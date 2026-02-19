package net.tysontheember.emberstextapi.immersivemessages;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.EmbersTextAPI;

/**
 * Hooks client events to draw messages every frame.
 */
@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.isPaused()) return;

        ImmersiveMessagesManager.tick();
    }

    @Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class LegacyOverlayRegistration {
        @SubscribeEvent
        public static void register(RegisterGuiOverlaysEvent event) {
            event.registerAbove(VanillaGuiOverlay.CHAT_PANEL.id(), "legacy_immersive_messages", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
                // This legacy queue renderer must also be ordered above chat to avoid chat overdraw.
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableDepthTest();
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                guiGraphics.setColor(1f, 1f, 1f, 1f);

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0f, 0f, 200f);
                ImmersiveMessagesManager.render(guiGraphics);
                guiGraphics.pose().popPose();
                guiGraphics.setColor(1f, 1f, 1f, 1f);
            });
        }
    }
}
