package net.tysontheember.emberstextapi.attributes.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.markup.RNode;
import net.tysontheember.emberstextapi.overlay.LayoutRun;
import net.tysontheember.emberstextapi.overlay.OverlayQueue;

import javax.annotation.Nullable;

/**
 * Applies vanilla color styling.
 */
public final class ColorAttribute implements AttributeHandler {
    @Override
    public String name() {
        return "color";
    }

    @Override
    public Style applyVanilla(Style base, RNode.RSpan span, AttributeContext ctx) {
        String value = firstValue(span);
        if (value == null) {
            return base;
        }

        TextColor color = parseColor(value);
        if (color == null) {
            return base;
        }
        return base.withColor(color);
    }

    @Override
    public void queueOverlay(OverlayQueue queue, LayoutRun run, RNode.RSpan span, AttributeContext ctx) {
        // Pure vanilla fallback; overlay handled by gradient handler if necessary.
    }

    @Nullable
    private static String firstValue(RNode.RSpan span) {
        if (span.attrs().isEmpty()) {
            return null;
        }
        if (span.attrs().containsKey("value")) {
            return span.attrs().get("value");
        }
        return span.attrs().values().stream().findFirst().orElse(null);
    }

    @Nullable
    public static TextColor parseColor(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("#")) {
            try {
                int rgb = (int) Long.parseLong(trimmed.substring(1), 16);
                if (trimmed.length() <= 7) {
                    return TextColor.fromRgb(rgb);
                }
                return TextColor.fromRgb(rgb & 0xFFFFFF);
            } catch (NumberFormatException ignored) {
            }
            return null;
        }
        ChatFormatting vanilla = ChatFormatting.getByName(trimmed);
        if (vanilla != null && vanilla.getColor() != null) {
            return TextColor.fromRgb(vanilla.getColor());
        }
        return TextColor.parseColor(trimmed);
    }
}
