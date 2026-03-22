package net.tysontheember.emberstextapi.util;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility for stripping markup tags from chat messages.
 * Used to remove effect tags for players who are not allowed to use markup.
 */
public final class MarkupStripper {

    private static final Pattern MARKUP_TAG_PATTERN = Pattern.compile("</?[a-zA-Z][^>]*>");

    private MarkupStripper() {
    }

    /**
     * Strip all markup tags from a string, leaving only plain text content.
     *
     * @param input the input string possibly containing markup tags
     * @return the string with all markup tags removed
     */
    public static String stripMarkup(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return MARKUP_TAG_PATTERN.matcher(input).replaceAll("");
    }

    /**
     * Strip only specific named tags from a string, leaving their content and all other tags intact.
     * For example, stripTags("&lt;glitch&gt;foo&lt;/glitch&gt; &lt;rainbow&gt;bar&lt;/rainbow&gt;", ["glitch"])
     * returns "foo &lt;rainbow&gt;bar&lt;/rainbow&gt;".
     *
     * @param input the input string
     * @param tagNames the tag names to strip (case-insensitive)
     * @return the string with only the specified tags removed
     */
    public static String stripTags(String input, List<String> tagNames) {
        if (input == null || input.isEmpty() || tagNames == null || tagNames.isEmpty()) {
            return input;
        }
        String result = input;
        for (String tag : tagNames) {
            String escaped = Pattern.quote(tag);
            // Strip opening tags (with optional attributes) and closing tags
            result = result.replaceAll("(?i)<" + escaped + "(\\s[^>]*)?>", "");
            result = result.replaceAll("(?i)</" + escaped + "\\s*>", "");
        }
        return result;
    }

    /**
     * Check if a string contains any markup tags.
     */
    public static boolean containsMarkup(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return MARKUP_TAG_PATTERN.matcher(input).find();
    }
}
