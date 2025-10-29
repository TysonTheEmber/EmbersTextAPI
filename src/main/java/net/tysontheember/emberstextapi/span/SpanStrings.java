package net.tysontheember.emberstextapi.span;

import java.util.Locale;

/**
 * Utility helpers for working with span markup strings.
 */
public final class SpanStrings {
    private SpanStrings() {
    }

    public static boolean noSpans(String text) {
        if (text == null || text.isEmpty()) {
            return true;
        }
        int lt = text.indexOf('<');
        if (lt >= 0) {
            int gt = text.indexOf('>', lt + 1);
            if (gt >= 0) {
                return false;
            }
        }
        int amp = text.indexOf('&');
        if (amp >= 0) {
            int semi = text.indexOf(';', amp + 1);
            if (semi > amp) {
                String entity = text.substring(amp + 1, semi).toLowerCase(Locale.ROOT);
                if ("lt".equals(entity) || "gt".equals(entity) || "amp".equals(entity)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String decodeEntities(String text) {
        if (text == null || text.indexOf('&') < 0) {
            return text;
        }
        StringBuilder builder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '&') {
                int semi = text.indexOf(';', i + 1);
                if (semi > 0) {
                    String entity = text.substring(i + 1, semi).toLowerCase(Locale.ROOT);
                    switch (entity) {
                        case "lt":
                            builder.append('<');
                            i = semi;
                            continue;
                        case "gt":
                            builder.append('>');
                            i = semi;
                            continue;
                        case "amp":
                            builder.append('&');
                            i = semi;
                            continue;
                        default:
                            break;
                    }
                }
            }
            builder.append(c);
        }
        return builder.toString();
    }
}
