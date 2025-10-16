package net.tysontheember.emberstextapi.markup;

import static net.tysontheember.emberstextapi.markup.RNode.RSpan;

/**
 * Public facade over the internal {@link MarkupParser}.  Exposed for testing
 * and for other mods to reuse the parsing logic without depending on the
 * higher level APIs.
 */
public final class EmberParser {
    private EmberParser() {
    }

    /**
     * Parses a markup string into a root {@link RSpan}.
     *
     * @param input markup text
     * @return root span containing the parsed tree
     */
    public static RSpan parse(String input) {
        return MarkupParser.parse(input);
    }
}
