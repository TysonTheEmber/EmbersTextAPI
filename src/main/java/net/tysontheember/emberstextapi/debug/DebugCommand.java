package net.tysontheember.emberstextapi.debug;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.EmbersTextAPI;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Implements the <code>/eta debug</code> command tree used to manage debug tooling during
 * development. The command handlers delegate to shared toggle helpers so hotkeys and tests can
 * reuse the same code paths.
 */
@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID)
public final class DebugCommand {
    private static final String SOURCE_COMMAND = "command";

    private DebugCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("eta")
                        .requires(source -> source.hasPermission(2))
                        .then(buildDebugTree()));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> buildDebugTree() {
        return Commands.literal("debug")
                .executes(ctx -> sendStatus(ctx.getSource()))
                .then(toggleSubcommand())
                .then(traceSubcommand())
                .then(overlaySubcommand())
                .then(perfSubcommand())
                .then(failSafeSubcommand())
                .then(dumpSubcommand());
    }

    private static ArgumentBuilder<CommandSourceStack, ?> toggleSubcommand() {
        return Commands.literal("toggle")
                .executes(ctx -> setDebug(ctx.getSource(), !DebugFlags.isDebugEnabled()))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> setDebug(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled"))));
    }

    private static int setDebug(CommandSourceStack source, boolean enabled) {
        applyDebugToggle(enabled, SOURCE_COMMAND);
        sendFeedback(source, Component.literal("Debug mode ").append(booleanText(enabled)));
        return Command.SINGLE_SUCCESS;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> traceSubcommand() {
        return Commands.literal("trace")
                .then(Commands.argument("channel", StringArgumentType.word())
                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(traceChannelSuggestions(), builder))
                        .executes(ctx -> {
                            DebugFlags.TraceChannel channel = parseTraceChannel(ctx, "channel");
                            return toggleTrace(ctx, channel, !DebugFlags.getTraceFlag(channel));
                        })
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    DebugFlags.TraceChannel channel = parseTraceChannel(ctx, "channel");
                                    return toggleTrace(ctx, channel, BoolArgumentType.getBool(ctx, "enabled"));
                                })));
    }

    private static int toggleTrace(CommandContext<CommandSourceStack> context, DebugFlags.TraceChannel channel, boolean enabled)
            throws CommandSyntaxException {
        applyTraceToggle(channel, enabled, SOURCE_COMMAND);
        sendFeedback(context.getSource(), Component.literal("Trace " + channel.name().toLowerCase(Locale.ROOT) + " ")
                .append(booleanText(enabled)));
        return Command.SINGLE_SUCCESS;
    }

    private static DebugFlags.TraceChannel parseTraceChannel(CommandContext<CommandSourceStack> context, String name)
            throws CommandSyntaxException {
        String raw = StringArgumentType.getString(context, name);
        try {
            return DebugFlags.TraceChannel.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create();
        }
    }

    private static String[] traceChannelSuggestions() {
        return Arrays.stream(DebugFlags.TraceChannel.values())
                .map(channel -> channel.name().toLowerCase(Locale.ROOT))
                .toArray(String[]::new);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> overlaySubcommand() {
        return Commands.literal("overlay")
                .executes(ctx -> showOverlayStatus(ctx.getSource()))
                .then(Commands.literal("toggle")
                        .executes(ctx -> setOverlay(ctx.getSource(), !DebugFlags.isOverlayFlagEnabled()))
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(ctx -> setOverlay(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled")))))
                .then(Commands.literal("level")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, DebugOverlay.MAX_LEVEL))
                                .executes(ctx -> setOverlayLevel(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "value")))));
    }

    private static int showOverlayStatus(CommandSourceStack source) {
        sendFeedback(source, Component.literal("Overlay ")
                .append(booleanText(DebugFlags.isOverlayFlagEnabled()))
                .append(Component.literal(" level " + DebugFlags.getOverlayLevel()).withStyle(ChatFormatting.GRAY)));
        return Command.SINGLE_SUCCESS;
    }

    private static int setOverlay(CommandSourceStack source, boolean enabled) {
        applyOverlayToggle(enabled, SOURCE_COMMAND);
        sendFeedback(source, Component.literal("Overlay ").append(booleanText(enabled))
                .append(Component.literal(" level " + DebugFlags.getOverlayLevel()).withStyle(ChatFormatting.GRAY)));
        return Command.SINGLE_SUCCESS;
    }

    private static int setOverlayLevel(CommandSourceStack source, int level) {
        applyOverlayLevel(level, SOURCE_COMMAND);
        sendFeedback(source, Component.literal("Overlay level set to " + DebugFlags.getOverlayLevel())
                .withStyle(ChatFormatting.GRAY));
        return Command.SINGLE_SUCCESS;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> perfSubcommand() {
        return Commands.literal("perf")
                .executes(ctx -> setPerf(ctx.getSource(), !DebugFlags.isPerfFlagEnabled()))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> setPerf(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled"))));
    }

    private static int setPerf(CommandSourceStack source, boolean enabled) {
        applyPerfToggle(enabled, SOURCE_COMMAND);
        sendFeedback(source, Component.literal("Performance timers ").append(booleanText(enabled)));
        return Command.SINGLE_SUCCESS;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> failSafeSubcommand() {
        return Commands.literal("failsafe")
                .executes(ctx -> setFailSafe(ctx.getSource(), !DebugFlags.isFailSafeOnError()))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> setFailSafe(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled"))));
    }

    private static int setFailSafe(CommandSourceStack source, boolean enabled) {
        applyFailSafeToggle(enabled, SOURCE_COMMAND);
        sendFeedback(source, Component.literal("Fail-safe ").append(booleanText(enabled)));
        return Command.SINGLE_SUCCESS;
    }

    private static ArgumentBuilder<CommandSourceStack, ?> dumpSubcommand() {
        return Commands.literal("dump").executes(ctx -> sendDump(ctx.getSource()));
    }

    private static int sendDump(CommandSourceStack source) {
        requestDump(SOURCE_COMMAND);
        MutableComponent message = Component.literal("Debug: ").append(booleanText(DebugFlags.isDebugEnabled()))
                .append(Component.literal("\nOverlay: ").append(booleanText(DebugFlags.isOverlayFlagEnabled()))
                        .append(Component.literal(" level " + DebugFlags.getOverlayLevel()).withStyle(ChatFormatting.GRAY)))
                .append(Component.literal("\nPerformance: ").append(booleanText(DebugFlags.isPerfFlagEnabled())))
                .append(Component.literal("\nFail-safe: ").append(booleanText(DebugFlags.isFailSafeOnError())));
        sendFeedback(source, message);
        return Command.SINGLE_SUCCESS;
    }

    private static int sendStatus(CommandSourceStack source) {
        MutableComponent message = Component.literal("Embers Text API debug is ")
                .append(booleanText(DebugFlags.isDebugEnabled()))
                .append(Component.literal(" (overlay level " + DebugFlags.getOverlayLevel() + ")")
                        .withStyle(ChatFormatting.GRAY));
        sendFeedback(source, message);
        return Command.SINGLE_SUCCESS;
    }

    private static void sendFeedback(CommandSourceStack source, Component message) {
        Supplier<Component> supplier = () -> message;
        source.sendSuccess(supplier, true);
    }

    static void applyDebugToggle(boolean enabled, String source) {
        DebugFlags.setDebugEnabled(enabled);
        DebugEnvironment.getEventBus().post(DebugEvents.debugModeChanged(enabled, source));
    }

    static void applyTraceToggle(DebugFlags.TraceChannel channel, boolean enabled, String source) {
        DebugFlags.setTraceFlag(channel, enabled);
        DebugEnvironment.getEventBus().post(DebugEvents.traceChannelToggled(channel, enabled, source));
    }

    static void applyOverlayToggle(boolean visible, String source) {
        if (visible && !DebugFlags.isDebugEnabled()) {
            applyDebugToggle(true, source);
        }
        DebugFlags.setOverlayFlag(visible);
        DebugEnvironment.getEventBus().post(DebugEvents.overlayVisibilityChanged(visible, source));
    }

    static void applyOverlayLevel(int level, String source) {
        DebugFlags.setOverlayLevel(level);
        DebugEnvironment.getEventBus().post(DebugEvents.overlayLevelChanged(DebugFlags.getOverlayLevel(), source));
    }

    static void applyPerfToggle(boolean enabled, String source) {
        DebugFlags.setPerfEnabled(enabled);
        DebugEnvironment.getEventBus().post(DebugEvents.perfModeChanged(enabled, source));
    }

    static void applyFailSafeToggle(boolean enabled, String source) {
        DebugFlags.setFailSafeOnError(enabled);
        DebugEnvironment.getEventBus().post(DebugEvents.failSafeModeChanged(enabled, source));
    }

    static void requestDump(String source) {
        DebugEnvironment.getEventBus().post(DebugEvents.debugDumpRequested(source));
    }

    private static MutableComponent booleanText(boolean value) {
        return Component.literal(value ? "ON" : "OFF")
                .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED);
    }
}
