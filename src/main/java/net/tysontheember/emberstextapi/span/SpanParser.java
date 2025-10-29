package net.tysontheember.emberstextapi.span;

import net.tysontheember.emberstextapi.debug.DebugEnvironment;
import net.tysontheember.emberstextapi.debug.DebugEventBus;
import net.tysontheember.emberstextapi.debug.DebugEvents;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Converts a raw text string with span markup into a {@link SpanDocument}.
 */
public final class SpanParser {
    public SpanDocument parse(String rawText) {
        return parse(rawText, DebugEnvironment.getEventBus());
    }

    public SpanDocument parse(String rawText, DebugEventBus eventBus) {
        String source = rawText == null ? "" : rawText;
        if (source.isEmpty()) {
            return SpanDocument.empty();
        }
        if (SpanStrings.noSpans(source)) {
            List<SpanNode> nodes = new ArrayList<>();
            nodes.add(new TextRunNode(source));
            if (eventBus != null) {
                eventBus.post(DebugEvents.parseStarted(source.length()));
                eventBus.post(DebugEvents.parseCompleted(source.length(), false));
            }
            return SpanDocument.of(source, nodes, false);
        }

        if (eventBus != null) {
            eventBus.post(DebugEvents.parseStarted(source.length()));
        }

        List<SpanNode> rootNodes = new ArrayList<>();
        Deque<MutableElement> stack = new ArrayDeque<>();
        StringBuilder textBuffer = new StringBuilder();
        boolean hasSpans = false;

        int index = 0;
        int length = source.length();
        while (index < length) {
            char c = source.charAt(index);
            if (c == '<') {
                flushText(rootNodes, stack, textBuffer);
                int closingIndex = source.indexOf('>', index + 1);
                if (closingIndex < 0) {
                    appendText(rootNodes, stack, source.substring(index));
                    if (eventBus != null) {
                        eventBus.post(DebugEvents.parseRecovered("Unterminated tag", index));
                    }
                    break;
                }
                String content = source.substring(index + 1, closingIndex);
                try {
                    SpanTokenizer.TagToken token = SpanTokenizer.parse(content, index);
                    if (token.isClosing()) {
                        if (!handleClosing(rootNodes, stack, token.getName())) {
                            if (eventBus != null) {
                                eventBus.post(DebugEvents.parseRecovered("Unexpected closing tag: " + token.getName(), index));
                            }
                            appendText(rootNodes, stack, "</" + token.getName() + ">");
                        }
                    } else {
                        hasSpans = true;
                        if (token.isSelfClosing()) {
                            SpanElementNode node = new SpanElementNode(token.getName(), token.getAttributes(), List.of());
                            addNode(rootNodes, stack, node);
                        } else {
                            stack.push(new MutableElement(token.getName(), token.getAttributes()));
                        }
                    }
                } catch (SpanParseException ex) {
                    if (eventBus != null) {
                        eventBus.post(DebugEvents.parseRecovered(ex.getMessage(), ex.getIndex()));
                    }
                    appendText(rootNodes, stack, '<' + content + '>');
                }
                index = closingIndex + 1;
            } else {
                textBuffer.append(c);
                index++;
            }
        }

        if (textBuffer.length() > 0) {
            appendText(rootNodes, stack, textBuffer.toString());
            textBuffer.setLength(0);
        }

        while (!stack.isEmpty()) {
            MutableElement element = stack.pop();
            if (eventBus != null) {
                eventBus.post(DebugEvents.parseRecovered("Auto-closing tag: " + element.name, length));
            }
            SpanElementNode node = element.toNode();
            addNode(rootNodes, stack, node);
        }

        SpanDocument document = SpanDocument.of(source, rootNodes, hasSpans && !rootNodes.isEmpty());
        if (eventBus != null) {
            eventBus.post(DebugEvents.parseCompleted(source.length(), document.hasSpans()));
        }
        return document;
    }

    private static void flushText(List<SpanNode> rootNodes, Deque<MutableElement> stack, StringBuilder buffer) {
        if (buffer.length() == 0) {
            return;
        }
        appendText(rootNodes, stack, buffer.toString());
        buffer.setLength(0);
    }

    private static void appendText(List<SpanNode> rootNodes, Deque<MutableElement> stack, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        TextRunNode node = new TextRunNode(SpanStrings.decodeEntities(text));
        if (stack.isEmpty()) {
            rootNodes.add(node);
        } else {
            stack.peek().children.add(node);
        }
    }

    private static void addNode(List<SpanNode> rootNodes, Deque<MutableElement> stack, SpanElementNode node) {
        if (stack.isEmpty()) {
            rootNodes.add(node);
        } else {
            stack.peek().children.add(node);
        }
    }

    private static boolean handleClosing(List<SpanNode> rootNodes, Deque<MutableElement> stack, String name) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.peek().name.equals(name)) {
            MutableElement element = stack.pop();
            SpanElementNode node = element.toNode();
            addNode(rootNodes, stack, node);
            return true;
        }
        return false;
    }

    private static final class MutableElement {
        final String name;
        final Map<String, String> attributes;
        final List<SpanNode> children = new ArrayList<>();

        MutableElement(String name, Map<String, String> attributes) {
            this.name = name;
            this.attributes = attributes;
        }

        SpanElementNode toNode() {
            return new SpanElementNode(name, attributes, children);
        }
    }
}
