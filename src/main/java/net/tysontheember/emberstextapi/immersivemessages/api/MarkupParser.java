package net.tysontheember.emberstextapi.immersivemessages.api;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.tysontheember.emberstextapi.core.markup.MarkupInstruction;
import net.tysontheember.emberstextapi.core.markup.MarkupInstructionType;
import net.tysontheember.emberstextapi.core.markup.MarkupStream;
import net.tysontheember.emberstextapi.core.markup.SpanEffectRegistry;
import net.tysontheember.emberstextapi.core.markup.SpanEffectRegistry.ActiveSpanEffect;
import net.tysontheember.emberstextapi.core.markup.SpanEffectRegistry.SpanEffectCloseContext;
import net.tysontheember.emberstextapi.core.markup.SpanEffectRegistry.SpanEffectFactory;
import net.tysontheember.emberstextapi.core.markup.SpanEffectRegistry.TagContext;

/**
 * Parses markup text into either TextSpan collections or raw tag instructions.
 */
public class MarkupParser {

    private static final Pattern TAG_PATTERN = Pattern.compile(
            "<(/?)([a-zA-Z][a-zA-Z0-9]*)((?:\\s+[a-zA-Z][a-zA-Z0-9]*[=:](?:[\\\"'][^\\\"']*[\\\"']|[^\\s>]+))*)>");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(
            "([a-zA-Z][a-zA-Z0-9]*)[=:](?:([\\\"])([^\\\"]*)\\2|([^\\s>]+))");

    private MarkupParser() {
    }

    /**
     * Parses markup text into a list of {@link TextSpan}s.
     */
    public static List<TextSpan> parse(String markup) {
        if (markup == null || markup.isEmpty()) {
            return Collections.emptyList();
        }
        return parseStream(stream(markup));
    }

    /**
     * Converts a {@link MarkupStream} into spans using the registered tag effects.
     */
    public static List<TextSpan> parseStream(MarkupStream stream) {
        List<TextSpan> result = new ArrayList<>();
        if (stream == null || stream.isEmpty()) {
            return result;
        }

        Deque<ActiveTag> stack = new ArrayDeque<>();
        String plain = stream.plainText();
        int cursor = 0;

        for (MarkupInstruction instruction : stream.instructions()) {
            int position = Math.min(Math.max(instruction.position(), 0), plain.length());
            if (position > cursor) {
                addSpanSegment(plain.substring(cursor, position), stack, result);
                cursor = position;
            }

            if (instruction.type() == MarkupInstructionType.OPEN) {
                TagContext context = new TagContext(instruction.name(), instruction.attributes());
                SpanEffectFactory factory = SpanEffectRegistry.getFactory(context.name());
                ActiveSpanEffect effect = factory != null ? factory.create(context) : null;
                stack.addLast(new ActiveTag(context, effect));
            } else {
                ActiveTag tag = popMatching(stack, instruction.name());
                if (tag != null && tag.effect() != null) {
                    tag.effect().onClose(new SpanEffectCloseContext(tag.context(), result));
                }
            }
        }

        if (cursor < plain.length()) {
            addSpanSegment(plain.substring(cursor), stack, result);
        }

        return result;
    }

    /**
     * Parses the raw markup into a {@link MarkupStream} containing plain text and tag instructions.
     */
    public static MarkupStream stream(String markup) {
        if (markup == null || markup.isEmpty()) {
            return new MarkupStream("", List.of());
        }

        List<MarkupInstruction> instructions = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        Matcher matcher = TAG_PATTERN.matcher(markup);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                builder.append(markup, lastEnd, matcher.start());
            }

            String closing = matcher.group(1);
            String tag = matcher.group(2);
            String attributeString = matcher.group(3);

            Map<String, String> attributes = parseAttributes(attributeString);
            MarkupInstructionType type = "/".equals(closing) ? MarkupInstructionType.CLOSE : MarkupInstructionType.OPEN;
            instructions.add(new MarkupInstruction(type, tag, builder.length(), attributes));

            lastEnd = matcher.end();
        }

        if (lastEnd < markup.length()) {
            builder.append(markup.substring(lastEnd));
        }

        return new MarkupStream(builder.toString(), instructions);
    }

    private static Map<String, String> parseAttributes(String attributeString) {
        Map<String, String> attributes = new HashMap<>();
        if (attributeString == null || attributeString.trim().isEmpty()) {
            return attributes;
        }

        Matcher matcher = ATTRIBUTE_PATTERN.matcher(attributeString);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
            attributes.put(key.toLowerCase(Locale.ROOT), value);
        }

        return attributes;
    }

    /**
     * Converts a list of spans back to a plain text string (removes markup).
     */
    public static String toPlainText(List<TextSpan> spans) {
        if (spans == null || spans.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (TextSpan span : spans) {
            result.append(span.getContent());
        }
        return result.toString();
    }

    /**
     * Utility method to create a simple single-span list from plain text.
     */
    public static List<TextSpan> fromPlainText(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new TextSpan(text));
    }

    private static void addSpanSegment(String content, Deque<ActiveTag> stack, List<TextSpan> output) {
        if (content.isEmpty()) {
            return;
        }
        TextSpan span = new TextSpan(content);
        for (ActiveTag tag : stack) {
            if (tag.effect() != null) {
                tag.effect().applyToTextSpan(span);
            }
        }
        output.add(span);
    }

    private static ActiveTag popMatching(Deque<ActiveTag> stack, String name) {
        if (stack.isEmpty() || name == null) {
            return null;
        }
        for (var iterator = stack.descendingIterator(); iterator.hasNext();) {
            ActiveTag tag = iterator.next();
            if (tag.context().name().equals(name)) {
                iterator.remove();
                return tag;
            }
        }
        return null;
    }

    private record ActiveTag(TagContext context, ActiveSpanEffect effect) {
    }
}
