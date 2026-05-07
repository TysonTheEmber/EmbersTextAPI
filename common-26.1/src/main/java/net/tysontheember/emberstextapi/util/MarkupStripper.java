package net.tysontheember.emberstextapi.util;

import java.util.List;
import java.util.regex.Pattern;

public final class MarkupStripper {

    private static final Pattern MARKUP_TAG_PATTERN = Pattern.compile("</?[a-zA-Z][^>]*>");

    private MarkupStripper() {
    }

    public static String stripMarkup(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return MARKUP_TAG_PATTERN.matcher(input).replaceAll("");
    }

    public static String stripTags(String input, List<String> tagNames) {
        if (input == null || input.isEmpty() || tagNames == null || tagNames.isEmpty()) {
            return input;
        }
        String result = input;
        for (String tag : tagNames) {
            String escaped = Pattern.quote(tag);

            result = result.replaceAll("(?i)<" + escaped + "(\\s[^>]*)?>", "");
            result = result.replaceAll("(?i)</" + escaped + "\\s*>", "");
        }
        return result;
    }

    public static boolean containsMarkup(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return MARKUP_TAG_PATTERN.matcher(input).find();
    }
}
