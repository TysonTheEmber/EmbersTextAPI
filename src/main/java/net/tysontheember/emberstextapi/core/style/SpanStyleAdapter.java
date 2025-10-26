package net.tysontheember.emberstextapi.core.style;

import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;

/**
 * Utility helpers that translate {@link TextSpan} attributes into vanilla {@link Style} mutations.
 */
public final class SpanStyleAdapter {
    private SpanStyleAdapter() {
    }

    public static Style applyTextSpan(Style style, TextSpan span) {
        if (style == null || span == null) {
            return style;
        }

        Style result = style;
        if (span.getColor() != null) {
            result = result.withColor(span.getColor());
        }
        if (span.getBold() != null) {
            result = result.withBold(span.getBold());
        }
        if (span.getItalic() != null) {
            result = result.withItalic(span.getItalic());
        }
        if (span.getUnderline() != null) {
            result = result.withUnderlined(span.getUnderline());
        }
        if (span.getStrikethrough() != null) {
            result = result.withStrikethrough(span.getStrikethrough());
        }
        if (span.getObfuscated() != null) {
            result = result.withObfuscated(span.getObfuscated());
        }
        if (span.getFont() != null) {
            result = result.withFont(span.getFont());
        }
        return result;
    }
}
