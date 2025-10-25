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
        int codePointIndex = 0;
        while (index < raw.length()) {
            char c = raw.charAt(index);
            if (c == '<') {
                int close = raw.indexOf('>', index + 1);
                if (close == -1) {
                    codePointIndex += appendLiteral(raw, index, raw.length(), sanitized);
                    break;
                }
                String body = raw.substring(index + 1, close).trim();
                if (body.isEmpty()) {
                    codePointIndex += appendLiteral(raw, index, close + 1, sanitized);
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
                            current.finish(codePointIndex);
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
                                MutableSpan span = new MutableSpan(name, codePointIndex, params);
                                SpanNode node = span.finishImmediate();
                                if (stack.isEmpty()) {
                                    roots.add(node);
                                } else {
                                    stack.peek().children.add(node);
                                }
                            } else {
                                stack.push(new MutableSpan(name, codePointIndex, params));
                            }
                        }
                    }
                }
                index = close + 1;
                continue;
            }
            int codePoint = raw.codePointAt(index);
            sanitized.appendCodePoint(codePoint);
            codePointIndex++;
            index += Character.charCount(codePoint);
        }

        while (!stack.isEmpty()) {
            MutableSpan span = stack.pop();
            span.finish(codePointIndex);
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

        List<SpanNode> finalizedRoots = finalizeTypewriterNodes(new ArrayList<>(roots), sanitizedText);
        SpanGraph graph = new SpanGraph(finalizedRoots, sanitizedText, codePointIndex, null);
        String signature = computeSignature(sanitizedText, graph);
        graph = new SpanGraph(graph.getRoots(), sanitizedText, graph.getSanitizedLength(), signature);
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

    private static List<SpanNode> finalizeTypewriterNodes(List<SpanNode> nodes, String sanitizedText) {
        boolean changed = false;
        List<SpanNode> result = new ArrayList<>(nodes.size());
        for (SpanNode node : nodes) {
            List<SpanNode> children = node.getChildren();
            List<SpanNode> updatedChildren = children.isEmpty() ? children : finalizeTypewriterNodes(children, sanitizedText);
            SpanNode current = updatedChildren == children ? node : node.withChildren(updatedChildren);
            if (isTypewriterNode(current)) {
                int[] boundaries = computeWordBoundariesIfNeeded(current, sanitizedText);
                SpanNode updated = current.withWordBoundaries(boundaries);
                if (updated != current) {
                    current = updated;
                    changed = true;
                }
            }
            if (current != node) {
                changed = true;
            }
            result.add(current);
        }
        return changed ? result : nodes;
    }

    private static boolean isTypewriterNode(SpanNode node) {
        return "typewriter".equals(node.getName());
    }

    private static int[] computeWordBoundariesIfNeeded(SpanNode node, String sanitizedText) {
        String mode = node.getParameter("mode");
        if (mode == null || !mode.equalsIgnoreCase("word")) {
            return new int[0];
        }
        int start = node.getStart();
        int end = node.getEnd();
        if (end <= start) {
            return new int[] { end };
        }
        int startChar = toCharIndex(sanitizedText, start);
        int endChar = toCharIndex(sanitizedText, end);
        if (startChar < 0 || endChar < startChar || endChar > sanitizedText.length()) {
            return new int[] { end };
        }
        int cpIndex = start;
        int charIndex = startChar;
        boolean inWord = false;
        int boundary = start;
        List<Integer> boundaries = new ArrayList<>();
        while (cpIndex < end && charIndex < endChar) {
            int codePoint = sanitizedText.codePointAt(charIndex);
            boolean whitespace = Character.isWhitespace(codePoint);
            if (!whitespace) {
                inWord = true;
                boundary = cpIndex + 1;
            } else if (inWord) {
                boundary = cpIndex + 1;
                boundaries.add(boundary);
                inWord = false;
            }
            cpIndex++;
            charIndex += Character.charCount(codePoint);
        }
        if (inWord) {
            boundaries.add(boundary);
        }
        if (boundaries.isEmpty() || boundaries.get(boundaries.size() - 1) < end) {
            boundaries.add(end);
        }
        int[] result = new int[boundaries.size()];
        for (int i = 0; i < boundaries.size(); i++) {
            result[i] = boundaries.get(i);
        }
        return result;
    }

    private static int toCharIndex(String text, int codePointIndex) {
        if (codePointIndex <= 0) {
            return 0;
        }
        if (codePointIndex >= text.codePointCount(0, text.length())) {
            return text.length();
        }
        return text.offsetByCodePoints(0, codePointIndex);
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

        private SpanNode toImmutable() {
            return new SpanNode(this.name, this.start, this.end, this.params, new ArrayList<>(this.children));
        }

        private SpanNode finishImmediate() {
            this.end = this.start;
            return toImmutable();
        }
    }

    private static int appendLiteral(String text, int start, int end, StringBuilder output) {
        int count = 0;
        int index = start;
        while (index < end) {
            int codePoint = text.codePointAt(index);
            output.appendCodePoint(codePoint);
            count++;
            index += Character.charCount(codePoint);
        }
        return count;
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
