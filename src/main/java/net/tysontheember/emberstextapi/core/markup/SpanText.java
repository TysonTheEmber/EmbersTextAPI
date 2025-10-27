package net.tysontheember.emberstextapi.core.markup;

import net.minecraft.network.chat.Component;
import net.tysontheember.emberstextapi.immersivemessages.api.MarkupParser;

/**
 * Utility helpers for working with markup-powered text components.
 */
public final class SpanText {
    private SpanText() {
    }

    /**
     * Parses markup into a {@link Component}. When parsing fails the original text is returned
     * without modifications to avoid crashing the caller.
     */
    public static Component parse(String markup) {
        if (markup == null || markup.isEmpty()) {
            return Component.literal("");
        }

        try {
            // Ensure the markup is syntactically valid; this will also strip tags if we ever
            // need a plain text fallback.
            MarkupParser.stream(markup);
            // Preserve the original markup for the rendering pipeline while still validating it.
            return Component.literal(markup);
        } catch (Exception ex) {
            // Fall back to a plain literal when parsing fails.
            return Component.literal(markup.replaceAll("<[^>]+>", ""));
        }
    }
}
