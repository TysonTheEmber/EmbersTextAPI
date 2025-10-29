package net.tysontheember.emberstextapi.span;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the parsed output of a span-enabled string.
 */
public final class SpanDocument {
    private static final SpanDocument EMPTY = new SpanDocument("", Collections.singletonList(new TextRunNode("")), false);

    private final String rawText;
    private final List<SpanNode> children;
    private final boolean hasSpans;

    private SpanDocument(String rawText, List<SpanNode> children, boolean hasSpans) {
        this.rawText = rawText;
        this.children = Collections.unmodifiableList(new ArrayList<>(children));
        this.hasSpans = hasSpans;
    }

    public static SpanDocument empty() {
        return EMPTY;
    }

    public static SpanDocument of(String rawText, List<SpanNode> children, boolean hasSpans) {
        if (rawText == null || rawText.isEmpty()) {
            return EMPTY;
        }
        return new SpanDocument(rawText, children, hasSpans);
    }

    public String getRawText() {
        return rawText;
    }

    public List<SpanNode> getChildren() {
        return children;
    }

    public boolean hasSpans() {
        return hasSpans;
    }

    public boolean noSpans() {
        return !hasSpans;
    }

    public String flattenText() {
        StringBuilder builder = new StringBuilder();
        for (SpanNode child : children) {
            child.appendPlainText(builder);
        }
        return builder.toString();
    }
}
