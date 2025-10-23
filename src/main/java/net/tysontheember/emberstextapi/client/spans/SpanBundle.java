package net.tysontheember.emberstextapi.client.spans;

import java.util.Collections;
import java.util.List;

/**
 * Immutable container for parsed markup spans.
 */
public final class SpanBundle {
    private final String plainText;
    private final List<TextSpanView> spans;
    private final List<net.tysontheember.emberstextapi.immersivemessages.api.TextSpan> legacySpans;
    private final List<String> warnings;
    private final List<String> errors;

    public SpanBundle(String plainText,
                      List<TextSpanView> spans,
                      List<net.tysontheember.emberstextapi.immersivemessages.api.TextSpan> legacySpans,
                      List<String> warnings,
                      List<String> errors) {
        this.plainText = plainText != null ? plainText : "";
        this.spans = Collections.unmodifiableList(List.copyOf(spans != null ? spans : List.of()));
        this.legacySpans = Collections.unmodifiableList(List.copyOf(legacySpans != null ? legacySpans : List.of()));
        this.warnings = Collections.unmodifiableList(List.copyOf(warnings != null ? warnings : List.of()));
        this.errors = Collections.unmodifiableList(List.copyOf(errors != null ? errors : List.of()));
    }

    public String plainText() {
        return plainText;
    }

    public List<TextSpanView> spans() {
        return spans;
    }

    /**
     * Temporary access to the mutable {@link net.tysontheember.emberstextapi.immersivemessages.api.TextSpan}
     * objects used by existing immersive message renderers.
     */
    public List<net.tysontheember.emberstextapi.immersivemessages.api.TextSpan> legacySpans() {
        return legacySpans;
    }

    public List<String> warnings() {
        return warnings;
    }

    public List<String> errors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }
}
