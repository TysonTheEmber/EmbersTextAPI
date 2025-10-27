package net.tysontheember.emberstextapi;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.minecraftforge.network.PacketDistributor;
import net.tysontheember.emberstextapi.command.ETADebugCommand;
import net.tysontheember.emberstextapi.debug.ETAItems;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.network.TooltipPacket;
import net.tysontheember.emberstextapi.network.Network;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EmbersTextAPI.MODID)
public class EmbersTextAPI
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "emberstextapi";
    public EmbersTextAPI(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        ETAItems.ITEMS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(ETADebugCommand::register);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        Network.register();
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }

    /**
     * Sends an {@link ImmersiveMessage} to the specified player.  This is a
     * convenience wrapper around the {@link TooltipPacket} network channel.
     */
    public static void sendMessage(net.minecraft.server.level.ServerPlayer player, ImmersiveMessage message) {
        TooltipPacket.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new TooltipPacket(message));
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
        }
    }
}
