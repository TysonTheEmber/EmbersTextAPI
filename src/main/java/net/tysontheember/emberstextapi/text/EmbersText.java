package net.tysontheember.emberstextapi.text;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Entry point for working with {@link AttributedText} instances from gameplay code.
 */
public final class EmbersText {

    private static final Logger LOGGER = LogUtils.getLogger();

    private EmbersText() {
    }

    /**
     * Parses a markup string into an {@link AttributedText} representation.
     *
     * @param input markup text, may be {@code null}
     * @return parsed attributed text (never {@code null})
     */
    public static AttributedText parse(String input) {
        if (input == null) {
            return AttributedText.builder().build();
        }
        try {
            return MarkupParser.parse(input);
        } catch (RuntimeException ex) {
            LOGGER.warn("Failed to parse markup text '{}': {}", input, ex.getMessage());
            return AttributedText.builder().text(input).build();
        }
    }

    /**
     * Converts the supplied attributed text into a vanilla {@link Component} approximation.
     *
     * @param text attributed text to convert
     * @return chat component with best-effort styling
     */
    public static Component toComponent(AttributedText text) {
        if (text == null) {
            return Component.literal("");
        }
        String raw = text.getText();
        if (raw.isEmpty()) {
            return Component.literal("");
        }
        MutableComponent result = Component.literal("");
        for (SpanRun run : buildRuns(text)) {
            int start = Math.min(run.start(), raw.length());
            int end = Math.min(run.end(), raw.length());
            if (start >= end) {
                continue;
            }
            Style style = mergeStyle(run.attributes());
            result.append(Component.literal(raw.substring(start, end)).withStyle(style));
        }
        return result.getSiblings().isEmpty() ? Component.literal(raw) : result;
    }

    private static List<SpanRun> buildRuns(AttributedText text) {
        List<Span> spans = new ArrayList<>(text.getSpans());
        spans.sort(Comparator.comparingInt(Span::getStart).thenComparingInt(Span::getEnd));
        List<SpanRun> runs = new ArrayList<>();
        int cursor = 0;
        int length = text.getText().length();
        for (Span span : spans) {
            int start = Math.max(0, Math.min(span.getStart(), length));
            int end = Math.max(0, Math.min(span.getEnd(), length));
            if (end <= start) {
                continue;
            }
            if (start > cursor) {
                runs.add(new SpanRun(cursor, start, null));
            }
            runs.add(new SpanRun(start, end, span.getAttributes()));
            cursor = Math.max(cursor, end);
        }
        if (cursor < length) {
            runs.add(new SpanRun(cursor, length, null));
        }
        if (runs.isEmpty() && length > 0) {
            runs.add(new SpanRun(0, length, null));
        }
        return runs;
    }

    private static Style mergeStyle(AttributeSet attributes) {
        Style base = Style.EMPTY;
        if (attributes == null) {
            return base;
        }
        AttributeSet.Style style = attributes.getStyle();
        if (style != null) {
            base = base.withBold(style.isBold())
                    .withItalic(style.isItalic())
                    .withUnderlined(style.isUnderlined())
                    .withStrikethrough(style.isStrikethrough())
                    .withObfuscated(style.isObfuscated());
        }
        String colourValue = attributes.getColor();
        if (colourValue != null) {
            TextColor color = parseColor(colourValue);
            if (color != null) {
                base = base.withColor(color);
            }
        }
        return base;
    }

    private static TextColor parseColor(String value) {
        if (value == null) {
            return null;
        }
        ChatFormatting fmt = ChatFormatting.getByName(value);
        if (fmt != null) {
            return TextColor.fromLegacyFormat(fmt);
        }
        return TextColor.parseColor(value);
    }

    private record SpanRun(int start, int end, AttributeSet attributes) {
    }
}
