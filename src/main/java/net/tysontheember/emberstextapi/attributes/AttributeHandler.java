package net.tysontheember.emberstextapi.attributes;

import net.minecraft.network.chat.MutableComponent;
import net.tysontheember.emberstextapi.markup.RNode.RSpan;
import net.tysontheember.emberstextapi.overlay.LayoutRun;
import net.tysontheember.emberstextapi.overlay.OverlayQueue;

/**
 * Service provider used to translate markup spans into vanilla style mutations
 * and overlay draw commands.
 */
public interface AttributeHandler {
    /**
     * @return attribute/tag name handled by this provider.
     */
    String name();

    /**
     * Apply best-effort vanilla styling hints.
     */
    void applyVanilla(MutableComponent component, RSpan span, AttributeContext ctx);

    /**
     * Enqueue overlay work for advanced rendering.
     */
    default void queueOverlay(OverlayQueue queue, LayoutRun run, RSpan span, AttributeContext ctx) {
    }

    /**
     * @return true if the tag is overlay-only (no vanilla fallback).
     */
    default boolean isOverlayOnly() {
        return false;
    }
}
