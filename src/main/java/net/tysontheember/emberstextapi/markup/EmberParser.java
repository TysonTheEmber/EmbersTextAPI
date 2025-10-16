package net.tysontheember.emberstextapi.markup;

/**
 * Public entry point for parsing Ember Markup strings.
 */
public final class EmberParser {
    private EmberParser() {
    }

    /**
     * Parses the supplied markup string into an AST.
     *
     * @param input markup text, may be {@code null}
     * @return parsed node tree (never {@code null})
     */
    public static RNode parse(String input) {
        return MarkupParser.parse(input);
    }
}
