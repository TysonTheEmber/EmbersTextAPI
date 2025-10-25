package net.tysontheember.emberstextapi.client.text;

import java.util.Objects;

import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/**
 * Helper utilities for span-aware width calculations and sanitisation.
 */
public final class SpanStringSplitter {
    private SpanStringSplitter() {
    }

    public static String sanitized(Style base, String rawIfNeeded) {
        if (base instanceof SpanStyleExtras extras) {
            SpanGraph graph = extras.eta$getSpanGraph();
            if (graph != null) {
                String sanitized = graph.getSanitizedText();
                if (sanitized != null) {
                    return sanitized;
                }
            }
        }
        if (rawIfNeeded == null) {
            return "";
        }
        if (containsMarkup(rawIfNeeded)) {
            return MarkupAdapter.parse(rawIfNeeded).sanitized;
        }
        return rawIfNeeded;
    }

    public static float widthOf(String sanitized, Style baseStyle, StringSplitter splitter) {
        if (sanitized == null || sanitized.isEmpty() || splitter == null) {
            return 0.0F;
        }
        FormattedCharSequence sequence = FormattedCharSequence.forward(sanitized, Objects.requireNonNullElse(baseStyle, Style.EMPTY));
        return ((StringSplitterBridge) splitter).emberstextapi$callStringWidthOriginal(sequence);
    }

    public static MutableComponent literal(String text, Style style) {
        MutableComponent component = Component.literal(text);
        if (style != null) {
            component.setStyle(style);
        }
        return component;
    }

    private static boolean containsMarkup(String value) {
        int open = value.indexOf('<');
        if (open < 0) {
            return false;
        }
        int close = value.indexOf('>', open + 1);
        return close > open;
    }
}
