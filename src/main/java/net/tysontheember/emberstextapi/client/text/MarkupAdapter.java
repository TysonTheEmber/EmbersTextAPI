package net.tysontheember.emberstextapi.client.text;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Markup adapter responsible for creating span graphs from raw strings.
 */
public final class MarkupAdapter {
    private MarkupAdapter() {
    }

    public static ParseResult parse(String raw) {
        if (raw == null || raw.isEmpty()) {
            return new ParseResult(raw == null ? "" : raw, null, null);
        }

        StringBuilder sanitized = new StringBuilder(raw.length());
        List<SpanNode> roots = new ArrayList<>();
        Deque<MutableSpan> stack = new ArrayDeque<>();
        boolean sawTag = false;
        int index = 0;
        while (index < raw.length()) {
            char c = raw.charAt(index);
            if (c == '<') {
                int close = raw.indexOf('>', index + 1);
                if (close == -1) {
                    sanitized.append(raw.substring(index));
                    break;
                }
                String body = raw.substring(index + 1, close).trim();
                if (body.isEmpty()) {
                    sanitized.append(raw, index, close + 1);
                    index = close + 1;
                    continue;
                }
                boolean closing = body.startsWith("/");
                boolean selfClosing = !closing && body.endsWith("/");
                if (closing) {
                    String name = body.substring(1).trim();
                    if (!name.isEmpty()) {
                        MutableSpan current = stack.peek();
                        if (current != null && current.name.equals(name)) {
                            stack.pop();
                            current.finish(sanitized.length());
                            SpanNode node = current.toImmutable();
                            if (stack.isEmpty()) {
                                roots.add(node);
                            } else {
                                stack.peek().children.add(node);
                            }
                            sawTag = true;
                        }
                    }
                } else {
                    String trimmed = selfClosing ? body.substring(0, body.length() - 1).trim() : body;
                    if (!trimmed.isEmpty()) {
                        int nameEnd = findNameEnd(trimmed);
                        String name = trimmed.substring(0, nameEnd);
                        Map<String, String> params = parseAttributes(trimmed.substring(nameEnd));
                        if (!name.isEmpty()) {
                            sawTag = true;
                            if (selfClosing) {
                                MutableSpan span = new MutableSpan(name, sanitized.length(), params);
                                SpanNode node = span.finishImmediate();
                                if (stack.isEmpty()) {
                                    roots.add(node);
                                } else {
                                    stack.peek().children.add(node);
                                }
                            } else {
                                stack.push(new MutableSpan(name, sanitized.length(), params));
                            }
                        }
                    }
                }
                index = close + 1;
                continue;
            }
            sanitized.append(c);
            index++;
        }

        while (!stack.isEmpty()) {
            MutableSpan span = stack.pop();
            span.finish(sanitized.length());
            SpanNode node = span.toImmutable();
            if (stack.isEmpty()) {
                roots.add(node);
            } else {
                stack.peek().children.add(node);
            }
        }

        String sanitizedText = sanitized.toString();
        if (!sawTag) {
            return new ParseResult(sanitizedText, null, null);
        }

        SpanGraph graph = new SpanGraph(new ArrayList<>(roots), sanitizedText.length(), null);
        String signature = computeSignature(sanitizedText, graph);
        graph = new SpanGraph(graph.getRoots(), graph.getSanitizedLength(), signature);
        return new ParseResult(sanitizedText, graph, signature);
    }

    private static int findNameEnd(String text) {
        int idx = 0;
        int len = text.length();
        while (idx < len) {
            char ch = text.charAt(idx);
            if (Character.isWhitespace(ch) || ch == '/' || ch == '>') {
                break;
            }
            idx++;
        }
        return idx;
    }

    private static Map<String, String> parseAttributes(String text) {
        Map<String, String> params = new LinkedHashMap<>();
        int idx = 0;
        int len = text.length();
        while (idx < len) {
            while (idx < len && Character.isWhitespace(text.charAt(idx))) {
                idx++;
            }
            if (idx >= len) {
                break;
            }
            int keyStart = idx;
            while (idx < len && !Character.isWhitespace(text.charAt(idx)) && text.charAt(idx) != '=') {
                idx++;
            }
            if (keyStart == idx) {
                idx++;
                continue;
            }
            String key = text.substring(keyStart, idx);
            while (idx < len && Character.isWhitespace(text.charAt(idx))) {
                idx++;
            }
            String value = "";
            if (idx < len && text.charAt(idx) == '=') {
                idx++;
                while (idx < len && Character.isWhitespace(text.charAt(idx))) {
                    idx++;
                }
                if (idx < len) {
                    char quote = text.charAt(idx);
                    if (quote == '"' || quote == '\'') {
                        idx++;
                        int valueStart = idx;
                        while (idx < len && text.charAt(idx) != quote) {
                            idx++;
                        }
                        value = text.substring(valueStart, Math.min(idx, len));
                        if (idx < len && text.charAt(idx) == quote) {
                            idx++;
                        }
                    } else {
                        int valueStart = idx;
                        while (idx < len && !Character.isWhitespace(text.charAt(idx))) {
                            idx++;
                        }
                        value = text.substring(valueStart, idx);
                    }
                }
            }
            params.put(key, value);
        }
        return params;
    }

    private static String computeSignature(String sanitized, SpanGraph graph) {
        StringBuilder builder = new StringBuilder(sanitized.length() + 64);
        builder.append(sanitized);
        appendNodes(builder, graph.getRoots());
        byte[] input = builder.toString().getBytes(StandardCharsets.UTF_8);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashed = digest.digest(input);
            StringBuilder hex = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                String part = Integer.toHexString(b & 0xFF);
                if (part.length() == 1) {
                    hex.append('0');
                }
                hex.append(part);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Missing SHA-1 implementation", ex);
        }
    }

    private static void appendNodes(StringBuilder builder, List<SpanNode> nodes) {
        for (SpanNode node : nodes) {
            builder.append('[')
                .append(node.getName())
                .append('@')
                .append(node.getStart())
                .append(':')
                .append(node.getEnd());
            if (!node.getParameters().isEmpty()) {
                builder.append('{');
                node.getParameters().forEach((key, value) -> builder.append(key).append('=').append(value).append(';'));
                builder.append('}');
            }
            if (!node.getChildren().isEmpty()) {
                appendNodes(builder, node.getChildren());
            }
            builder.append(']');
        }
    }

    private static final class MutableSpan {
        private final String name;
        private final int start;
        private final Map<String, String> params;
        private final List<SpanNode> children = new ArrayList<>();
        private int end;

        private MutableSpan(String name, int start, Map<String, String> params) {
            this.name = name;
            this.start = start;
            this.params = params == null ? new LinkedHashMap<>() : new LinkedHashMap<>(params);
            this.end = start;
        }

        private void finish(int endIndex) {
            this.end = endIndex;
        }

        private SpanNode finishImmediate() {
            this.end = this.start;
            return toImmutable();
        }

        private SpanNode toImmutable() {
            return new SpanNode(this.name, this.start, this.end, this.params, new ArrayList<>(this.children));
        }
    }

    public static final class ParseResult {
        public final String sanitized;
        public final SpanGraph graph;
        public final String signature;

        public ParseResult(String sanitized, SpanGraph graph, String signature) {
            this.sanitized = sanitized;
            this.graph = graph;
            this.signature = signature;
        }
    }
}
