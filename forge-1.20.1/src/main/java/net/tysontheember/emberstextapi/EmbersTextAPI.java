package net.tysontheember.emberstextapi;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.client.event.RegisterShadersEvent;
import org.slf4j.Logger;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.api.FontAliasRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.MessageEffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.MessageAttributeRegistry;
import net.tysontheember.emberstextapi.sdf.SDFShaders;
import net.tysontheember.emberstextapi.platform.NetworkHelper;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import java.io.IOException;

@Mod(EmbersTextAPI.MODID)
public class EmbersTextAPI
{
    public static final String MODID = "emberstextapi";
    public static final Logger LOGGER = LogUtils.getLogger();
    public EmbersTextAPI()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        ConfigHelper.getInstance().register();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        NetworkHelper.getInstance().register();
    }

    public static void sendMessage(net.minecraft.server.level.ServerPlayer player, ImmersiveMessage message) {
        NetworkHelper.getInstance().sendMessage(player, message);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            event.enqueueWork(() -> {
                EffectRegistry.initializeDefaultEffects();
                MessageEffectRegistry.initializeDefaultEffects();
                MessageAttributeRegistry.initializeDefaultAttributes();
                FontAliasRegistry.initBuiltins();
                LOGGER.info("EmbersTextAPI: Initialized visual effects system");
            });
        }

        @SubscribeEvent
        public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
            event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                    "rendertype_eta_sdf_text",
                    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                SDFShaders::setSdfTextShader
            );
            event.registerShader(
                new ShaderInstance(event.getResourceProvider(),
                    "rendertype_eta_sdf_text_see_through",
                    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP),
                SDFShaders::setSdfTextSeeThroughShader
            );
        }
    }
}
