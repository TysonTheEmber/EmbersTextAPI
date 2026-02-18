package net.tysontheember.emberstextapi;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.tysontheember.emberstextapi.commands.MessageCommands;
import net.tysontheember.emberstextapi.network.neoforge.NeoForgeNetworkHandler;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import net.tysontheember.emberstextapi.platform.NetworkHelper;
import org.slf4j.Logger;

@Mod("emberstextapi")
public class EmbersTextAPI {

    public static final String MODID = "emberstextapi";
    private static final Logger LOGGER = LogUtils.getLogger();

    public EmbersTextAPI(IEventBus modEventBus) {
        LOGGER.info("EmbersTextAPI NeoForge 1.21.1 initializing...");

        // Register config
        ConfigHelper.getInstance().register();

        // Setup event
        modEventBus.addListener(this::commonSetup);

        // Network registration
        modEventBus.addListener(this::registerPayloads);

        // Register server commands
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);

        // Check for incompatible mods
        checkIncompatibleMods();
    }

    private static void checkIncompatibleMods() {
        if (ModList.get().isLoaded("emojiful")) {
            LOGGER.error("==========================================================");
            LOGGER.error("EmbersTextAPI has detected Emojiful is installed!");
            LOGGER.error("Emojiful modifies the text renderer in ways that conflict");
            LOGGER.error("with EmbersTextAPI's mixin-based rendering pipeline.");
            LOGGER.error("Text rendering may be broken or cause crashes.");
            LOGGER.error("Please remove Emojiful.");
            LOGGER.error("==========================================================");
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("EmbersTextAPI common setup complete");
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID)
            .versioned("3")
            .optional();

        // Register client-bound packets with StreamCodecs (NeoForge 1.21.1 API)
        registrar.playToClient(
            NeoForgeNetworkHandler.TooltipPayload.TYPE,
            NeoForgeNetworkHandler.TooltipPayload.STREAM_CODEC,
            NeoForgeNetworkHandler::handleTooltip
        );

        registrar.playToClient(
            NeoForgeNetworkHandler.OpenMessagePayload.TYPE,
            NeoForgeNetworkHandler.OpenMessagePayload.STREAM_CODEC,
            NeoForgeNetworkHandler::handleOpenMessage
        );

        registrar.playToClient(
            NeoForgeNetworkHandler.UpdateMessagePayload.TYPE,
            NeoForgeNetworkHandler.UpdateMessagePayload.STREAM_CODEC,
            NeoForgeNetworkHandler::handleUpdateMessage
        );

        registrar.playToClient(
            NeoForgeNetworkHandler.CloseMessagePayload.TYPE,
            NeoForgeNetworkHandler.CloseMessagePayload.STREAM_CODEC,
            NeoForgeNetworkHandler::handleCloseMessage
        );

        registrar.playToClient(
            NeoForgeNetworkHandler.CloseAllMessagesPayload.TYPE,
            NeoForgeNetworkHandler.CloseAllMessagesPayload.STREAM_CODEC,
            NeoForgeNetworkHandler::handleCloseAllMessages
        );

        registrar.playToClient(
            NeoForgeNetworkHandler.OpenQueuePayload.TYPE,
            NeoForgeNetworkHandler.OpenQueuePayload.STREAM_CODEC,
            NeoForgeNetworkHandler::handleOpenQueue
        );

        registrar.playToClient(
            NeoForgeNetworkHandler.ClearQueuePayload.TYPE,
            NeoForgeNetworkHandler.ClearQueuePayload.STREAM_CODEC,
            NeoForgeNetworkHandler::handleClearQueue
        );

        LOGGER.info("Network payloads registered");
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        MessageCommands.register(event.getDispatcher());
    }

    public static void sendMessage(net.minecraft.server.level.ServerPlayer player,
                                   net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage message) {
        NetworkHelper.getInstance().sendMessage(player, message);
    }
}
