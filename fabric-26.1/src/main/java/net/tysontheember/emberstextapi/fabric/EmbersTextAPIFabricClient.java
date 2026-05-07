package net.tysontheember.emberstextapi.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;
import net.tysontheember.emberstextapi.client.ClientMessageManager;
import net.tysontheember.emberstextapi.immersivemessages.api.FontAliasRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.MessageEffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.MessageAttributeRegistry;
import net.tysontheember.emberstextapi.network.fabric.packets.FabricClientPacketHandlers;

public class EmbersTextAPIFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EmbersTextAPIFabric.LOGGER.info("Initializing EmbersTextAPI client for Fabric 26.1");

        EffectRegistry.initializeDefaultEffects();
        MessageEffectRegistry.initializeDefaultEffects();
        MessageAttributeRegistry.initializeDefaultAttributes();
        FontAliasRegistry.initBuiltins();
        EmbersTextAPIFabric.LOGGER.info("Initialized visual effects system");

        FabricClientPacketHandlers.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientMessageManager.tick(client);
        });

        HudElementRegistry.attachElementAfter(
            Identifier.withDefaultNamespace("chat"),
            Identifier.fromNamespaceAndPath("emberstextapi", "immersive_messages"),
            (drawContext, tickCounter) -> {
                drawContext.pose().pushMatrix();
                ClientMessageManager.render(drawContext, tickCounter.getGameTimeDeltaPartialTick(false));
                drawContext.pose().popMatrix();
            }
        );

        EmbersTextAPIFabric.LOGGER.info("EmbersTextAPI client initialization complete");
    }
}
