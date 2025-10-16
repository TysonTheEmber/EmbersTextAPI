package net.tysontheember.emberstextapi.markup;

import net.tysontheember.emberstextapi.attributes.TextAttributes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Hand-written parser for Ember Markup. The parser is intentionally lightweight
 * and does not allocate intermediate token structures.
 */
final class MarkupParser {
    private final String source;
    private final ParserConfig config;
    private final List<Character> textBuffer = new ArrayList<>();

    private MarkupParser(String source, ParserConfig config) {
        this.source = source == null ? "" : source;
        this.config = config == null ? ParserConfig.defaults() : config;
    }

    static RSpan parse(String source, ParserConfig config) {
        MarkupParser parser = new MarkupParser(source, config);
        return parser.parseInternal();
    }

    private RSpan parseInternal() {
        NodeBuilder root = new NodeBuilder("root", Map.of());
        Deque<NodeBuilder> stack = new ArrayDeque<>();
        stack.push(root);
        int i = 0;
        while (i < source.length()) {
            char c = source.charAt(i);
            if (c == '\\') {
                if (i + 1 < source.length() && source.charAt(i + 1) == '<') {
                    textBuffer.add('<');
                    i += 2;
                    continue;
                }
            }
            if (c == '<') {
                flushText(stack.peek());
                TagToken token = readTag(i + 1);
                if (token == null) {
                    textBuffer.add('<');
                    i++;
                    continue;
                }
                i = token.endIndex;
                if (token.type == TagType.CLOSE) {
                    popUntil(stack, token.name);
                } else {
                    NodeBuilder builder = new NodeBuilder(token.name, token.attributes);
                    if (token.type == TagType.SELF_CLOSING) {
                        stack.peek().addChild(builder.toSpan());
                    } else {
                        stack.push(builder);
                    }
                }
                continue;
            }
            textBuffer.add(c);
            i++;
        }
        flushText(stack.peek());
        while (stack.size() > 1) {
            NodeBuilder open = stack.pop();
            stack.peek().addChild(open.toSpan());
        }
        return root.toSpan();
    }

    private void popUntil(Deque<NodeBuilder> stack, String name) {
        while (stack.size() > 1) {
            NodeBuilder builder = stack.pop();
            stack.peek().addChild(builder.toSpan());
            if (builder.tag.equalsIgnoreCase(name)) {
                return;
            }
        }
    }

    private void flushText(NodeBuilder current) {
        if (textBuffer.isEmpty() || current == null) {
            return;
        }
        StringBuilder sb = new StringBuilder(textBuffer.size());
        for (char c : textBuffer) {
            sb.append(c);
        }
        textBuffer.clear();
        current.addChild(new RText(sb.toString()));
    }

    private TagToken readTag(int startIndex) {
        int i = startIndex;
        if (i >= source.length()) {
            return null;
        }
        boolean closing = false;
        if (source.charAt(i) == '/') {
            closing = true;
            i++;
        }
        while (i < source.length() && Character.isWhitespace(source.charAt(i))) {
            i++;
        }
        int nameStart = i;
        while (i < source.length()) {
            char c = source.charAt(i);
            if (Character.isWhitespace(c) || c == '>' || c == '/') {
                break;
            }
            i++;
        }
        if (i == nameStart) {
            return null;
        }
        String name = source.substring(nameStart, i).toLowerCase(Locale.ROOT);
        Map<String, String> attrs = new LinkedHashMap<>();
        while (i < source.length()) {
            char c = source.charAt(i);
            if (c == '>') {
                break;
            }
            if (c == '/') {
                int ahead = skipWhitespace(i + 1);
                if (ahead < source.length() && source.charAt(ahead) == '>') {
                    i = ahead;
                    break;
                }
            }
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            int keyStart = i;
            while (i < source.length()) {
                c = source.charAt(i);
                if (c == '=' || Character.isWhitespace(c) || c == '>' || c == '/') {
                    break;
                }
                i++;
            }
            String key = source.substring(keyStart, i).toLowerCase(Locale.ROOT);
            while (i < source.length() && Character.isWhitespace(source.charAt(i))) {
                i++;
            }
            String value;
            if (i < source.length() && source.charAt(i) == '=') {
                i++;
                while (i < source.length() && Character.isWhitespace(source.charAt(i))) {
                    i++;
                }
                if (i < source.length() && (source.charAt(i) == '"' || source.charAt(i) == '\'')) {
                    char quote = source.charAt(i++);
                    int valueStart = i;
                    while (i < source.length() && source.charAt(i) != quote) {
                        if (source.charAt(i) == '\\' && i + 1 < source.length()) {
                            i += 2;
                            continue;
                        }
                        i++;
                    }
                    value = source.substring(valueStart, Math.min(i, source.length()));
                    if (i < source.length()) {
                        i++;
                    }
                } else {
                    int valueStart = i;
                    while (i < source.length()) {
                        char ch = source.charAt(i);
                        if (Character.isWhitespace(ch) || ch == '>' || ch == '/') {
                            break;
                        }
                        i++;
                    }
                    value = source.substring(valueStart, i);
                }
            } else {
                value = "";
            }
            if (!key.isEmpty()) {
                attrs.put(key, value);
            }
        }
        boolean selfClosing = false;
        int end = i;
        if (end < source.length() && source.charAt(end) == '/') {
            selfClosing = true;
            end++;
        }
        if (end < source.length() && source.charAt(end) == '>') {
            end++;
        }
        TagType type = closing ? TagType.CLOSE : (selfClosing ? TagType.SELF_CLOSING : TagType.OPEN);
        return new TagToken(type, name, attrs, end);
    }

    private int skipWhitespace(int index) {
        int i = index;
        while (i < source.length() && Character.isWhitespace(source.charAt(i))) {
            i++;
        }
        return i;
    }

    enum TagType { OPEN, CLOSE, SELF_CLOSING }

    private record TagToken(TagType type, String name, Map<String, String> attributes, int endIndex) { }

    private final class NodeBuilder {
        private final String tag;
        private final Map<String, String> attrs;
        private final List<RNode> children = new ArrayList<>();

        private NodeBuilder(String tag, Map<String, String> attrs) {
            this.tag = tag == null ? "" : tag;
            this.attrs = attrs == null ? Map.of() : new LinkedHashMap<>(attrs);
        }

        private void addChild(RNode node) {
            if (node == null) {
                return;
            }
            if (MarkupParser.this.config.stripUnknownTags && node instanceof RSpan span && TextAttributes.get(span.tag()) == null) {
                children.addAll(span.children());
                return;
            }
            children.add(node);
        }

        private RSpan toSpan() {
            return new RSpan(tag, Map.copyOf(attrs), List.copyOf(children));
        }
    }

    static final class ParserConfig {
        final boolean stripUnknownTags;

        ParserConfig(boolean stripUnknownTags) {
            this.stripUnknownTags = stripUnknownTags;
        }

        static ParserConfig defaults() {
            return new ParserConfig(false);
        }
    }
}
