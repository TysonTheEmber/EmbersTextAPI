package net.tysontheember.emberstextapi.span;

/**
 * Base type for nodes in a parsed span document.
 */
public abstract class SpanNode {
    SpanNode() {
    }

    /**
     * Appends the plain-text representation of the node to the supplied builder.
     */
    abstract void appendPlainText(StringBuilder builder);
}
