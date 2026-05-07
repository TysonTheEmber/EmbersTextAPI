package net.tysontheember.emberstextapi.fabric;

import net.fabricmc.api.ModInitializer;
import net.tysontheember.emberstextapi.chat.FabricChatMarkupHandler;
import net.tysontheember.emberstextapi.commands.FabricCommands;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.MessageEffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.MessageAttributeRegistry;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import net.tysontheember.emberstextapi.platform.NetworkHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbersTextAPIFabric implements ModInitializer {
    public static final String MODID = "emberstextapi";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing EmbersTextAPI for Fabric 26.1");

        ConfigHelper.getInstance().register();

        EffectRegistry.initializeDefaultEffects();
        MessageEffectRegistry.initializeDefaultEffects();
        MessageAttributeRegistry.initializeDefaultAttributes();

        NetworkHelper.getInstance().register();

        FabricCommands.register();

        FabricChatMarkupHandler.register();

        LOGGER.info("EmbersTextAPI initialization complete");
    }
}
