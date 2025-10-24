package net.tysontheember.emberstextapi.client.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.client.ClientMessageManager;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.api.TextAnchor;

import java.util.List;
import java.util.UUID;

/**
 * Client-side showcase command that emits sample markup in common contexts.
 */
@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class TestTextCommand {
    private static final UUID TOOLTIP_ID = UUID.fromString("f36a8c69-18c7-4cb1-8af9-709e815ef61a");
    private static final UUID WRAP_ID = UUID.fromString("9442bb13-1b53-48c4-8775-74d0fbcaf6aa");

    private static final List<String> CHAT_LINES = List.of(
        "<bold>Nested <italic>markup</italic> demo</bold>",
        "<grad from=#ff8800 to=#00aaff><bold>Gradient headline</bold> with color fade</grad>",
        "<underline>Underlined <color value=light_purple>violet accent</color> sample</underline>",
        "<typewriter speed=2.5>Typewriter effect reveals <bold>emphasis</bold> gradually.</typewriter>",
        "<obfuscate mode=random speed=0.75>Hidden <bold>messages</bold> unscramble over time.</obfuscate>",
        "<font value=\"minecraft:alt\"><bold>Alternate font</bold> sample via markup</font>",
        "<grad values=#ff66cc,#ffa500,#ffff66><underline>Multi-stop gradient with underline</underline></grad>",
        "<c value=aqua><bold>Inline color tag alias</bold> demonstration</c>",
        "<grad from=#66ff66 to=#4466ff><italic>Wrapped chat sample showcasing spans.</italic></grad>",
        "<typewriter speed=3.5><obfuscate mode=left speed=1.5>Stacked typewriter + obfuscate sample</obfuscate></typewriter>"
    );

    private TestTextCommand() {
    }

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("eapi")
            .then(Commands.literal("testtext")
                .executes(ctx -> execute(ctx.getSource())));
        event.getDispatcher().register(root);
    }

    private static int execute(CommandSourceStack source) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft != null ? minecraft.player : null;
        if (player == null) {
            return 0;
        }

        sendChatShowcase(player);
        spawnTooltipPreview();
        spawnWrappedLabel();

        source.sendSuccess(() -> Component.literal("Displayed Embers Text API markup showcase."), false);
        return Command.SINGLE_SUCCESS;
    }

    private static void sendChatShowcase(LocalPlayer player) {
        for (String line : CHAT_LINES) {
            player.sendSystemMessage(Component.literal(line));
        }
    }

    private static void spawnTooltipPreview() {
        ImmersiveMessage tooltip = ImmersiveMessage.fromMarkup(120f,
            "<background color=#cc101010 bordercolor=#ffcc8844><bold>Tooltip Preview</bold> <color value=gold>Styled markup</color> <italic>right in-place.</italic></background>");
        tooltip.anchor(TextAnchor.TOP_LEFT)
            .align(TextAnchor.TOP_LEFT)
            .offset(18f, 36f)
            .wrap(150)
            .scale(0.9f)
            .fadeInTicks(10)
            .fadeOutTicks(20);
        ClientMessageManager.open(TOOLTIP_ID, tooltip);
    }

    private static void spawnWrappedLabel() {
        ImmersiveMessage wrapped = ImmersiveMessage.fromMarkup(160f,
            "<grad from=#00ffaa to=#0088ff><bold>Wrapped label demonstrates multi-line gradient text without extra setup.</bold></grad> <italic>Existing overlays reuse span adapters automatically.</italic>");
        wrapped.anchor(TextAnchor.TOP_CENTER)
            .align(TextAnchor.TOP_CENTER)
            .offset(0f, 110f)
            .wrap(220)
            .fadeInTicks(10)
            .fadeOutTicks(30);
        ClientMessageManager.open(WRAP_ID, wrapped);
    }
}
