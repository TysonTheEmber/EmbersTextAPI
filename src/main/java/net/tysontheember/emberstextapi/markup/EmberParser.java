package net.tysontheember.emberstextapi.markup;

/**
 * Public facade around the internal {@link MarkupParser} implementation.
 */
public final class EmberParser {
    private EmberParser() {
    }

    /**
     * Parses Ember Markup using default options.
     *
     * @param input markup string to parse
     * @return root span containing parsed nodes
     */
    public static RSpan parse(String input) {
        return MarkupParser.parse(input, MarkupParser.ParserConfig.defaults());
    }

    /**
     * Parses Ember Markup with custom configuration.
     *
     * @param input markup string to parse
     * @param stripUnknown if {@code true}, unknown tags are stripped and their
     *                     children are inlined into the parent
     * @return root span representing parsed nodes
     */
    public static RSpan parse(String input, boolean stripUnknown) {
        return MarkupParser.parse(input, new MarkupParser.ParserConfig(stripUnknown));
    }
}
