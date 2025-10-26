package net.tysontheember.emberstextapi.client.text;

import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.client.GlobalSwitches;

/**
 * Placeholder dispatcher used to route span-aware rendering during string decomposition.
 */
public final class SpanDispatcher {
    private SpanDispatcher() {
    }

    /**
     * Determines whether the supplied style currently carries span metadata.
     *
     * @param style the style to inspect
     * @return {@code true} when span extras are present
     */
    public static boolean hasSpans(Style style) {
        if (!GlobalSwitches.enabled() || !(style instanceof SpanStyleExtras extras)) {
            return false;
        }
        return extras.eta$getSpanGraph() != null;
    }

    /**
     * Returns the style to use for the given code point. For now this is a direct pass-through.
     *
     * @param base         the originating style
     * @param codePoint    the code point being processed
     * @param logicalIndex the logical index of the glyph
     * @return the style to use when emitting the glyph
     */
    public static Style styleForCodePoint(Style base, int codePoint, int logicalIndex) {
        return base;
    }
}
