package net.tysontheember.emberstextapi.markup;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.tysontheember.emberstextapi.markup.RNode.RSpan;
import static net.tysontheember.emberstextapi.markup.RNode.RText;

/**
 * Hand written parser for Ember markup strings.  The grammar is intentionally
 * permissive – malformed tags are treated as literal text wherever possible so
 * that user input does not explode the rendering pipeline.
 */
final class MarkupParser {
    private MarkupParser() {
    }

    static RSpan parse(String input) {
        String source = input == null ? "" : input;
        ParserState state = new ParserState(source);
        state.parse();
        return state.root();
    }

    private static final class ParserState {
        private final String source;
        private final int length;
        private final Deque<Frame> stack = new ArrayDeque<>();
        private final StringBuilder textBuffer = new StringBuilder();

        ParserState(String source) {
            this.source = source;
            this.length = source.length();
            this.stack.push(Frame.rootFrame());
        }

        RSpan root() {
            flushText();
            while (stack.size() > 1) {
                Frame frame = stack.pop();
                stack.peek().children.add(frame.build());
            }
            return stack.peek().build();
        }

        void parse() {
            int i = 0;
            while (i < length) {
                char c = source.charAt(i);
                if (c == '\\') {
                    if (i + 1 < length && source.charAt(i + 1) == '<') {
                        textBuffer.append('<');
                        i += 2;
                        continue;
                    }
                }

                if (c == '<') {
                    int tagEnd = findTagEnd(i + 1);
                    if (tagEnd == -1) {
                        textBuffer.append(source.substring(i));
                        break;
                    }

                    String tagContent = source.substring(i + 1, tagEnd).trim();
                    if (tagContent.isEmpty()) {
                        textBuffer.append('<');
                        i++;
                        continue;
                    }

                    if (!tagContent.contains(" ") && tagContent.startsWith("/")) {
                        // simple closing tag like </tag>
                        flushText();
                        String closing = tagContent.substring(1).trim();
                        closeFrame(closing);
                        i = tagEnd + 1;
                        continue;
                    }

                    if (tagContent.startsWith("/")) {
                        // malformed closing tag; treat literally
                        textBuffer.append('<').append(tagContent).append('>');
                        i = tagEnd + 1;
                        continue;
                    }

                    flushText();
                    boolean selfClosing = tagContent.endsWith("/");
                    if (selfClosing) {
                        tagContent = tagContent.substring(0, tagContent.length() - 1).trim();
                    }

                    TagDescriptor descriptor = parseTagDescriptor(tagContent);
                    if (descriptor == null) {
                        textBuffer.append('<').append(tagContent).append('>');
                        i = tagEnd + 1;
                        continue;
                    }

                    Frame frame = new Frame(descriptor.name(), descriptor.attrs());
                    if (selfClosing) {
                        stack.peek().children.add(frame.build());
                    } else {
                        stack.push(frame);
                    }

                    i = tagEnd + 1;
                    continue;
                }

                textBuffer.append(c);
                i++;
            }
        }

        private void closeFrame(String closing) {
            Deque<Frame> temp = new ArrayDeque<>();
            boolean matched = false;
            while (!stack.isEmpty() && stack.peek().isRoot() == false) {
                Frame frame = stack.pop();
                if (frame.tag.equalsIgnoreCase(closing)) {
                    matched = true;
                    stack.peek().children.add(frame.build());
                    break;
                }
                temp.push(frame);
            }

            while (!temp.isEmpty()) {
                stack.push(temp.pop());
            }

            if (!matched && !closing.isEmpty()) {
                stack.peek().children.add(new RText("</" + closing + ">"));
            }
        }

        private void flushText() {
            if (textBuffer.isEmpty()) {
                return;
            }
            stack.peek().children.add(new RText(textBuffer.toString()));
            textBuffer.setLength(0);
        }

        private int findTagEnd(int start) {
            int depth = 0;
            for (int i = start; i < length; i++) {
                char ch = source.charAt(i);
                if (ch == '\\') {
                    i++; // skip escaped character inside attribute
                    continue;
                }
                if (ch == '"') {
                    i = consumeQuoted(i + 1, '"');
                    if (i == -1) {
                        return -1;
                    }
                    continue;
                }
                if (ch == '\'') {
                    i = consumeQuoted(i + 1, '\'');
                    if (i == -1) {
                        return -1;
                    }
                    continue;
                }
                if (ch == '<') {
                    depth++;
                }
                if (ch == '>' && depth == 0) {
                    return i;
                }
                if (ch == '>' && depth > 0) {
                    depth--;
                }
            }
            return -1;
        }

        private int consumeQuoted(int start, char quote) {
            for (int i = start; i < length; i++) {
                char ch = source.charAt(i);
                if (ch == '\\') {
                    i++;
                    continue;
                }
                if (ch == quote) {
                    return i;
                }
            }
            return -1;
        }
    }

    private record Frame(String tag, Map<String, String> attrs, List<RNode> children, boolean root) {
        static Frame rootFrame() {
            return new Frame("root", Map.of(), new ArrayList<>(), true);
        }

        Frame(String tag, Map<String, String> attrs) {
            this(tag, attrs, new ArrayList<>(), false);
        }

        RSpan build() {
            return new RSpan(tag, attrs, children);
        }

        boolean isRoot() {
            return root;
        }
    }

    private record TagDescriptor(String name, Map<String, String> attrs) {
    }

    private static TagDescriptor parseTagDescriptor(String content) {
        int firstSpace = indexOfWhitespace(content);
        String name = firstSpace == -1 ? content : content.substring(0, firstSpace);
        if (name.isEmpty()) {
            return null;
        }
        Map<String, String> attrs = firstSpace == -1
            ? Map.of()
            : parseAttributes(content.substring(firstSpace + 1));
        return new TagDescriptor(name, attrs);
    }

    private static Map<String, String> parseAttributes(String content) {
        Map<String, String> attrs = new LinkedHashMap<>();
        Tokeniser tokeniser = new Tokeniser(content);
        String token;
        while ((token = tokeniser.next()) != null) {
            int eq = token.indexOf('=');
            if (eq == -1) {
                attrs.putIfAbsent("value", unquote(token));
            } else {
                String key = token.substring(0, eq);
                String value = token.substring(eq + 1);
                attrs.put(key, unquote(value));
            }
        }
        return attrs;
    }

    private static int indexOfWhitespace(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (Character.isWhitespace(input.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private static String unquote(String token) {
        if (token.length() >= 2) {
            char first = token.charAt(0);
            char last = token.charAt(token.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return token.substring(1, token.length() - 1);
            }
        }
        return token;
    }

    private static final class Tokeniser {
        private final String input;
        private int index;

        Tokeniser(String input) {
            this.input = input;
            this.index = 0;
        }

        String next() {
            skipWhitespace();
            if (index >= input.length()) {
                return null;
            }
            int start = index;
            boolean inQuote = false;
            char quote = '\0';
            while (index < input.length()) {
                char c = input.charAt(index);
                if (!inQuote && Character.isWhitespace(c)) {
                    break;
                }
                if (c == '\\' && index + 1 < input.length()) {
                    index += 2;
                    continue;
                }
                if (c == '"' || c == '\'') {
                    if (inQuote && c == quote) {
                        inQuote = false;
                    } else if (!inQuote) {
                        inQuote = true;
                        quote = c;
                    }
                }
                index++;
            }
            String token = input.substring(start, index);
            skipWhitespace();
            return token;
        }

        private void skipWhitespace() {
            while (index < input.length() && Character.isWhitespace(input.charAt(index))) {
                index++;
            }
        }
    }
}
