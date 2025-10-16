package net.tysontheember.emberstextapi.markup;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Hand-rolled parser that turns a markup string into an {@link RNode} tree.
 */
final class MarkupParser {
    private MarkupParser() {
    }

    static RNode parse(String input) {
        String source = input == null ? "" : input;
        Deque<Frame> stack = new ArrayDeque<>();
        stack.push(new Frame("__root__"));
        StringBuilder text = new StringBuilder();

        int length = source.length();
        int index = 0;
        while (index < length) {
            char c = source.charAt(index);
            if (c == '\\') {
                if (index + 1 < length && source.charAt(index + 1) == '<') {
                    text.append('<');
                    index += 2;
                    continue;
                }
                text.append(c);
                index++;
                continue;
            }

            if (c != '<') {
                text.append(c);
                index++;
                continue;
            }

            // tag start
            int tagStart = index;
            int tagEnd = findTagEnd(source, tagStart + 1);
            if (tagEnd == -1) {
                // unterminated tag, treat as literal
                text.append(source.substring(tagStart));
                break;
            }

            flushText(text, stack.peek());

            String tagContent = source.substring(tagStart + 1, tagEnd);
            boolean closing = tagContent.startsWith("/");
            boolean selfClosing = tagContent.endsWith("/");

            if (closing) {
                handleClosing(stack, tagContent.substring(1).trim());
            } else {
                parseOpening(stack, tagContent, selfClosing);
            }

            index = tagEnd + 1;
        }

        flushText(text, stack.peek());

        while (stack.size() > 1) {
            Frame frame = stack.pop();
            stack.peek().children.add(frame.toSpan());
        }

        List<RNode> children = stack.peek().children;
        if (children.isEmpty()) {
            return new RNode.RText("");
        }
        if (children.size() == 1) {
            return children.get(0);
        }
        return new RNode.RSpan("root", Map.of(), List.copyOf(children));
    }

    private static void flushText(StringBuilder text, Frame frame) {
        if (text.length() == 0 || frame == null) {
            return;
        }
        frame.children.add(new RNode.RText(text.toString()));
        text.setLength(0);
    }

    private static void handleClosing(Deque<Frame> stack, String rawName) {
        String name = rawName.trim();
        if (name.isEmpty()) {
            return;
        }
        name = canonical(name);
        if (stack.size() <= 1) {
            return;
        }

        Frame frame = stack.peek();
        if (frame.tag.equals(name)) {
            stack.pop();
            stack.peek().children.add(frame.toSpan());
            return;
        }

        // Error tolerance: unwind until match
        Deque<Frame> buffer = new ArrayDeque<>();
        while (stack.size() > 1) {
            Frame current = stack.pop();
            if (current.tag.equals(name)) {
                stack.peek().children.add(current.toSpan());
                while (!buffer.isEmpty()) {
                    Frame dangling = buffer.pop();
                    stack.peek().children.add(dangling.toSpan());
                }
                return;
            }
            buffer.push(current);
        }

        // no matching tag, push back buffered frames
        while (!buffer.isEmpty()) {
            stack.push(buffer.pop());
        }
    }

    private static void parseOpening(Deque<Frame> stack, String content, boolean selfClosing) {
        ParsedTag tag = parseTag(content);
        if (tag == null || tag.name.isEmpty()) {
            // treat as literal text by re-appending
            Frame frame = stack.peek();
            frame.children.add(new RNode.RText("<" + content + ">"));
            return;
        }
        Frame frame = new Frame(tag.name, tag.attrs);
        if (selfClosing) {
            stack.peek().children.add(frame.toSpan());
            return;
        }
        stack.push(frame);
    }

    private static int findTagEnd(String source, int start) {
        int length = source.length();
        for (int i = start; i < length; i++) {
            char c = source.charAt(i);
            if (c == '"') {
                i = skipQuoted(source, i + 1, '"');
                continue;
            }
            if (c == '\'') {
                i = skipQuoted(source, i + 1, '\'');
                continue;
            }
            if (c == '>') {
                return i;
            }
        }
        return -1;
    }

    private static int skipQuoted(String source, int start, char quote) {
        int length = source.length();
        for (int i = start; i < length; i++) {
            char c = source.charAt(i);
            if (c == quote && source.charAt(i - 1) != '\\') {
                return i;
            }
        }
        return length - 1;
    }

    private static ParsedTag parseTag(String raw) {
        int length = raw.length();
        int index = 0;
        while (index < length && Character.isWhitespace(raw.charAt(index))) {
            index++;
        }
        int start = index;
        while (index < length) {
            char c = raw.charAt(index);
            if (Character.isWhitespace(c) || c == '/' || c == '>') {
                break;
            }
            index++;
        }
        String name = canonical(raw.substring(start, index));
        Map<String, String> attrs = new HashMap<>();

        while (index < length) {
            while (index < length && Character.isWhitespace(raw.charAt(index))) {
                index++;
            }
            if (index >= length) {
                break;
            }
            char c = raw.charAt(index);
            if (c == '/') {
                break;
            }

            int keyStart = index;
            while (index < length) {
                c = raw.charAt(index);
                if (c == '=' || Character.isWhitespace(c) || c == '/' || c == '>') {
                    break;
                }
                index++;
            }
            String key = raw.substring(keyStart, index);
            while (index < length && Character.isWhitespace(raw.charAt(index))) {
                index++;
            }
            boolean hasEquals = index < length && raw.charAt(index) == '=';
            String value;
            if (hasEquals) {
                index++;
                while (index < length && Character.isWhitespace(raw.charAt(index))) {
                    index++;
                }
                if (index < length) {
                    char valueStart = raw.charAt(index);
                    if (valueStart == '"' || valueStart == '\'') {
                        int valueEnd = skipQuoted(raw, index + 1, valueStart);
                        value = raw.substring(index + 1, Math.min(valueEnd, length));
                        index = Math.min(valueEnd + 1, length);
                    } else {
                        int valueEnd = index;
                        while (valueEnd < length) {
                            char ch = raw.charAt(valueEnd);
                            if (Character.isWhitespace(ch) || ch == '/' || ch == '>') {
                                break;
                            }
                            valueEnd++;
                        }
                        value = raw.substring(index, valueEnd);
                        index = valueEnd;
                    }
                } else {
                    value = "";
                }
                if (!key.isEmpty()) {
                    attrs.put(canonical(key), value);
                }
            } else {
                if (!key.isEmpty()) {
                    attrs.putIfAbsent("value", key);
                }
            }
        }

        return new ParsedTag(name, attrs);
    }

    private static String canonical(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private record ParsedTag(String name, Map<String, String> attrs) {
    }

    private static final class Frame {
        final String tag;
        final Map<String, String> attrs;
        final List<RNode> children = new ArrayList<>();

        Frame(String tag) {
            this(tag, Map.of());
        }

        Frame(String tag, Map<String, String> attrs) {
            this.tag = canonical(tag);
            this.attrs = attrs == null ? Map.of() : Map.copyOf(attrs);
        }

        RNode.RSpan toSpan() {
            return new RNode.RSpan(tag, attrs, List.copyOf(children));
        }
    }
}
