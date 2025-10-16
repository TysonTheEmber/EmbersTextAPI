package net.tysontheember.emberstextapi.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.tysontheember.emberstextapi.markup.EmberMarkup;

/**
 * Client-facing command that renders a quick preview of markup strings.
 */
public final class EmberPreviewCommand {
    private EmberPreviewCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("emberstextapi")
                .then(Commands.literal("preview")
                        .then(Commands.argument("markup", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String markup = StringArgumentType.getString(ctx, "markup");
                                    Component component = EmberMarkup.toComponent(markup);
                                    ctx.getSource().sendSuccess(() -> component, false);
                                    return 1;
                                }))));
    }
}
