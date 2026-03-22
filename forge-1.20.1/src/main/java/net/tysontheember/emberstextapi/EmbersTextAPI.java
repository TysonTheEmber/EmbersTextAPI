package net.tysontheember.emberstextapi;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
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
import net.tysontheember.emberstextapi.sdf.SDFShaders;
import net.tysontheember.emberstextapi.platform.NetworkHelper;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import java.io.IOException;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EmbersTextAPI.MODID)
public class EmbersTextAPI
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "emberstextapi";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public EmbersTextAPI(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register mod configuration
        ConfigHelper.getInstance().register();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        NetworkHelper.getInstance().register();
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }

    /**
     * Sends an {@link ImmersiveMessage} to the specified player.
     * This is a convenience method for server-side code.
     */
    public static void sendMessage(net.minecraft.server.level.ServerPlayer player, ImmersiveMessage message) {
        NetworkHelper.getInstance().sendMessage(player, message);
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Initialize effect registry with all built-in effects
            event.enqueueWork(() -> {
                EffectRegistry.initializeDefaultEffects();
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
