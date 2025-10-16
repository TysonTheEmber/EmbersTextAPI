package net.tysontheember.emberstextapi.attributes.impl;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.markup.RNode;
import net.tysontheember.emberstextapi.overlay.LayoutRun;
import net.tysontheember.emberstextapi.overlay.OverlayQueue;

import javax.annotation.Nullable;

/**
 * Provides gradient coloring (vanilla fallback uses the starting color).
 */
public final class GradientAttribute implements AttributeHandler {
    @Override
    public String name() {
        return "gradient";
    }

    @Override
    public Style applyVanilla(Style base, RNode.RSpan span, AttributeContext ctx) {
        String from = span.attrs().getOrDefault("from", span.attrs().get("value"));
        TextColor color = ColorAttribute.parseColor(from);
        if (color == null) {
            return base;
        }
        return base.withColor(color);
    }

    @Override
    public void queueOverlay(OverlayQueue queue, LayoutRun run, RNode.RSpan span, AttributeContext ctx) {
        if (!ctx.overlayEnabled()) {
            return;
        }
        queue.add(new OverlayQueue.OverlayTask("gradient", run, span.attrs()));
    }
}
