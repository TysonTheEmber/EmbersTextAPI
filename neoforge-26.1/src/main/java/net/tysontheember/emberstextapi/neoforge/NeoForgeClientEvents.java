package net.tysontheember.emberstextapi.neoforge;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.tysontheember.emberstextapi.client.ClientMessageManager;
import net.tysontheember.emberstextapi.immersivemessages.api.FontAliasRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.MessageEffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.MessageAttributeRegistry;
import net.tysontheember.emberstextapi.EmbersTextAPI;

@EventBusSubscriber(modid = "emberstextapi", value = Dist.CLIENT)
public class NeoForgeClientEvents {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        ClientMessageManager.tick(Minecraft.getInstance());
    }
}

@EventBusSubscriber(modid = "emberstextapi", value = Dist.CLIENT)
class NeoForgeClientModEvents {
    @SubscribeEvent
    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
            VanillaGuiLayers.CHAT,
            Identifier.fromNamespaceAndPath(EmbersTextAPI.MODID, "immersive_messages"),
            (guiGraphics, partialTick) -> {
                guiGraphics.pose().pushMatrix();
                ClientMessageManager.render(guiGraphics, partialTick.getGameTimeDeltaPartialTick(false));
                guiGraphics.pose().popMatrix();
            }
        );
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            EffectRegistry.initializeDefaultEffects();
            MessageEffectRegistry.initializeDefaultEffects();
            MessageAttributeRegistry.initializeDefaultAttributes();
            FontAliasRegistry.initBuiltins();
        });
    }

}
