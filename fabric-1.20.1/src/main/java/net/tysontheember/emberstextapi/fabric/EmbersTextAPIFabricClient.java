package net.tysontheember.emberstextapi.fabric;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.tysontheember.emberstextapi.client.ClientMessageManager;
import net.tysontheember.emberstextapi.immersivemessages.ImmersiveMessagesManager;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry;
import net.tysontheember.emberstextapi.network.fabric.packets.FabricClientPacketHandlers;

/**
 * Client-side mod initializer for Fabric.
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
            // Tick legacy queue manager (only when not paused)
            if (client.player != null && !client.isPaused()) {
                ImmersiveMessagesManager.tick();
            }
        });

        // HudRenderCallback is invoked after vanilla InGameHud rendering (including ChatHud).
        // Rendering here keeps immersive messages above chat while still below regular Screen rendering.
        HudRenderCallback.EVENT.register((guiGraphics, tickDelta) -> {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            guiGraphics.setColor(1f, 1f, 1f, 1f);

            // Positive GUI Z ensures immersive quads/glyphs remain in front if other HUD draws share the callback.
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0f, 0f, 200f);
            ClientMessageManager.render(guiGraphics, tickDelta);
            ImmersiveMessagesManager.render(guiGraphics);
            guiGraphics.pose().popPose();
            guiGraphics.setColor(1f, 1f, 1f, 1f);
        });

        EmbersTextAPIFabric.LOGGER.info("EmbersTextAPI client initialization complete");
    }
}
