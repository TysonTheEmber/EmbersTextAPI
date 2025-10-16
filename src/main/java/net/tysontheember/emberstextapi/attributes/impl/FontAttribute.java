package net.tysontheember.emberstextapi.attributes.impl;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.markup.RNode;
import net.tysontheember.emberstextapi.overlay.LayoutRun;
import net.tysontheember.emberstextapi.overlay.OverlayQueue;

/**
 * Applies a custom font to the style.
 */
public final class FontAttribute implements AttributeHandler {
    @Override
    public String name() {
        return "font";
    }

    @Override
    public Style applyVanilla(Style base, RNode.RSpan span, AttributeContext ctx) {
        String value = span.attrs().get("value");
        if (value == null || value.isEmpty()) {
            return base;
        }
        try {
            return base.withFont(new ResourceLocation(value));
        } catch (IllegalArgumentException ignored) {
            return base;
        }
    }

    @Override
    public void queueOverlay(OverlayQueue queue, LayoutRun run, RNode.RSpan span, AttributeContext ctx) {
        // Font change is purely vanilla.
    }
}
