package net.tysontheember.emberstextapi.render;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.inline.AttributeSpan;
import net.tysontheember.emberstextapi.inline.AttributedText;
import net.tysontheember.emberstextapi.inline.TagAttribute;
import net.tysontheember.emberstextapi.inline.attrs.Bold;
import net.tysontheember.emberstextapi.inline.attrs.Color;
import net.tysontheember.emberstextapi.inline.attrs.Gradient;
import net.tysontheember.emberstextapi.inline.attrs.Italic;
import net.tysontheember.emberstextapi.inline.attrs.Obfuscated;
import net.tysontheember.emberstextapi.inline.attrs.Typewriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AttributedComponentRenderer {
    public Component toComponent(AttributedText text, float timeSeconds) {
        if (text.spans().isEmpty()) {
            return Component.literal(text.raw());
        }
        int length = text.raw().length();
        List<Style> styles = new ArrayList<>(Collections.nCopies(length, Style.EMPTY));
        int visibleLength = length;
        for (AttributeSpan span : text.spans()) {
            for (TagAttribute attribute : span.attributes()) {
                if (attribute instanceof Typewriter typewriter) {
                    visibleLength = Math.min(visibleLength, computeTypewriterLimit(span, typewriter, timeSeconds));
                }
            }
        }
        if (visibleLength < length) {
            length = Math.max(0, visibleLength);
        }
        for (AttributeSpan span : text.spans()) {
            for (TagAttribute attribute : span.attributes()) {
                applyAttribute(styles, span.start(), span.end(), attribute, timeSeconds);
            }
        }
        return buildComponent(text.raw().substring(0, length), styles.subList(0, length));
    }

    private static int computeTypewriterLimit(AttributeSpan span, Typewriter typewriter, float timeSeconds) {
        float ticks = timeSeconds * 20.0f;
        float delay = typewriter.delay();
        if (ticks < delay) {
            return span.start();
        }
        float elapsed = ticks - delay;
        float speed = Math.max(0.001f, typewriter.speed());
        int visible = span.start() + Math.min(span.end() - span.start(), (int) Math.floor(elapsed / speed));
        return Math.max(span.start(), Math.min(span.end(), visible));
    }

    private static void applyAttribute(List<Style> styles, int start, int end, TagAttribute attribute, float timeSeconds) {
        if (start >= end) {
            return;
        }
        if (attribute instanceof Bold) {
            for (int i = start; i < end && i < styles.size(); i++) {
                styles.set(i, styles.get(i).withBold(true));
            }
        } else if (attribute instanceof Italic) {
            for (int i = start; i < end && i < styles.size(); i++) {
                styles.set(i, styles.get(i).withItalic(true));
            }
        } else if (attribute instanceof Obfuscated) {
            for (int i = start; i < end && i < styles.size(); i++) {
                styles.set(i, styles.get(i).withObfuscated(true));
            }
        } else if (attribute instanceof Color color) {
            TextColor textColor = TextColor.fromRgb(color.argb() & 0xFFFFFF);
            if (textColor != null) {
                for (int i = start; i < end && i < styles.size(); i++) {
                    styles.set(i, styles.get(i).withColor(textColor));
                }
            }
        } else if (attribute instanceof Gradient gradient) {
            int spanLength = Math.max(1, Math.min(end, styles.size()) - start);
            for (int i = 0; i < spanLength; i++) {
                float t = spanLength == 1 ? 0f : (float) i / (spanLength - 1);
                int colour = lerpColor(gradient.from(), gradient.to(), t);
                TextColor textColor = TextColor.fromRgb(colour & 0xFFFFFF);
                if (textColor != null) {
                    styles.set(start + i, styles.get(start + i).withColor(textColor));
                }
            }
        }
    }

    private static Component buildComponent(String text, List<Style> styles) {
        MutableComponent current = Component.empty();
        if (text.isEmpty()) {
            return current;
        }
        char[] chars = text.toCharArray();
        Style runStyle = styles.get(0);
        StringBuilder runText = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            Style style = styles.get(i);
            if (!style.equals(runStyle) && runText.length() > 0) {
                current.append(Component.literal(runText.toString()).withStyle(runStyle));
                runText.setLength(0);
            }
            if (!style.equals(runStyle)) {
                runStyle = style;
            }
            runText.append(chars[i]);
        }
        if (runText.length() > 0) {
            current.append(Component.literal(runText.toString()).withStyle(runStyle));
        }
        return current;
    }

    private static int lerpColor(int from, int to, float t) {
        int fr = (from >> 16) & 0xFF;
        int fg = (from >> 8) & 0xFF;
        int fb = from & 0xFF;
        int tr = (to >> 16) & 0xFF;
        int tg = (to >> 8) & 0xFF;
        int tb = to & 0xFF;
        int r = (int) (fr + (tr - fr) * t);
        int g = (int) (fg + (tg - fg) * t);
        int b = (int) (fb + (tb - fb) * t);
        return (r << 16) | (g << 8) | b;
    }
}
