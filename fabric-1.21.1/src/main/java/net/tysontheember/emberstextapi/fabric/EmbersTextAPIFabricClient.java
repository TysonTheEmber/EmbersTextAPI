package net.tysontheember.emberstextapi.fabric;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.client.ClientMessageManager;
import net.tysontheember.emberstextapi.immersivemessages.api.FontAliasRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.MessageEffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.MessageAttributeRegistry;
import net.tysontheember.emberstextapi.sdf.SDFShaders;
import net.tysontheember.emberstextapi.network.fabric.packets.FabricClientPacketHandlers;

public class EmbersTextAPIFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EmbersTextAPIFabric.LOGGER.info("Initializing EmbersTextAPI client for Fabric");

        EffectRegistry.initializeDefaultEffects();
        MessageEffectRegistry.initializeDefaultEffects();
        MessageAttributeRegistry.initializeDefaultAttributes();
        FontAliasRegistry.initBuiltins();
        EmbersTextAPIFabric.LOGGER.info("Initialized visual effects system");

        CoreShaderRegistrationCallback.EVENT.register(context -> {
            context.register(
                ResourceLocation.fromNamespaceAndPath("minecraft", "rendertype_eta_sdf_text"),
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                SDFShaders::setSdfTextShader
            );
            context.register(
                ResourceLocation.fromNamespaceAndPath("minecraft", "rendertype_eta_sdf_text_see_through"),
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                SDFShaders::setSdfTextSeeThroughShader
            );
        });

        FabricClientPacketHandlers.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientMessageManager.tick(client);
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            drawContext.setColor(1f, 1f, 1f, 1f);

            drawContext.pose().pushPose();
            drawContext.pose().translate(0f, 0f, 200f);
            ClientMessageManager.render(drawContext, tickCounter.getGameTimeDeltaPartialTick(false));
            drawContext.pose().popPose();
            drawContext.setColor(1f, 1f, 1f, 1f);
        });

        EmbersTextAPIFabric.LOGGER.info("EmbersTextAPI client initialization complete");
    }
}
