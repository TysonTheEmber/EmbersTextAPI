package net.tysontheember.emberstextapi.neoforge;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.tysontheember.emberstextapi.client.ClientMessageManager;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry;

@EventBusSubscriber(modid = "emberstextapi", bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class NeoForgeClientEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientMessageManager.tick(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        // MC 1.21.1: getPartialTick() returns DeltaTracker, extract float value
        ClientMessageManager.render(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
    }
}

@EventBusSubscriber(modid = "emberstextapi", bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
class NeoForgeClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EffectRegistry.initializeDefaultEffects();
        });
    }
}
