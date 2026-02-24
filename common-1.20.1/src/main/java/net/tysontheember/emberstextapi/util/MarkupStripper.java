package net.tysontheember.emberstextapi.util;

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
     * Check if a string contains any markup tags.
     */
    public static boolean containsMarkup(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return MARKUP_TAG_PATTERN.matcher(input).find();
    }
}
