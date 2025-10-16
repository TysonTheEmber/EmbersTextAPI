package net.tysontheember.emberstextapi.attributes.impl;

import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.markup.RNode;
import net.tysontheember.emberstextapi.overlay.LayoutRun;
import net.tysontheember.emberstextapi.overlay.OverlayQueue;

/**
 * Enables strikethrough in vanilla fallback.
 */
public final class StrikethroughAttribute implements AttributeHandler {
    @Override
    public String name() {
        return "strikethrough";
    }

    @Override
    public Style applyVanilla(Style base, RNode.RSpan span, AttributeContext ctx) {
        return base.withStrikethrough(true);
    }

    @Override
    public void queueOverlay(OverlayQueue queue, LayoutRun run, RNode.RSpan span, AttributeContext ctx) {
        // No overlay action required.
    }
}
