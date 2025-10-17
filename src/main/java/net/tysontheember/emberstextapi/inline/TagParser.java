package net.tysontheember.emberstextapi.inline;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class TagParser {
    private static final Logger LOGGER = LogUtils.getLogger();

    private TagParser() {
    }

    public static AttributedText parse(String input) {
        if (!InlineConfig.enabled() || input.indexOf('<') < 0) {
            return AttributedText.of(unescape(input));
        }
        Parser parser = new Parser(input);
        return parser.parse();
    }

    public static String stripTags(String input) {
        return parse(input).raw();
    }

    private static String unescape(String input) {
        if (input.indexOf('\\') < 0) {
            return input;
        }
        StringBuilder builder = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\\' && i + 1 < input.length()) {
                char next = input.charAt(i + 1);
                if (next == '<' || next == '\\') {
                    builder.append(next);
                    i++;
                    continue;
                }
            }
            builder.append(c);
        }
        return builder.toString();
    }

    private static final class Parser {
        private final String input;
        private final StringBuilder plain = new StringBuilder();
        private final List<AttributeSpan> spans = new ArrayList<>();
        private final Deque<OpenTag> stack = new ArrayDeque<>();

        private Parser(String input) {
            this.input = input.length() > InlineConfig.maxLength()
                    ? input.substring(0, InlineConfig.maxLength())
                    : input;
        }

        private AttributedText parse() {
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (c == '\\') {
                    if (i + 1 < input.length()) {
                        char next = input.charAt(i + 1);
                        if (next == '<' || next == '\\') {
                            plain.append(next);
                            i++;
                            continue;
                        }
                    }
                    plain.append(c);
                    continue;
                }
                if (c == '<') {
                    int close = findTagEnd(i + 1);
                    if (close < 0) {
                        plain.append(c);
                        continue;
                    }
                    String content = input.substring(i + 1, close);
                    if (content.startsWith("/")) {
                        handleClose(content.substring(1).trim().toLowerCase(Locale.ROOT));
                    } else {
                        handleOpen(content);
                    }
                    i = close;
                    continue;
                }
                plain.append(c);
            }
            while (!stack.isEmpty()) {
                OpenTag tag = stack.pop();
                emitSpan(tag);
            }
            return new AttributedText(plain.toString(), spans);
        }

        private int findTagEnd(int from) {
            boolean quoted = false;
            char quoteChar = 0;
            for (int i = from; i < input.length(); i++) {
                char c = input.charAt(i);
                if (quoted) {
                    if (c == quoteChar) {
                        quoted = false;
                    }
                    continue;
                }
                if (c == '"' || c == '\'') {
                    quoted = true;
                    quoteChar = c;
                    continue;
                }
                if (c == '>') {
                    return i;
                }
            }
            return -1;
        }

        private void handleOpen(String content) {
            if (stack.size() >= InlineConfig.maxDepth()) {
                LOGGER.warn("Inline tag depth exceeded maxDepth={}, skipping tag {}", InlineConfig.maxDepth(), content);
                return;
            }
            TagToken token = parseToken(content);
            TagFactory factory = TagRegistry.get(token.name());
            List<TagAttribute> attributes = List.of();
            if (factory != null) {
                try {
                    attributes = factory.create(token, new TagParserContext(stackAttributes()));
                } catch (TagParseException e) {
                    LOGGER.warn("Failed to parse tag {}: {}", token.name(), e.getMessage());
                    attributes = List.of();
                }
            } else if (InlineConfig.logUnknown()) {
                LOGGER.debug("Unknown inline tag: {}", token.name());
            }
            stack.push(new OpenTag(token.name(), plain.length(), attributes));
        }

        private void handleClose(String name) {
            while (!stack.isEmpty()) {
                OpenTag tag = stack.pop();
                emitSpan(tag);
                if (tag.name.equals(name)) {
                    return;
                }
            }
        }

        private void emitSpan(OpenTag tag) {
            if (!tag.attributes.isEmpty()) {
                spans.add(new AttributeSpan(tag.start, plain.length(), tag.attributes));
            }
        }

        private TagToken parseToken(String content) {
            int idx = 0;
            int len = content.length();
            while (idx < len && Character.isWhitespace(content.charAt(idx))) {
                idx++;
            }
            int startName = idx;
            while (idx < len && !Character.isWhitespace(content.charAt(idx))) {
                idx++;
            }
            String name = content.substring(startName, idx).toLowerCase(Locale.ROOT);
            Map<String, String> attributes = new LinkedHashMap<>();
            while (idx < len) {
                while (idx < len && Character.isWhitespace(content.charAt(idx))) {
                    idx++;
                }
                if (idx >= len) {
                    break;
                }
                int keyStart = idx;
                while (idx < len && !Character.isWhitespace(content.charAt(idx)) && content.charAt(idx) != '=') {
                    idx++;
                }
                String key = content.substring(keyStart, idx);
                while (idx < len && Character.isWhitespace(content.charAt(idx))) {
                    idx++;
                }
                String value;
                if (idx < len && content.charAt(idx) == '=') {
                    idx++;
                    while (idx < len && Character.isWhitespace(content.charAt(idx))) {
                        idx++;
                    }
                    if (idx < len && (content.charAt(idx) == '"' || content.charAt(idx) == '\'')) {
                        char quote = content.charAt(idx++);
                        int valueStart = idx;
                        while (idx < len && content.charAt(idx) != quote) {
                            idx++;
                        }
                        value = content.substring(valueStart, Math.min(idx, len));
                        if (idx < len && content.charAt(idx) == quote) {
                            idx++;
                        }
                    } else {
                        int valueStart = idx;
                        while (idx < len && !Character.isWhitespace(content.charAt(idx))) {
                            idx++;
                        }
                        value = content.substring(valueStart, idx);
                    }
                } else {
                    value = "";
                }
                if (value.isEmpty() && !attributes.containsKey("value")) {
                    attributes.put("value", key);
                } else {
                    attributes.put(key, value);
                }
            }
            return new TagToken(name, attributes);
        }

        private Deque<TagAttribute> stackAttributes() {
            Deque<TagAttribute> attrs = new ArrayDeque<>();
            for (OpenTag tag : stack) {
                attrs.addAll(tag.attributes);
            }
            return attrs;
        }

        private record OpenTag(String name, int start, List<TagAttribute> attributes) {
        }
    }
}
