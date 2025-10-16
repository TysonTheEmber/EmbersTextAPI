package net.tysontheember.emberstextapi.attributes.impl;

import net.minecraft.network.chat.MutableComponent;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.markup.RNode.RSpan;
import net.tysontheember.emberstextapi.overlay.LayoutRun;
import net.tysontheember.emberstextapi.overlay.OverlayQueue;

public final class BgAttribute implements AttributeHandler {
    @Override
    public String name() {
        return "bg";
    }

    @Override
    public void applyVanilla(MutableComponent component, RSpan span, AttributeContext ctx) {
    }

    @Override
    public void queueOverlay(OverlayQueue queue, LayoutRun run, RSpan span, AttributeContext ctx) {
        queue.enqueue(span.tag(), span, run);
    }

    @Override
    public boolean isOverlayOnly() {
        return true;
    }
}
