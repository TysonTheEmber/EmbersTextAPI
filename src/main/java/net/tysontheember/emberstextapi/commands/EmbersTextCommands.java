package net.tysontheember.emberstextapi.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.api.EmbersText;

@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID)
public final class EmbersTextCommands {
    private EmbersTextCommands() {
    }

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("embertextapi")
                .then(Commands.literal("parse")
                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    String raw = StringArgumentType.getString(ctx, "text");
                                    Component parsed = EmbersText.render(raw, ctx.getSource().getLevel().getGameTime() / 20f);
                                    ctx.getSource().sendSuccess(() -> parsed, false);
                                    return 1;
                                })))
                .then(Commands.literal("demo")
                        .executes(ctx -> {
                            ctx.getSource().sendSuccess(() -> EmbersText.render("<wave amp=1.5>Hi there!</wave>", ctx.getSource().getLevel().getGameTime() / 20f), false);
                            return 1;
                        })));
    }
}
