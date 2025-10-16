package net.tysontheember.emberstextapi.attributes.impl;

import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.markup.RNode;
import net.tysontheember.emberstextapi.overlay.LayoutRun;
import net.tysontheember.emberstextapi.overlay.OverlayQueue;

/**
 * Shadow effect placeholder.
 */
public final class ShadowAttribute implements AttributeHandler {
    @Override
    public String name() {
        return "shadow";
    }

    @Override
    public Style applyVanilla(Style base, RNode.RSpan span, AttributeContext ctx) {
        return base;
    }

    @Override
    public void queueOverlay(OverlayQueue queue, LayoutRun run, RNode.RSpan span, AttributeContext ctx) {
        if (!ctx.overlayEnabled()) {
            return;
        }
        queue.add(new OverlayQueue.OverlayTask("shadow", run, span.attrs()));
    }
}
