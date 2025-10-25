package net.tysontheember.emberstextapi.client.text;

import net.minecraft.network.chat.Style;

/**
 * Helper methods for interacting with {@link SpanStyleExtras} implementations.
 */
public final class ETAStyleOps {
    private ETAStyleOps() {
    }

    public static SpanGraph getGraph(Style style) {
        if (style instanceof SpanStyleExtras extras) {
            return extras.eta$getSpanGraph();
        }
        return null;
    }

    public static Style withGraph(Style style, SpanGraph graph) {
        if (style instanceof SpanStyleExtras extras) {
            extras.eta$setSpanGraph(graph);
            extras.eta$setSpanSignature(graph == null ? null : graph.getSignature());
        }
        return style;
    }

    public static Style withGraphMerged(Style style, SpanGraph parent, SpanGraph child) {
        if (!(style instanceof SpanStyleExtras)) {
            return style;
        }
        if (child != null) {
            return withGraph(style, child);
        }
        if (parent != null) {
            return withGraph(style, parent);
        }
        return withGraph(style, null);
    }

    public static void copyExtras(Style from, Style to) {
        if (!(from instanceof SpanStyleExtras source) || !(to instanceof SpanStyleExtras target)) {
            return;
        }

        target.eta$setSpanGraph(source.eta$getSpanGraph());
        target.eta$setSpanSignature(source.eta$getSpanSignature());

        target.eta$setActiveEffects(source.eta$getActiveEffects());
        target.eta$setTypewriterTrack(source.eta$getTypewriterTrack());
        target.eta$setTypewriterIndex(source.eta$getTypewriterIndex());
    }
}
