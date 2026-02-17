package net.tysontheember.emberstextapi.welcome;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.config.ModConfig;

/**
 * Commands for controlling the welcome message feature and providing help.
 */
@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID)
public class WelcomeMessageCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        // Register /eta command
        event.getDispatcher().register(
            Commands.literal("eta")
                .executes(context -> {
                    // Default /eta command shows help
                    showHelp(context);
                    return 1;
                })
                .then(Commands.literal("help")
                    .executes(context -> {
                        showHelp(context);
                        return 1;
                    })
                )
                .then(Commands.literal("welcome")
                    .requires(source -> source.hasPermission(2)) // Require operator permission
                    .then(Commands.literal("enable")
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                            .executes(context -> {
                                boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                ModConfig.setWelcomeMessageEnabled(enabled);
                                context.getSource().sendSuccess(() ->
                                    Component.literal("Welcome message " + (enabled ? "enabled" : "disabled")),
                                    true);
                                return 1;
                            })
                        )
                    )
                )
        );
    }

    private static void showHelp(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal(""), false);

        context.getSource().sendSuccess(() ->
            Component.literal("=== ")
                .withStyle(style -> style.withColor(0xAAAAAA))
                .append(Component.literal("EmbersTextAPI")
                    .withStyle(style -> style.withColor(0xFFAA00).withBold(true)))
                .append(Component.literal(" ===")
                    .withStyle(style -> style.withColor(0xAAAAAA))),
            false);

        context.getSource().sendSuccess(() ->
            Component.literal("A powerful API for creating immersive, animated text")
                .withStyle(style -> style.withColor(0xCCCCCC)),
            false);

        context.getSource().sendSuccess(() ->
            Component.literal("with gradients, typewriter effects, shake, fade, and more!")
                .withStyle(style -> style.withColor(0xCCCCCC)),
            false);

        context.getSource().sendSuccess(() -> Component.literal(""), false);

        context.getSource().sendSuccess(() ->
            Component.literal("Documentation & Examples: ")
                .withStyle(style -> style.withColor(0xAAAAAA))
                .append(Component.literal("tysontheember.dev")
                    .withStyle(Style.EMPTY
                        .withColor(0x55AAFF)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://tysontheember.dev")))),
            false);

        context.getSource().sendSuccess(() ->
            Component.literal("Join the Discord: ")
                .withStyle(style -> style.withColor(0xAAAAAA))
                .append(Component.literal("discord.gg/vY77wF48GV")
                    .withStyle(Style.EMPTY
                        .withColor(0x7289DA)
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/vY77wF48GV")))),
            false);

        context.getSource().sendSuccess(() -> Component.literal(""), false);

        context.getSource().sendSuccess(() ->
            Component.literal("Commands:")
                .withStyle(style -> style.withColor(0xFFAA00).withBold(true)),
            false);

        context.getSource().sendSuccess(() ->
            Component.literal("  /eta help")
                .withStyle(style -> style.withColor(0x55FF55))
                .append(Component.literal(" - Show this help message")
                    .withStyle(style -> style.withColor(0xAAAAAA))),
            false);

        context.getSource().sendSuccess(() ->
            Component.literal("  /eta welcome enable <true | false>")
                .withStyle(style -> style.withColor(0x55FF55))
                .append(Component.literal(" - Toggle welcome message (Op)")
                    .withStyle(style -> style.withColor(0xAAAAAA))),
            false);

        context.getSource().sendSuccess(() -> Component.literal(""), false);
    }
}
