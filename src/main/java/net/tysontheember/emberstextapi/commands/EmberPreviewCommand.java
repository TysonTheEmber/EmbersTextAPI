package net.tysontheember.emberstextapi.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.tysontheember.emberstextapi.markup.EmberMarkup;

/**
 * Command that previews markup strings directly in chat for quick iteration.
 */
public final class EmberPreviewCommand {
    private EmberPreviewCommand() {
    }

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("preview")
            .then(Commands.argument("markup", StringArgumentType.greedyString())
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    String markup = StringArgumentType.getString(ctx, "markup");
                    player.sendSystemMessage(Component.translatable("command.emberstextapi.preview.header"));
                    player.sendSystemMessage(EmberMarkup.toComponent(markup));
                    return Command.SINGLE_SUCCESS;
                }));
    }
}
