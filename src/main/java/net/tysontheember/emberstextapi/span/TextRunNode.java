package net.tysontheember.emberstextapi.span;

import java.util.Objects;

/**
 * Represents a contiguous run of text without any nested spans.
 */
public final class TextRunNode extends SpanNode {
    private final String text;

    public TextRunNode(String text) {
        this.text = text == null ? "" : text;
    }

    public String getText() {
        return text;
    }

    @Override
    void appendPlainText(StringBuilder builder) {
        builder.append(text);
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TextRunNode other)) {
            return false;
        }
        return Objects.equals(text, other.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }
}
