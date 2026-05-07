package net.tysontheember.emberstextapi.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.fabric.EmbersTextAPIFabric;

public class FabricCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            dispatcher.register(
                Commands.literal("emberstextapi")
                    .then(FabricMessageCommands.testSubcommand())
                    .then(FabricMessageCommands.sendSubcommand())
                    .then(FabricMessageCommands.queueSubcommand())
                    .then(FabricMessageCommands.clearQueueSubcommand())
                    .then(FabricMessageCommands.stopQueueSubcommand())
                    .then(FabricMessageCommands.closeAllSubcommand())
            );

            dispatcher.register(
                Commands.literal("eta")
                    .executes(context -> {
                        showHelp(context);
                        return 1;
                    })
                    .then(Commands.literal("help")
                        .executes(context -> {
                            showHelp(context);
                            return 1;
                        })
                    )
                    .then(FabricMessageCommands.testSubcommand())
                    .then(FabricMessageCommands.sendSubcommand())
                    .then(FabricMessageCommands.queueSubcommand())
                    .then(FabricMessageCommands.clearQueueSubcommand())
                    .then(FabricMessageCommands.stopQueueSubcommand())
                    .then(FabricMessageCommands.closeAllSubcommand())
            );

            EmbersTextAPIFabric.LOGGER.info("Registered commands");
        });
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

        context.getSource().sendSuccess(() -> Component.literal(""), false);
    }
}
