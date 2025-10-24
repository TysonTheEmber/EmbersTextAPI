package net.tysontheember.emberstextapi.client.spans;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import net.tysontheember.emberstextapi.client.markup.MarkupService;
import net.tysontheember.emberstextapi.client.spans.SpanBundle;
import net.tysontheember.emberstextapi.client.spans.TextSpanView;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class SpanifiedSequenceTest {

    @Test
    void appliesNestedBoldAndGradientStyles() {
        String markup = "<bold>Hello <grad values=\"" + ChatFormatting.RED.getName() + ", #00ff00\">World</grad></bold>";
        SpanBundle bundle = MarkupService.getInstance()
            .parse(markup, Locale.ROOT, false)
            .orElseThrow();

        FormattedCharSequence base = Component.literal(bundle.plainText()).getVisualOrderText();
        FormattedCharSequence sequence = SpanifiedSequence.of(base, bundle, SpanifiedSequence.EvalContext.EMPTY);

        StringBuilder rendered = new StringBuilder();
        List<Style> styles = new ArrayList<>();
        sequence.accept((index, style, codePoint) -> {
            rendered.append(Character.toChars(codePoint));
            styles.add(style);
            return true;
        });

        assertEquals(bundle.plainText(), rendered.toString());
        assertEquals(bundle.plainText().length(), styles.size());

        for (int i = 0; i < 6; i++) {
            assertTrue(styles.get(i).isBold(), "Expected bold for index " + i);
            assertNull(styles.get(i).getColor());
        }

        TextSpanView gradientSpan = bundle.spans().get(1);
        int gradientStart = gradientSpan.start();
        int gradientLength = gradientSpan.end() - gradientSpan.start();
        TextColor[] stops = gradientSpan.attr().gradient().colors();
        for (int i = gradientStart; i < gradientSpan.end(); i++) {
            Style style = styles.get(i);
            assertTrue(style.isBold(), "Gradient span should inherit bold at index " + i);
            TextColor expected = sampleGradient(stops, i - gradientStart, gradientLength);
            TextColor actual = style.getColor();
            assertNotNull(actual, "Gradient colour should be present at index " + i);
            assertEquals(expected.getValue(), actual.getValue(), "Unexpected gradient colour at index " + i);
        }
    }

    @Test
    void typewriterEffectRevealsCharactersOverTime() {
        String markup = "<typewriter speed=1.0>Hello</typewriter>";
        SpanBundle bundle = MarkupService.getInstance()
            .parse(markup, Locale.ROOT, false)
            .orElseThrow();

        AtomicLong time = new AtomicLong(0L);
        SpanifiedSequence.EvalContext context = new SpanifiedSequence.EvalContext(time::get, 0L, Locale.ROOT);
        FormattedCharSequence sequence = SpanifiedSequence.of(
            Component.literal(bundle.plainText()).getVisualOrderText(),
            bundle,
            context
        );

        StringBuilder rendered = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            rendered.appendCodePoint(codePoint);
            return true;
        });
        assertEquals("", rendered.toString(), "No characters should be visible initially");

        time.set(50L);
        rendered.setLength(0);
        sequence.accept((index, style, codePoint) -> {
            rendered.appendCodePoint(codePoint);
            return true;
        });
        assertEquals("H", rendered.toString(), "First character should appear after one tick");

        time.set(250L);
        rendered.setLength(0);
        sequence.accept((index, style, codePoint) -> {
            rendered.appendCodePoint(codePoint);
            return true;
        });
        assertEquals(bundle.plainText(), rendered.toString(), "All characters should be revealed after sufficient time");
    }

    private static TextColor sampleGradient(TextColor[] colors, int offset, int spanLength) {
        if (colors.length == 1 || spanLength <= 1) {
            return colors[0];
        }
        float t = spanLength <= 1 ? 0f : offset / (float) (spanLength - 1);
        int segments = colors.length - 1;
        float scaled = t * segments;
        int segIndex = Math.min(Math.max((int) Math.floor(scaled), 0), segments - 1);
        float local = scaled - segIndex;

        int start = colors[segIndex].getValue();
        int end = colors[segIndex + 1].getValue();
        int rgb = lerpColor(start, end, local);
        return TextColor.fromRgb(rgb);
    }

    private static int lerpColor(int start, int end, float t) {
        int sr = (start >> 16) & 0xFF;
        int sg = (start >> 8) & 0xFF;
        int sb = start & 0xFF;

        int er = (end >> 16) & 0xFF;
        int eg = (end >> 8) & 0xFF;
        int eb = end & 0xFF;

        int r = sr + Math.round((er - sr) * t);
        int g = sg + Math.round((eg - sg) * t);
        int b = sb + Math.round((eb - sb) * t);
        return (r << 16) | (g << 8) | b;
    }
}
