package net.tysontheember.emberstextapi.text;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Parses lightweight markup tags into {@link AttributedText} instances.
 */
public final class TagParser {
    private TagParser() {
    }

    record AttributeRange(int start, int end, Attribute attribute) {}

    public static AttributedText parse(String input, BiConsumer<String, Throwable> warningSink) {
        if (input == null || input.isEmpty()) {
            return AttributedText.of("");
        }
        StringBuilder output = new StringBuilder(input.length());
        List<AttributeRange> ranges = new ArrayList<>();
        List<ActiveTag> stack = new ArrayList<>();

        int index = 0;
        while (index < input.length()) {
            char c = input.charAt(index);
            if (c == '\\' && index + 1 < input.length() && input.charAt(index + 1) == '<') {
                output.append('<');
                index += 2;
                continue;
            }
            if (c == '&' && input.regionMatches(true, index, "&lt;", 0, 4)) {
                output.append('<');
                index += 4;
                continue;
            }
            if (c == '<') {
                int end = findTagEnd(input, index + 1);
                if (end == -1) {
                    output.append(c);
                    index++;
                    continue;
                }
                String inside = input.substring(index + 1, end);
                if (inside.isEmpty()) {
                    output.append(c);
                    index++;
                    continue;
                }
                boolean closing = inside.startsWith("/");
                boolean selfClosing = !closing && inside.trim().endsWith("/");
                if (closing) {
                    String name = inside.substring(1).trim();
                    if (!closeTag(stack, name, output.length(), ranges, warningSink)) {
                        output.append('<').append(inside).append('>');
                    }
                } else {
                    if (selfClosing) {
                        inside = inside.substring(0, inside.lastIndexOf('/'));
                    }
                    TagInfo info = parseTagInfo(inside, warningSink);
                    if (info == null) {
                        output.append('<').append(inside);
                        if (selfClosing) {
                            output.append('/');
                        }
                        output.append('>');
                    } else if (selfClosing) {
                        ranges.add(new AttributeRange(output.length(), output.length(), info.attribute()));
                    } else {
                        stack.add(new ActiveTag(output.length(), info));
                    }
                }
                index = end + 1;
                continue;
            }
            output.append(c);
            index++;
        }

        if (!stack.isEmpty()) {
            for (ActiveTag tag : stack) {
                if (warningSink != null) {
                    warningSink.accept("Unclosed tag <" + tag.info().name() + ">", null);
                }
            }
        }

        AttributedText attributed = new AttributedText(output.toString());
        for (AttributeRange range : ranges) {
            if (range.start < range.end) {
                attributed.apply(range.attribute().id(), range.attribute().params(), range.start, range.end);
            }
        }
        return attributed;
    }

    private static boolean closeTag(List<ActiveTag> stack, String name, int end, List<AttributeRange> ranges, BiConsumer<String, Throwable> warningSink) {
        if (stack.isEmpty()) {
            if (warningSink != null) {
                warningSink.accept("Encountered closing tag </" + name + "> without matching open", null);
            }
            return false;
        }
        ActiveTag active = stack.get(stack.size() - 1);
        if (!active.info().name().equalsIgnoreCase(name)) {
            if (warningSink != null) {
                warningSink.accept("Mismatched closing tag </" + name + "> expected </" + active.info().name() + ">", null);
            }
            return false;
        }
        stack.remove(stack.size() - 1);
        if (active.start() < end) {
            ranges.add(new AttributeRange(active.start(), end, active.info().attribute()));
        }
        return true;
    }

    private static TagInfo parseTagInfo(String inside, BiConsumer<String, Throwable> warningSink) {
        String trimmed = inside.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        int pos = 0;
        int len = trimmed.length();
        String name = readToken(trimmed, len, pos);
        if (name == null) {
            return null;
        }
        pos += name.length();
        Map<String, Object> params = new LinkedHashMap<>();
        while (pos < len) {
            while (pos < len && Character.isWhitespace(trimmed.charAt(pos))) {
                pos++;
            }
            if (pos >= len) break;
            int start = pos;
            while (pos < len && !Character.isWhitespace(trimmed.charAt(pos)) && trimmed.charAt(pos) != '=') {
                pos++;
            }
            String key = trimmed.substring(start, pos);
            if (key.isEmpty()) {
                pos++;
                continue;
            }
            while (pos < len && Character.isWhitespace(trimmed.charAt(pos))) {
                pos++;
            }
            Object value = Boolean.TRUE;
            if (pos < len && trimmed.charAt(pos) == '=') {
                pos++;
                while (pos < len && Character.isWhitespace(trimmed.charAt(pos))) {
                    pos++;
                }
                if (pos < len) {
                    ParseValue parsed = readValue(trimmed, pos);
                    value = parsed.value();
                    pos = parsed.next();
                }
            }
            params.put(key.toLowerCase(Locale.ROOT), value);
        }
        ResourceLocation id = resolveId(name);
        return new TagInfo(name, new Attribute(id, Params.of(params)));
    }

    private static String readToken(String text, int len, int pos) {
        int start = pos;
        while (pos < len && !Character.isWhitespace(text.charAt(pos))) {
            pos++;
        }
        if (start == pos) {
            return null;
        }
        return text.substring(start, pos);
    }

    private static ParseValue readValue(String text, int pos) {
        char quote = text.charAt(pos);
        if (quote == '\'' || quote == '"') {
            int next = pos + 1;
            StringBuilder builder = new StringBuilder();
            while (next < text.length()) {
                char c = text.charAt(next);
                if (c == quote) {
                    return new ParseValue(builder.toString(), next + 1);
                }
                builder.append(c);
                next++;
            }
            return new ParseValue(builder.toString(), next);
        }
        int next = pos;
        while (next < text.length() && !Character.isWhitespace(text.charAt(next))) {
            next++;
        }
        String raw = text.substring(pos, next);
        Object parsed = parsePrimitive(raw);
        return new ParseValue(parsed, next);
    }

    private static Object parsePrimitive(String raw) {
        String lower = raw.toLowerCase(Locale.ROOT);
        if ("true".equals(lower)) {
            return Boolean.TRUE;
        }
        if ("false".equals(lower)) {
            return Boolean.FALSE;
        }
        if (raw.startsWith("#")) {
            return raw;
        }
        try {
            if (raw.contains(".")) {
                return Double.parseDouble(raw);
            }
            return Integer.parseInt(raw);
        } catch (NumberFormatException ignored) {
        }
        return raw;
    }

    private static ResourceLocation resolveId(String name) {
        Objects.requireNonNull(name, "name");
        String normalized = name.trim();
        if (normalized.contains(":")) {
            return new ResourceLocation(normalized);
        }
        return new ResourceLocation("embers", normalized.toLowerCase(Locale.ROOT));
    }

    private static int findTagEnd(String input, int pos) {
        boolean quoted = false;
        char quoteChar = 0;
        for (int i = pos; i < input.length(); i++) {
            char c = input.charAt(i);
            if ((c == '"' || c == '\'') && (quoteChar == 0 || quoteChar == c)) {
                quoted = !quoted;
                quoteChar = quoted ? c : 0;
                continue;
            }
            if (c == '>' && !quoted) {
                return i;
            }
        }
        return -1;
    }

    private record ParseValue(Object value, int next) {}

    private record ActiveTag(int start, TagInfo info) {}

    private record TagInfo(String name, Attribute attribute) {}
}
