package net.tysontheember.emberstextapi.text;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.EmbersTextAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * A string decorated with attribute spans produced either by the {@link TagParser}
 * or programmatically.
 */
public final class AttributedText {
    private final String text;
    private final List<Span> spans;

    public AttributedText(String text) {
        this.text = text == null ? "" : text;
        this.spans = new ArrayList<>();
    }

    private AttributedText(String text, List<Span> spans) {
        this.text = text == null ? "" : text;
        this.spans = new ArrayList<>(spans);
    }

    public static AttributedText of(String text) {
        return new AttributedText(text);
    }

    public static AttributedText of(String text, List<Span> spans) {
        return new AttributedText(text, spans);
    }

    public String text() {
        return text;
    }

    public int length() {
        return text.length();
    }

    public List<Span> spans() {
        if (spans.isEmpty()) {
            return List.of();
        }
        List<Span> filtered = new ArrayList<>();
        for (Span span : spans) {
            if (!span.isEmptyAttributes()) {
                filtered.add(span);
            }
        }
        return Collections.unmodifiableList(filtered);
    }

    public void apply(Attribute attribute, int start, int end) {
        if (attribute == null) return;
        start = Math.max(0, start);
        end = Math.min(length(), end);
        if (start >= end) return;

        ensureBaseSpan();
        splitAt(start);
        splitAt(end);

        for (int i = 0; i < spans.size(); i++) {
            Span span = spans.get(i);
            if (span.end() <= start) {
                continue;
            }
            if (span.start() >= end) {
                break;
            }
            List<Attribute> newAttributes = new ArrayList<>(span.attributes());
            newAttributes.add(attribute);
            spans.set(i, span.withAttributes(newAttributes));
        }

        coalesce();
    }

    public void apply(ResourceLocation id, Params params, int start, int end) {
        apply(new Attribute(id, params), start, end);
    }

    private void ensureBaseSpan() {
        if (spans.isEmpty() && length() > 0) {
            spans.add(new Span(0, length(), List.of()));
        }
    }

    private void splitAt(int index) {
        if (index <= 0 || index >= length()) {
            return;
        }
        ensureBaseSpan();
        for (int i = 0; i < spans.size(); i++) {
            Span span = spans.get(i);
            if (index == span.start() || index == span.end()) {
                return;
            }
            if (span.start() < index && index < span.end()) {
                List<Attribute> attrs = new ArrayList<>(span.attributes());
                Span first = new Span(span.start(), index, attrs);
                Span second = new Span(index, span.end(), attrs);
                spans.set(i, first);
                spans.add(i + 1, second);
                return;
            }
        }
    }

    private void coalesce() {
        if (spans.isEmpty()) {
            return;
        }
        List<Span> result = new ArrayList<>();
        Span previous = spans.get(0);
        for (int i = 1; i < spans.size(); i++) {
            Span next = spans.get(i);
            if (previous.end() == next.start() && previous.attributes().equals(next.attributes())) {
                previous = new Span(previous.start(), next.end(), previous.attributes());
            } else {
                result.add(previous);
                previous = next;
            }
        }
        result.add(previous);
        spans.clear();
        spans.addAll(result);
    }

    public void toBuffer(FriendlyByteBuf buf) {
        buf.writeUtf(text);
        buf.writeVarInt(spans.size());
        for (Span span : spans) {
            span.toBuffer(buf);
        }
    }

    public static AttributedText fromBuffer(FriendlyByteBuf buf) {
        String text = buf.readUtf();
        int size = buf.readVarInt();
        List<Span> spans = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            spans.add(Span.fromBuffer(buf));
        }
        return new AttributedText(text, spans);
    }

    public interface WarningSink extends BiConsumer<String, Throwable> {}

    public static AttributedText parse(String tagged) {
        return TagParser.parse(tagged, (message, throwable) -> {
            if (EmbersTextAPI.logger().isDebugEnabled()) {
                if (throwable != null) {
                    EmbersTextAPI.logger().debug(message, throwable);
                } else {
                    EmbersTextAPI.logger().debug(message);
                }
            }
        });
    }

    public static boolean looksTagged(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        int search = 0;
        while (search < input.length()) {
            int open = input.indexOf('<', search);
            if (open < 0 || open + 1 >= input.length()) {
                return false;
            }
            char next = input.charAt(open + 1);
            if (Character.isLetter(next) || next == '/' || next == '#') {
                int close = input.indexOf('>', open + 1);
                if (close > open + 1) {
                    return true;
                }
            }
            search = open + 1;
        }
        return false;
    }

    static AttributedText fromParsed(String text, List<Span> spans) {
        return new AttributedText(text, spans);
    }
}
