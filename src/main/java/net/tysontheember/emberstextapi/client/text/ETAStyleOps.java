package net.tysontheember.emberstextapi.client.text;

import java.util.List;

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
        }
        return style;
    }

    public static void copyExtras(Style from, Style to) {
        if (!(from instanceof SpanStyleExtras source) || !(to instanceof SpanStyleExtras target)) {
            return;
        }

        target.eta$setSpanGraph(source.eta$getSpanGraph());
        target.eta$setSpanSignature(source.eta$getSpanSignature());

        List<SpanEffect> effects = source.eta$getActiveEffects();
        target.eta$setActiveEffects(effects);

        target.eta$setTypewriterTrack(source.eta$getTypewriterTrack());
        target.eta$setTypewriterIndex(source.eta$getTypewriterIndex());
    }
}
