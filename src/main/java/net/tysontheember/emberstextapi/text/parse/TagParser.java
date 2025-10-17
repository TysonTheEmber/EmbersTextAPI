package net.tysontheember.emberstextapi.text.parse;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.text.Attribute;
import net.tysontheember.emberstextapi.text.AttributedText;
import net.tysontheember.emberstextapi.text.Attributes;
import net.tysontheember.emberstextapi.text.Span;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Simple stack-based parser that consumes the custom tag grammar and produces an {@link AttributedText} instance.
 */
public final class TagParser {
    private static final Logger LOGGER = LogUtils.getLogger();

    private record Frame(Attribute attribute, int startIndex) {
    }

    public AttributedText parse(String input) {
        StringBuilder raw = new StringBuilder();
        List<Span> spans = new ArrayList<>();
        Deque<Frame> stack = new ArrayDeque<>();

        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (c == '\\') {
                if (i + 1 < input.length() && input.charAt(i + 1) == '<') {
                    raw.append('<');
                    i += 2;
                    continue;
                }
                raw.append(c);
                i++;
            } else if (c == '&') {
                if (input.startsWith("&lt;", i)) {
                    raw.append('<');
                    i += 4;
                } else if (input.startsWith("&gt;", i)) {
                    raw.append('>');
                    i += 4;
                } else if (input.startsWith("&amp;", i)) {
                    raw.append('&');
                    i += 5;
                } else {
                    raw.append(c);
                    i++;
                }
            } else if (c == '<') {
                int closing = findTagEnd(input, i + 1);
                if (closing == -1) {
                    raw.append('<');
                    i++;
                    continue;
                }
                String tagContent = input.substring(i + 1, closing).trim();
                if (tagContent.isEmpty()) {
                    raw.append('<');
                    i++;
                    continue;
                }
                if (tagContent.startsWith("/")) {
                    String name = tagContent.substring(1).trim();
                    ResourceLocation id = Attributes.resolve(name);
                    closeTag(id, raw.length(), stack, spans);
                } else {
                    boolean selfClosing = tagContent.endsWith("/");
                    if (selfClosing) {
                        tagContent = tagContent.substring(0, tagContent.length() - 1).trim();
                    }
                    ParsedTag parsed = parseTag(tagContent);
                    if (parsed != null) {
                        ResourceLocation id = Attributes.resolve(parsed.name);
                        Attribute attribute = new Attribute(id, parsed.params);
                        if (selfClosing) {
                            // Zero-length span
                            spans.add(new Span(raw.length(), raw.length(), List.of(attribute)));
                        } else {
                            stack.push(new Frame(attribute, raw.length()));
                        }
                    }
                }
                i = closing + 1;
            } else {
                raw.append(c);
                i++;
            }
        }

        while (!stack.isEmpty()) {
            Frame frame = stack.pop();
            LOGGER.debug("Unclosed tag {}", frame.attribute.id());
        }

        spans.sort((a, b) -> Integer.compare(a.start(), b.start()));
        AttributedText.Builder builder = AttributedText.builder(raw.toString());
        for (Span span : spans) {
            builder.addSpan(span);
        }
        return builder.build();
    }

    private void closeTag(ResourceLocation id, int endIndex, Deque<Frame> stack, List<Span> spans) {
        Frame frame = null;
        while (!stack.isEmpty()) {
            Frame current = stack.pop();
            if (current.attribute.id().equals(id)) {
                frame = current;
                break;
            }
        }
        if (frame == null) {
            LOGGER.debug("Ignoring unmatched closing tag {}", id);
            return;
        }
        if (endIndex > frame.startIndex) {
            spans.add(new Span(frame.startIndex, endIndex, List.of(frame.attribute)));
        }
    }

    private ParsedTag parseTag(String content) {
        int idx = 0;
        int len = content.length();
        while (idx < len && Character.isWhitespace(content.charAt(idx))) {
            idx++;
        }
        int nameStart = idx;
        while (idx < len && !Character.isWhitespace(content.charAt(idx))) {
            idx++;
        }
        if (nameStart == idx) {
            return null;
        }
        String name = content.substring(nameStart, idx);
        Map<String, Object> params = new LinkedHashMap<>();
        while (idx < len) {
            while (idx < len && Character.isWhitespace(content.charAt(idx))) {
                idx++;
            }
            if (idx >= len) break;
            int keyStart = idx;
            while (idx < len && content.charAt(idx) != '=' && !Character.isWhitespace(content.charAt(idx))) {
                idx++;
            }
            if (idx >= len) {
                break;
            }
            String key = content.substring(keyStart, idx);
            while (idx < len && Character.isWhitespace(content.charAt(idx))) {
                idx++;
            }
            if (idx < len && content.charAt(idx) == '=') {
                idx++;
            }
            while (idx < len && Character.isWhitespace(content.charAt(idx))) {
                idx++;
            }
            if (idx >= len) {
                params.put(key, "");
                break;
            }
            char valueStart = content.charAt(idx);
            String value;
            boolean quoted = valueStart == '"' || valueStart == '\'';
            if (quoted) {
                char quote = valueStart;
                idx++;
                StringBuilder sb = new StringBuilder();
                boolean escaped = false;
                while (idx < len) {
                    char ch = content.charAt(idx++);
                    if (escaped) {
                        sb.append(ch);
                        escaped = false;
                    } else if (ch == '\\') {
                        escaped = true;
                    } else if (ch == quote) {
                        break;
                    } else {
                        sb.append(ch);
                    }
                }
                value = sb.toString();
            } else {
                int startValue = idx;
                while (idx < len && !Character.isWhitespace(content.charAt(idx))) {
                    idx++;
                }
                value = content.substring(startValue, idx);
            }
            params.put(key, parseValue(value));
        }
        return new ParsedTag(name, params);
    }

    private Object parseValue(String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if ("true".equals(lower) || "false".equals(lower)) {
            return Boolean.parseBoolean(lower);
        }
        if (trimmed.startsWith("#") || trimmed.startsWith("0x")) {
            return trimmed;
        }
        try {
            if (trimmed.contains(".")) {
                return Float.parseFloat(trimmed);
            }
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ignored) {
        }
        return trimmed;
    }

    private int findTagEnd(String input, int start) {
        boolean inQuote = false;
        char quoteChar = '\0';
        for (int i = start; i < input.length(); i++) {
            char c = input.charAt(i);
            if ((c == '"' || c == '\'') && (quoteChar == '\0' || quoteChar == c)) {
                if (!inQuote) {
                    inQuote = true;
                    quoteChar = c;
                } else {
                    inQuote = false;
                    quoteChar = '\0';
                }
            } else if (c == '>' && !inQuote) {
                return i;
            }
        }
        return -1;
    }

    private record ParsedTag(String name, Map<String, Object> params) {
    }
}
