package net.tysontheember.emberstextapi.attributes;

import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.markup.RNode;
import net.tysontheember.emberstextapi.overlay.LayoutRun;
import net.tysontheember.emberstextapi.overlay.OverlayQueue;

/**
 * Contract for runtime attribute handlers.
 */
public interface AttributeHandler {
    /**
     * @return canonical attribute/tag name.
     */
    String name();

    /**
     * Applies vanilla style fallback to the provided style.
     */
    Style applyVanilla(Style base, RNode.RSpan span, AttributeContext ctx);

    /**
     * Queues overlay rendering commands for advanced effects.
     */
    void queueOverlay(OverlayQueue queue, LayoutRun run, RNode.RSpan span, AttributeContext ctx);
}
