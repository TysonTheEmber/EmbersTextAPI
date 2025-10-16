package net.tysontheember.emberstextapi.attributes.impl;

import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.MutableComponent;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.markup.RNode.RSpan;
import net.tysontheember.emberstextapi.overlay.LayoutRun;
import net.tysontheember.emberstextapi.overlay.OverlayQueue;

import java.util.Locale;

public final class GradientAttribute implements AttributeHandler {
    @Override
    public String name() {
        return "gradient";
    }

    @Override
    public void applyVanilla(MutableComponent component, RSpan span, AttributeContext ctx) {
        span.attrs().entrySet().stream()
            .filter(entry -> entry.getKey().equalsIgnoreCase("from") || entry.getKey().equalsIgnoreCase("value"))
            .findFirst()
            .map(entry -> parse(entry.getValue()))
            .ifPresent(color -> component.withStyle(style -> style.withColor(color)));
    }

    @Override
    public void queueOverlay(OverlayQueue queue, LayoutRun run, RSpan span, AttributeContext ctx) {
        queue.enqueue(span.tag(), span, run);
    }

    private TextColor parse(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1);
        }
        try {
            int rgb = (int) Long.parseLong(trimmed, 16);
            if (trimmed.length() > 6) {
                rgb &= 0xFFFFFF;
            }
            return TextColor.fromRgb(rgb);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
