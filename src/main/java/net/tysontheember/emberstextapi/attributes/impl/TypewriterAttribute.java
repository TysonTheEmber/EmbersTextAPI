package net.tysontheember.emberstextapi.attributes.impl;

import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.markup.RNode;
import net.tysontheember.emberstextapi.overlay.LayoutRun;
import net.tysontheember.emberstextapi.overlay.OverlayQueue;

/**
 * Typewriter animation placeholder.
 */
public final class TypewriterAttribute implements AttributeHandler {
    @Override
    public String name() {
        return "typewriter";
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
        queue.add(new OverlayQueue.OverlayTask("typewriter", run, span.attrs()));
    }
}
