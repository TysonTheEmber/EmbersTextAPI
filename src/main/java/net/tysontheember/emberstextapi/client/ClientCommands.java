package net.tysontheember.emberstextapi.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.immersivemessages.effects.preset.PresetLoader;
import net.tysontheember.emberstextapi.immersivemessages.effects.preset.PresetRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client-side commands for EmbersTextAPI.
 * <p>
 * Registers commands that only make sense on the client, such as reloading presets.
 * </p>
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID, value = Dist.CLIENT)
public final class ClientCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCommands.class);

    private ClientCommands() {
        // Utility class
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        // Register /emberstextapi reload
        event.getDispatcher().register(
                Commands.literal("emberstextapi")
                        .then(Commands.literal("reload")
                                .executes(ClientCommands::reloadPresets))
        );

        // Register /eta reload (alias)
        event.getDispatcher().register(
                Commands.literal("eta")
                        .then(Commands.literal("reload")
                                .executes(ClientCommands::reloadPresets))
        );

        LOGGER.debug("Registered client commands: /emberstextapi reload, /eta reload");
    }

    /**
     * Reload all presets from resource packs.
     *
     * @param ctx Command context
     * @return Command success
     */
    private static int reloadPresets(CommandContext<CommandSourceStack> ctx) {
        try {
            var resourceManager = Minecraft.getInstance().getResourceManager();

            // Clear caches
            TextLayoutCache.clear();
            PresetRegistry.clear();

            // Load and register presets
            var presets = PresetLoader.loadAll(resourceManager);
            presets.forEach(PresetRegistry::register);

            int count = presets.size();
            LOGGER.info("Reloaded {} presets via command", count);

            ctx.getSource().sendSuccess(() ->
                    Component.literal("[EmbersTextAPI] Reloaded " + count + " preset(s)."), false);

            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            LOGGER.error("Failed to reload presets", e);
            ctx.getSource().sendFailure(
                    Component.literal("[EmbersTextAPI] Failed to reload presets: " + e.getMessage()));
            return 0;
        }
    }
}
