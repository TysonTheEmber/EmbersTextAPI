package net.tysontheember.emberstextapi.client.text;

/**
 * Placeholder markup adapter responsible for creating span graphs.
 */
public final class MarkupAdapter {
    private MarkupAdapter() {
    }

    /**
     * Parses raw text into a span graph stub.
     *
     * @param input text to parse
     * @return empty span graph stub
     */
    public static SpanGraph parse(String input) {
        return SpanGraph.empty();
    }
}
