package net.tysontheember.emberstextapi.attributes;

import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.markup.RSpan;
import net.tysontheember.emberstextapi.overlay.LayoutRun;
import net.tysontheember.emberstextapi.overlay.OverlayBatches;

/**
 * Service provider used to add new markup attributes.
 */
public interface AttributeHandler {
    String name();

    Style applyVanilla(Style base, RSpan span, AttributeContext ctx);

    default void queueOverlay(OverlayBatches queue, LayoutRun run, RSpan span, AttributeContext ctx) {
        ctx.markOverlay(span);
        queue.add(span);
    }
}
