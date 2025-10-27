package net.tysontheember.emberstextapi.command;

import java.util.Collection;
import java.util.StringJoiner;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.tysontheember.emberstextapi.core.markup.SpanText;
import net.tysontheember.emberstextapi.debug.DebugIM;
import net.tysontheember.emberstextapi.debug.DebugSamples;
import net.tysontheember.emberstextapi.debug.DebugTextItem;

/**
 * Registers the /eta debug command for exercising markup across different systems.
 */
public final class ETADebugCommand {
    private ETADebugCommand() {
    }

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(root());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root() {
        return Commands.literal("eta")
                .then(Commands.literal("debug")
                        .then(Commands.literal("list").executes(ETADebugCommand::list))
                        .then(chatCommand())
                        .then(itemCommand())
                        .then(imCommand())
                        .then(allCommand()));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> chatCommand() {
        return Commands.literal("chat")
                .then(Commands.argument("key", StringArgumentType.word())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            return sendChat(player, context.getArgument("key", String.class));
                        }));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> itemCommand() {
        return Commands.literal("item")
                .then(Commands.argument("key", StringArgumentType.word())
                        .executes(context -> giveItem(context, context.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> giveItem(context, EntityArgument.getPlayer(context, "target")))));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> imCommand() {
        return Commands.literal("im")
                .then(Commands.argument("key", StringArgumentType.word())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            return sendImmersive(player, context.getArgument("key", String.class));
                        }));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> allCommand() {
        return Commands.literal("all")
                .then(Commands.argument("key", StringArgumentType.word())
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            String key = context.getArgument("key", String.class);
                            int total = sendChat(player, key);
                            total += giveItem(context, player);
                            total += sendImmersive(player, key);
                            return total;
                        }));
    }

    private static int list(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("Samples: " + joinKeys(DebugSamples.keys())), false);
        return 1;
    }

    private static int sendChat(ServerPlayer player, String key) {
        return DebugSamples.get(key).map(sample -> {
            Component component = SpanText.parse(sample);
            player.sendSystemMessage(component);
            return 1;
        }).orElseGet(() -> {
            player.sendSystemMessage(missingKeyMessage(key));
            return 0;
        });
    }

    private static int giveItem(CommandContext<CommandSourceStack> context, ServerPlayer target) {
        String key = context.getArgument("key", String.class);
        return DebugSamples.get(key).map(sample -> {
            ItemStack stack = DebugTextItem.createSample(key);
            if (!target.getInventory().add(stack)) {
                target.drop(stack, false);
            }
            context.getSource().sendSuccess(
                    () -> Component.literal("Gave sample '" + key + "' to " + target.getName().getString()), false);
            return 1;
        }).orElseGet(() -> {
            context.getSource().sendFailure(missingKeyMessage(key));
            return 0;
        });
    }

    private static int sendImmersive(ServerPlayer player, String key) {
        return DebugSamples.get(key).map(sample -> {
            Component component = SpanText.parse(sample);
            DebugIM.render(player, component);
            return 1;
        }).orElseGet(() -> {
            player.sendSystemMessage(missingKeyMessage(key));
            return 0;
        });
    }

    private static Component missingKeyMessage(String key) {
        return Component.literal("Unknown sample '" + key + "'. Valid keys: " + joinKeys(DebugSamples.keys()));
    }

    private static String joinKeys(Collection<String> keys) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String key : keys) {
            joiner.add(key);
        }
        return joiner.toString();
    }
}
