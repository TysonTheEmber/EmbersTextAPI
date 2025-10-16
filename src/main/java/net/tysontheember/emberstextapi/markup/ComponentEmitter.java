package net.tysontheember.emberstextapi.markup;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.attributes.TextAttributes;
import net.tysontheember.emberstextapi.overlay.LayoutRun;
import net.tysontheember.emberstextapi.overlay.Markers;

/**
 * Converts an {@link RNode} tree into a vanilla component hierarchy.
 */
public final class ComponentEmitter {
    private ComponentEmitter() {
    }

    public static MutableComponent toComponent(RNode node, AttributeContext ctx) {
        MutableComponent root = Component.empty();
        append(root, node, Style.EMPTY, ctx);
        return root;
    }

    private static void append(MutableComponent parent, RNode node, Style style, AttributeContext ctx) {
        if (node instanceof RNode.RText text) {
            if (text.text().isEmpty()) {
                return;
            }
            parent.append(Component.literal(text.text()).withStyle(style));
            return;
        }
        if (node instanceof RNode.RSpan span) {
            AttributeHandler handler = TextAttributes.get(span.tag());
            Style next = handler != null ? handler.applyVanilla(style, span, ctx) : style;
            MutableComponent container = Component.empty();
            for (RNode child : span.children()) {
                append(container, child, next, ctx);
            }
            if (handler != null && ctx.overlayEnabled() && ctx.overlayQueue() != null) {
                handler.queueOverlay(ctx.overlayQueue(), new LayoutRun(container, 0, 0, 1.0F), span, ctx);
            }
            if (handler != null && ctx.overlayEnabled()) {
                container.append(Markers.encode(span));
            }
            parent.append(container);
            return;
        }
    }
}
