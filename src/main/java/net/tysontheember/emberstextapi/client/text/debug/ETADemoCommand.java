package net.tysontheember.emberstextapi.client.text.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.client.text.GlobalTextConfig;

/**
 * Provides a lightweight client-only showcase command for manual verification.
 */
@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ETADemoCommand {
    private static final String GRADIENT_SAMPLE = "<grad from=#ff6a00 to=#ffd500><bold>EMBERCRAFT</bold></grad> <grad from=#ffd500 to=#ff6a00>global styling demo</grad>";
    private static final String WRAP_SAMPLE = "<grad from=#51cf66 to=#20c997>This tooltip-length sentence intentionally runs quite long to verify wrapping, gradients, and other per-span styling survive vanilla layout.</grad>";
    private static final String TYPEWRITER_CHAR_SAMPLE = "<typewriter mode=char speed=1.0><grad from=#4dabf7 to=#9775fa>Typewriter (char) reveal — watch me appear one glyph at a time.</grad></typewriter>";
    private static final String TYPEWRITER_WORD_SAMPLE = "<typewriter mode=word speed=0.7><grad from=#ff8787 to=#f06595>Typewriter (word) reveal — this currently shares BY_CHAR timing but checks track plumbing.</grad></typewriter>";

    private ETADemoCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("eta_demo")
                .requires(source -> Minecraft.getInstance().player != null)
                .executes(context -> execute());
        dispatcher.register(command);
    }

    private static int execute() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return 0;
        }

        send(player, Component.translatable("commands.emberstextapi.eta_demo.header")
                .withStyle(ChatFormatting.GOLD));
        send(player, Component.literal(GRADIENT_SAMPLE));

        Component translatableLine = Component.translatable(
                "commands.emberstextapi.eta_demo.translatable",
                Component.literal("<grad from=#63e6be to=#0ca678>Gradient Arg</grad>"),
                Component.literal("<shake type=wave amplitude=0.6 speed=8>Shaky Arg</shake>"));
        send(player, translatableLine);

        Component wrapLine = Component.translatable(
                "commands.emberstextapi.eta_demo.wrap",
                Component.literal(WRAP_SAMPLE));
        send(player, wrapLine);

        send(player, Component.literal(TYPEWRITER_CHAR_SAMPLE));
        send(player, Component.literal(TYPEWRITER_WORD_SAMPLE));

        boolean animationsEnabled = GlobalTextConfig.getOptions().animationEnabled();
        MutableComponent animationState = Component.translatable(
                animationsEnabled
                        ? "commands.emberstextapi.eta_demo.animation.on"
                        : "commands.emberstextapi.eta_demo.animation.off");
        send(player, animationState.withStyle(animationsEnabled ? ChatFormatting.GREEN : ChatFormatting.RED));

        Component overlay = Component.translatable(
                "commands.emberstextapi.eta_demo.overlay",
                Component.literal("<grad from=#74c0fc to=#5f3dc4>Overlay check</grad>"));
        minecraft.gui.setOverlayMessage(overlay, false);

        return 1;
    }

    private static void send(LocalPlayer player, Component message) {
        player.displayClientMessage(message, false);
    }
}
