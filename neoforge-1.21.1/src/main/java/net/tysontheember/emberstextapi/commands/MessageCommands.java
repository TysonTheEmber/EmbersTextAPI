package net.tysontheember.emberstextapi.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.config.ModConfig;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.api.MarkupParser;
import net.tysontheember.emberstextapi.immersivemessages.api.TextAnchor;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;
import net.tysontheember.emberstextapi.platform.NetworkHelper;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MessageCommands {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Register full command name
        dispatcher.register(
            Commands.literal("emberstextapi")
                .requires(source -> source.hasPermission(2))
                .then(testSubcommand())
                .then(sendSubcommand())
                .then(queueSubcommand())
                .then(clearQueueSubcommand())
                .then(stopQueueSubcommand())
                .then(closeAllSubcommand())
        );

        // Register short alias with help and welcome subcommands
        dispatcher.register(
            Commands.literal("eta")
                .requires(source -> source.hasPermission(2))
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
                .then(Commands.literal("welcome")
                    .requires(source -> source.hasPermission(2))
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
                .then(testSubcommand())
                .then(sendSubcommand())
                .then(queueSubcommand())
                .then(clearQueueSubcommand())
                .then(stopQueueSubcommand())
                .then(closeAllSubcommand())
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

    private static ArgumentBuilder<net.minecraft.commands.CommandSourceStack, ?> testSubcommand() {
        return Commands.literal("test")
            .then(Commands.argument("id", IntegerArgumentType.integer(1, 33))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    int id = IntegerArgumentType.getInteger(ctx, "id");
                    runTest(player, id);
                    return Command.SINGLE_SUCCESS;
                }))
            .then(EffectTestCommands.buildEffectTestCommand())
            .then(EffectTestCommands.buildComboCommand());
    }

    private static ArgumentBuilder<net.minecraft.commands.CommandSourceStack, ?> sendSubcommand() {
        return Commands.literal("send")
            .then(Commands.argument("player", EntityArgument.players())
                .then(Commands.argument("duration", FloatArgumentType.floatArg())
                    .then(Commands.argument("text", StringArgumentType.greedyString())
                        .executes(ctx -> sendBasicMessage(ctx)))));
    }

    private static int sendBasicMessage(com.mojang.brigadier.context.CommandContext<net.minecraft.commands.CommandSourceStack> ctx)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "player");
        float duration = FloatArgumentType.getFloat(ctx, "duration");
        String text = StringArgumentType.getString(ctx, "text");

        // Support markup in basic send command
        ImmersiveMessage msg;
        if (text.contains("<") && text.contains(">")) {
            msg = ImmersiveMessage.fromMarkup(duration, text);
        } else {
            msg = ImmersiveMessage.builder(duration, text);
        }

        for (ServerPlayer target : targets) {
            EmbersTextAPI.sendMessage(target, msg);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static ArgumentBuilder<net.minecraft.commands.CommandSourceStack, ?> queueSubcommand() {
        return Commands.literal("queue")
            .then(Commands.argument("player", EntityArgument.players())
                .then(Commands.argument("channel", StringArgumentType.word())
                    .then(Commands.argument("queue_definition", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "player");
                            String channel = StringArgumentType.getString(ctx, "channel");
                            String queueDef = StringArgumentType.getString(ctx, "queue_definition");

                            // Split on " | " to get steps
                            String[] rawSteps = queueDef.split(" \\| ");
                            List<List<ImmersiveMessage>> steps = new ArrayList<>();

                            for (String rawStep : rawSteps) {
                                // Split on " & " to get simultaneous messages in a step
                                String[] rawMessages = rawStep.split(" & ");
                                List<ImmersiveMessage> stepMsgs = new ArrayList<>();

                                for (String rawMsg : rawMessages) {
                                    String text = rawMsg.trim();
                                    // Strip surrounding quotes
                                    if (text.length() >= 2
                                            && ((text.startsWith("\"") && text.endsWith("\""))
                                            || (text.startsWith("'") && text.endsWith("'")))) {
                                        text = text.substring(1, text.length() - 1);
                                    }

                                    // Extract <dur:N> tag
                                    Object[] extracted = MarkupParser.extractDuration(text);
                                    float duration = (float) extracted[0];
                                    String markup = (String) extracted[1];

                                    if (duration < 0) {
                                        LOGGER.warn("No <dur:N> found in queue message '{}', defaulting to 60", text);
                                        duration = 60f;
                                    }

                                    stepMsgs.add(ImmersiveMessage.fromMarkup(duration, markup));
                                }

                                steps.add(stepMsgs);
                            }

                            for (ServerPlayer target : targets) {
                                NetworkHelper.getInstance().sendQueue(target, channel, steps);
                            }
                            return Command.SINGLE_SUCCESS;
                        }))));
    }

    private static ArgumentBuilder<net.minecraft.commands.CommandSourceStack, ?> clearQueueSubcommand() {
        return Commands.literal("clearqueue")
            .then(Commands.argument("player", EntityArgument.players())
                .executes(ctx -> {
                    Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "player");
                    for (ServerPlayer target : targets) {
                        NetworkHelper.getInstance().sendClearAllQueues(target);
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("channel", StringArgumentType.word())
                    .executes(ctx -> {
                        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "player");
                        String channel = StringArgumentType.getString(ctx, "channel");
                        for (ServerPlayer target : targets) {
                            NetworkHelper.getInstance().sendClearQueue(target, channel);
                        }
                        return Command.SINGLE_SUCCESS;
                    })));
    }

    private static ArgumentBuilder<net.minecraft.commands.CommandSourceStack, ?> stopQueueSubcommand() {
        return Commands.literal("stopqueue")
            .then(Commands.argument("player", EntityArgument.players())
                .executes(ctx -> {
                    Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "player");
                    for (ServerPlayer target : targets) {
                        NetworkHelper.getInstance().sendStopAllQueues(target);
                    }
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("channel", StringArgumentType.word())
                    .executes(ctx -> {
                        Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "player");
                        String channel = StringArgumentType.getString(ctx, "channel");
                        for (ServerPlayer target : targets) {
                            NetworkHelper.getInstance().sendStopQueue(target, channel);
                        }
                        return Command.SINGLE_SUCCESS;
                    })));
    }

    private static ArgumentBuilder<net.minecraft.commands.CommandSourceStack, ?> closeAllSubcommand() {
        return Commands.literal("closeall")
            .then(Commands.argument("player", EntityArgument.players())
                .executes(ctx -> {
                    Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "player");
                    for (ServerPlayer target : targets) {
                        NetworkHelper.getInstance().sendStopAllQueues(target);
                    }
                    return Command.SINGLE_SUCCESS;
                }));
    }

    private static void runTest(ServerPlayer player, int id) {
        switch (id) {
            // --- Group 1: Layout & Display (1-7) ---
            case 1 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.builder(100f, "Plain text message")
                            .anchor(TextAnchor.MIDDLE)
                            .scale(2f));
            case 2 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.builder(100f, "Background box")
                            .background(true)
                            .anchor(TextAnchor.MIDDLE)
                            .scale(2f));
            case 3 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.builder(100f, "Gold border")
                            .background(true)
                            .borderGradient(new ImmersiveColor(0xFFFFAA00), new ImmersiveColor(0xFFFFAA00))
                            .anchor(TextAnchor.MIDDLE)
                            .scale(2f));
            case 4 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.builder(100f, "Large scale 3x")
                            .scale(3f)
                            .anchor(TextAnchor.MIDDLE));
            case 5 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.builder(140f, "This is a long message that wraps onto multiple lines when there is enough text to overflow the line width.")
                            .wrap(180)
                            .background(true)
                            .anchor(TextAnchor.MIDDLE));
            case 6 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.builder(140f, "Smooth fade in and fade out")
                            .fadeInTicks(20)
                            .fadeOutTicks(20)
                            .anchor(TextAnchor.MIDDLE)
                            .scale(2f));
            case 7 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.builder(100f, "Shifted up 40px")
                            .anchor(TextAnchor.MIDDLE)
                            .offset(0f, -40f)
                            .scale(2f));

            // --- Group 2: Text Formatting (8-13) ---
            case 8 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<bold>Bold</bold>  <italic>Italic</italic>  <c value=#FF5555>Red</c>")
                            .anchor(TextAnchor.MIDDLE)
                            .scale(1.5f));
            case 9 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<c value=gold>Gold</c>  <c value=aqua>Aqua</c>  <c value=#FF55FF>Pink</c>  <c value=green>Green</c>")
                            .anchor(TextAnchor.MIDDLE)
                            .scale(1.5f));
            case 10 -> {
                Component norse = Component.literal("\u16A0\u16A2\u16A6\u16A8\u16AB\u16B2")
                        .withStyle(s -> s.withFont(ResourceLocation.fromNamespaceAndPath(EmbersTextAPI.MODID, "norse")));
                EmbersTextAPI.sendMessage(player, new ImmersiveMessage(norse, 100f).scale(4f).anchor(TextAnchor.MIDDLE));
            }
            case 11 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<font id=emberstextapi:metamorphous>Metamorphous Fantasy Font!</font>")
                            .scale(2f)
                            .anchor(TextAnchor.MIDDLE));
            case 12 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<grad from=FF0000 to=00FFFF>Red to Cyan Gradient</grad>")
                            .scale(2f)
                            .anchor(TextAnchor.MIDDLE));
            case 13 -> {
                List<TextSpan> spans = new ArrayList<>();
                spans.add(new TextSpan("\u16A0\u16A2\u16A6 ").font(ResourceLocation.fromNamespaceAndPath(EmbersTextAPI.MODID, "norse")));
                spans.add(new TextSpan("and default italic"));
                EmbersTextAPI.sendMessage(player, new ImmersiveMessage(spans, 100f).background(true).scale(1.5f).anchor(TextAnchor.MIDDLE));
            }

            // --- Group 3: Text Animation (14-19) ---
            case 14 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(140f, "<type speed=50>Typewriter reveal effect!</type>")
                            .scale(1.5f)
                            .background(true)
                            .anchor(TextAnchor.MIDDLE));
            case 15 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(120f, "<obfuscate mode=reveal speed=60 direction=left>Revealing left to right</obfuscate>")
                            .scale(1.5f)
                            .background(true)
                            .anchor(TextAnchor.MIDDLE));
            case 16 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<obfuscate mode=random>Random obfuscation mask</obfuscate>")
                            .scale(1.5f)
                            .background(true)
                            .anchor(TextAnchor.MIDDLE));
            case 17 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<rainbow>Rainbow color cycling</rainbow>")
                            .scale(2f)
                            .anchor(TextAnchor.MIDDLE));
            case 18 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<neon c=00FFFF>Neon glow effect</neon>")
                            .scale(2f)
                            .anchor(TextAnchor.MIDDLE));
            case 19 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<shadow c=FF0000 x=3 y=3>Red drop shadow</shadow>")
                            .scale(2f)
                            .anchor(TextAnchor.MIDDLE));

            // --- Group 4: Motion Effects (20-25) ---
            case 20 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<wave>Wave vertical motion</wave>")
                            .scale(2f)
                            .anchor(TextAnchor.MIDDLE));
            case 21 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<bounce>Bounce animation</bounce>")
                            .scale(2f)
                            .anchor(TextAnchor.MIDDLE));
            case 22 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<pulse>Pulse opacity</pulse>")
                            .scale(2f)
                            .anchor(TextAnchor.MIDDLE));
            case 23 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<shake>Shake jitter</shake>")
                            .scale(2f)
                            .anchor(TextAnchor.MIDDLE));
            case 24 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<glitch>Glitch distortion</glitch>")
                            .scale(2f)
                            .anchor(TextAnchor.MIDDLE));
            case 25 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(100f, "<turb>Turbulence chaos</turb>")
                            .scale(2f)
                            .anchor(TextAnchor.MIDDLE));

            // --- Group 5: Items & Entities (26-29) ---
            case 26 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(120f,
                            "Found <item value=\"minecraft:diamond\" size=1></item> x3 and <item value=\"minecraft:gold_ingot\" size=1></item> x5!")
                            .anchor(TextAnchor.MIDDLE));
            case 27 -> {
                List<TextSpan> spans = new ArrayList<>();
                spans.add(new TextSpan("Raised "));
                spans.add(new TextSpan("").item("minecraft:emerald", 1).itemOffset(0, -3));
                spans.add(new TextSpan(" item offset"));
                EmbersTextAPI.sendMessage(player, new ImmersiveMessage(spans, 100f).anchor(TextAnchor.MIDDLE));
            }
            case 28 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(120f, "Beware <entity value=\"minecraft:creeper\" scale=0.6></entity>!")
                            .scale(3f)
                            .anchor(TextAnchor.MIDDLE));
            case 29 -> {
                List<TextSpan> spans = new ArrayList<>();
                spans.add(new TextSpan("Side-view "));
                spans.add(new TextSpan("").entity("minecraft:pig", 0.7f).entityRotation(90, 0));
                spans.add(new TextSpan(" pig"));
                EmbersTextAPI.sendMessage(player, new ImmersiveMessage(spans, 100f).scale(3f).anchor(TextAnchor.MIDDLE));
            }

            // --- Group 6: Combinations & Queue (30-33) ---
            case 30 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(120f, "<grad from=FF0000 to=0000FF><wave><neon>Gradient + Wave + Neon</neon></wave></grad>")
                            .scale(2f)
                            .background(true)
                            .anchor(TextAnchor.MIDDLE));
            case 31 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(120f, "<font id=emberstextapi:metamorphous><rainbow><bounce>Font + Rainbow + Bounce</bounce></rainbow></font>")
                            .scale(2f)
                            .background(true)
                            .anchor(TextAnchor.MIDDLE));
            case 32 -> EmbersTextAPI.sendMessage(player,
                    ImmersiveMessage.fromMarkup(160f, "<font id=emberstextapi:norse><obfuscate mode=reveal speed=60><shake>You shall not pass!</shake></obfuscate></font>")
                            .scale(2.5f)
                            .background(true)
                            .backgroundGradient(new ImmersiveColor(0xFF220000), new ImmersiveColor(0xFF000022))
                            .fadeInTicks(15)
                            .fadeOutTicks(15)
                            .anchor(TextAnchor.MIDDLE));
            case 33 -> {
                List<List<ImmersiveMessage>> steps = new ArrayList<>();
                steps.add(List.of(
                        ImmersiveMessage.fromMarkup(80f, "<rainbow><bold>Quest Complete!</bold></rainbow>")
                                .scale(2.5f).background(true).anchor(TextAnchor.MIDDLE)
                                .fadeInTicks(10).fadeOutTicks(10)));
                steps.add(List.of(
                        ImmersiveMessage.fromMarkup(80f, "<type speed=40>You have slain the dragon...</type>")
                                .scale(1.5f).background(true).anchor(TextAnchor.MIDDLE)
                                .fadeInTicks(10).fadeOutTicks(10)));
                steps.add(List.of(
                        ImmersiveMessage.fromMarkup(100f, "<neon c=FFD700><wave>+ 1000 XP</wave></neon>")
                                .scale(2f).background(true).anchor(TextAnchor.MIDDLE)
                                .fadeInTicks(10).fadeOutTicks(10)));
                NetworkHelper.getInstance().sendQueue(player, "test_quest", steps);
            }
        }
    }
}
