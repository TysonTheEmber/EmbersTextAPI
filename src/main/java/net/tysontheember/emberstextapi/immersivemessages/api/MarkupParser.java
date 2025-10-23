package net.tysontheember.emberstextapi.immersivemessages.api;

import net.tysontheember.emberstextapi.client.markup.MarkupService;
import net.tysontheember.emberstextapi.client.spans.SpanBundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Backwards-compatible facade for parsing immersive text markup.
 */
public class MarkupParser {

    private MarkupParser() {
    }

    public static List<TextSpan> parse(String markup) {
        var parsed = MarkupService.getInstance().parse(markup, Locale.getDefault(), true);
        if (parsed.isPresent()) {
            return new ArrayList<>(parsed.get().spans());
        }
        if (markup == null || markup.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(fromPlainText(markup));
    }

    public static String toPlainText(List<TextSpan> spans) {
        if (spans == null || spans.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (TextSpan span : spans) {
            result.append(span.getContent());
        }
        return result.toString();
    }

    public static List<TextSpan> fromPlainText(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new TextSpan(text));
    }
}
