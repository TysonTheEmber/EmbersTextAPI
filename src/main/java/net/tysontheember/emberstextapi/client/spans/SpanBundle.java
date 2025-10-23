package net.tysontheember.emberstextapi.client.spans;

import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;

import java.util.Collections;
import java.util.List;

/**
 * Immutable container for parsed markup spans.
 */
public final class SpanBundle {
    private final String plainText;
    private final List<TextSpan> spans;
    private final List<String> warnings;
    private final List<String> errors;

    public SpanBundle(String plainText, List<TextSpan> spans, List<String> warnings, List<String> errors) {
        this.plainText = plainText != null ? plainText : "";
        this.spans = Collections.unmodifiableList(List.copyOf(spans != null ? spans : List.of()));
        this.warnings = Collections.unmodifiableList(List.copyOf(warnings != null ? warnings : List.of()));
        this.errors = Collections.unmodifiableList(List.copyOf(errors != null ? errors : List.of()));
    }

    public String plainText() {
        return plainText;
    }

    public List<TextSpan> spans() {
        return spans;
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
