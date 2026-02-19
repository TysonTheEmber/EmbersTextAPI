package net.tysontheember.emberstextapi.fabric;

import com.mojang.blaze3d.systems.RenderSystem;
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

        // HudRenderCallback runs after InGameHud.render (including chat), but before Screen.render.
        // This keeps immersive text in front of non-GUI chat, but behind opened GUIs.
        // MC 1.21.1: HudRenderCallback passes DeltaTracker instead of float
        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            drawContext.setColor(1f, 1f, 1f, 1f);

            // Keep immersive messages in front of chat quads/glyphs if HUD layers share this callback.
            drawContext.pose().pushPose();
            drawContext.pose().translate(0f, 0f, 200f);
            ClientMessageManager.render(drawContext, tickCounter.getGameTimeDeltaPartialTick(false));
            drawContext.pose().popPose();
            drawContext.setColor(1f, 1f, 1f, 1f);
        });

        EmbersTextAPIFabric.LOGGER.info("EmbersTextAPI client initialization complete");
    }
}
