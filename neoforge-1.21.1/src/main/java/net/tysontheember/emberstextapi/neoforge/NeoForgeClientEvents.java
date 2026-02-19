package net.tysontheember.emberstextapi.neoforge;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.tysontheember.emberstextapi.client.ClientMessageManager;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry;
import net.tysontheember.emberstextapi.EmbersTextAPI;

@EventBusSubscriber(modid = "emberstextapi", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class NeoForgeClientEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientMessageManager.tick(Minecraft.getInstance());
    }
}

@EventBusSubscriber(modid = "emberstextapi", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
class NeoForgeClientModEvents {
    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
            VanillaGuiLayers.CHAT,
            ResourceLocation.fromNamespaceAndPath(EmbersTextAPI.MODID, "immersive_messages"),
            (guiGraphics, partialTick) -> {
                // Chat is a dedicated vanilla GUI layer in NeoForge 1.21.1, so register above it explicitly.
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableDepthTest();
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                guiGraphics.setColor(1f, 1f, 1f, 1f);

                // Positive GUI Z keeps our quads/glyphs in front within this layer while preserving animations/alpha.
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0f, 0f, 200f);
                ClientMessageManager.render(guiGraphics, partialTick.getGameTimeDeltaPartialTick(false));
                guiGraphics.pose().popPose();
                guiGraphics.setColor(1f, 1f, 1f, 1f);
            }
        );
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EffectRegistry.initializeDefaultEffects();
        });
    }
}
