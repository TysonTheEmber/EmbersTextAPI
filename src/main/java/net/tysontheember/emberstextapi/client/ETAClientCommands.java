package net.tysontheember.emberstextapi.client;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.EmbersTextAPI;

/**
 * Registers the /eta_demo client command for quick smoke testing.
 */
@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID, value = Dist.CLIENT)
public final class ETAClientCommands {
    private ETAClientCommands() {
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(demoCommand());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> demoCommand() {
        return Commands.literal("eta_demo").executes(context -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return 0;
            }
            mc.player.sendSystemMessage(Component.literal("<grad from=#ff6a00 to=#ffd500><bold>EMBERCRAFT</bold></grad> Hello"));
            mc.player.sendSystemMessage(Component.literal("<bold>Nested <italic><wave a=1.5>markup</wave></italic> demo</bold>"));
            mc.gui.setOverlayMessage(Component.literal("<typewriter speed=35><grad from=#ff6a00 to=#ffd500>Typewriter & Gradient</grad> demo</typewriter>"), false);
            return 1;
        });
    }
}
