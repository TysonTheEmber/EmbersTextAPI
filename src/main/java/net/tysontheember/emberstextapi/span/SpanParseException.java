package net.tysontheember.emberstextapi.span;

/**
 * Checked exception used to indicate parse failures. The parser recovers by treating the
 * offending token as literal text whenever possible.
 */
public final class SpanParseException extends Exception {
    private final int index;

    public SpanParseException(String message, int index) {
        super(message);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
