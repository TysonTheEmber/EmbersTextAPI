package net.tysontheember.emberstextapi.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Aggregates status information for the debug overlay. Later commits will extend this with
 * span-specific diagnostics; for now it surfaces configuration state to validate toggles and
 * hotkeys.
 */
public final class DebugOverlay {
    public static final int MAX_LEVEL = 3;

    private DebugOverlay() {
    }

    public static boolean shouldRender() {
        return DebugFlags.isOverlayEnabled();
    }

    public static int getLevel() {
        return DebugFlags.getOverlayLevel();
    }

    public static int cycleLevel() {
        int next = (DebugFlags.getOverlayLevel() + 1) % (MAX_LEVEL + 1);
        DebugFlags.setOverlayLevel(next);
        return next;
    }

    public static List<Component> gatherOverlayLines() {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("[Embers Text API Debug]").withStyle(ChatFormatting.GOLD));

        lines.add(statusLine("Debug", DebugFlags.isDebugEnabled()));
        lines.add(statusLine("Overlay", DebugFlags.isOverlayFlagEnabled())
                .append(Component.literal(" (level " + DebugFlags.getOverlayLevel() + ")")
                        .withStyle(ChatFormatting.GRAY)));
        lines.add(statusLine("Performance timers", DebugFlags.isPerfFlagEnabled()));
        lines.add(statusLine("Fail-safe", DebugFlags.isFailSafeOnError()));

        if (getLevel() >= 1) {
            lines.add(Component.literal("Trace channels:").withStyle(ChatFormatting.AQUA));
            for (DebugFlags.TraceChannel channel : DebugFlags.TraceChannel.values()) {
                lines.add(Component.literal(" - " + channel.name().toLowerCase(Locale.ROOT) + ": ")
                        .append(booleanText(DebugFlags.getTraceFlag(channel))));
            }
        }

        if (getLevel() >= 2) {
            lines.add(Component.literal("Span everywhere: ")
                    .append(booleanText(DebugFlags.isSpanEverywhere())));
            lines.add(Component.literal("Effects version: " + DebugFlags.getEffectsVersion())
                    .withStyle(ChatFormatting.GRAY));
        }

        if (getLevel() >= 3) {
            lines.add(Component.literal("Overlay detail level maxed").withStyle(ChatFormatting.DARK_GRAY));
        }

        return lines;
    }

    private static MutableComponent statusLine(String name, boolean enabled) {
        return Component.literal(name + ": ").withStyle(ChatFormatting.WHITE)
                .append(booleanText(enabled));
    }

    private static MutableComponent booleanText(boolean value) {
        return Component.literal(value ? "ON" : "OFF")
                .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED);
    }
}
