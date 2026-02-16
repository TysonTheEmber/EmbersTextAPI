package net.tysontheember.emberstextapi.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.tysontheember.emberstextapi.client.ClientMessageManager;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry;
import net.tysontheember.emberstextapi.network.fabric.packets.FabricClientPacketHandlers;

/**
 * Client-side mod initializer for Fabric 1.21.1.
 * Handles client-specific setup and event registration.
 */
public class EmbersTextAPIFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EmbersTextAPIFabric.LOGGER.info("Initializing EmbersTextAPI client for Fabric");

        // Initialize effect registry
        EffectRegistry.initializeDefaultEffects();
        EmbersTextAPIFabric.LOGGER.info("Initialized visual effects system");

        // Register network packet handlers
        FabricClientPacketHandlers.register();

        // Register client tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientMessageManager.tick(client);
        });

        // Register HUD render event
        // MC 1.21.1: HudRenderCallback passes DeltaTracker instead of float
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            ClientMessageManager.render(drawContext, tickCounter.getGameTimeDeltaPartialTick(false));
        });

        EmbersTextAPIFabric.LOGGER.info("EmbersTextAPI client initialization complete");
    }
}
