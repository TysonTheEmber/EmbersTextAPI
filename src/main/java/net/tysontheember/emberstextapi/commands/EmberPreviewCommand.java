package net.tysontheember.emberstextapi.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.tysontheember.emberstextapi.markup.EmberMarkup;

/**
 * Registers a simple client-side preview command.
 */
public final class EmberPreviewCommand {
    private EmberPreviewCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("emberstextapi")
            .then(Commands.literal("preview")
                .then(Commands.greedyString("markup")
                    .executes(EmberPreviewCommand::preview))));
    }

    private static int preview(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String markup = ctx.getArgument("markup", String.class);
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.emberstextapi.preview.feedback", EmberMarkup.toComponent(markup)), false);
        return 1;
    }
}
