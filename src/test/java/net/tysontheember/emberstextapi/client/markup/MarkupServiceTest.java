package net.tysontheember.emberstextapi.client.markup;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.client.spans.SpanAttr;
import net.tysontheember.emberstextapi.client.spans.SpanBundle;
import net.tysontheember.emberstextapi.client.spans.TextSpanView;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class MarkupServiceTest {

    @Test
    void parsesNestedTagsWithInheritedStyles() {
        String markup = "<bold>Hello <italic>World</italic></bold>";
        SpanBundle bundle = MarkupService.getInstance()
            .parse(markup, Locale.ROOT, false)
            .orElseThrow();

        assertEquals("Hello World", bundle.plainText());
        assertEquals(2, bundle.legacySpans().size());

        TextSpan first = bundle.legacySpans().get(0);
        assertEquals("Hello ", first.getContent());
        assertEquals(Boolean.TRUE, first.getBold());
        assertNull(first.getItalic());

        TextSpan second = bundle.legacySpans().get(1);
        assertEquals("World", second.getContent());
        assertEquals(Boolean.TRUE, second.getBold());
        assertEquals(Boolean.TRUE, second.getItalic());

        TextSpanView firstView = bundle.spans().get(0);
        assertEquals(0, firstView.start());
        assertEquals(6, firstView.end());
        SpanAttr.StyleFlags flags = firstView.attr().style();
        assertTrue(flags.bold());
        assertFalse(flags.italic());

        TextSpanView secondView = bundle.spans().get(1);
        assertEquals(6, secondView.start());
        assertEquals(11, secondView.end());
        assertTrue(secondView.attr().style().bold());
        assertTrue(secondView.attr().style().italic());

        assertEquals(2, bundle.maxSpanDepth());
        assertEquals(0, bundle.maxEffectLayers());
    }

    @Test
    void parsesGradientAttributes() {
        String markup = "<grad values=\"red, #00ff00\">Hi</grad>";
        SpanBundle bundle = MarkupService.getInstance()
            .parse(markup, Locale.ROOT, false)
            .orElseThrow();

        assertEquals("Hi", bundle.plainText());
        assertEquals(1, bundle.legacySpans().size());
        TextSpan span = bundle.legacySpans().get(0);
        TextColor[] colors = span.getGradientColors();
        assertNotNull(colors);
        assertEquals(2, colors.length);
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.RED).getValue(), colors[0].getValue());
        assertEquals(TextColor.parseColor("#00ff00").getValue(), colors[1].getValue());

        TextSpanView view = bundle.spans().get(0);
        assertEquals(0, view.start());
        assertEquals(2, view.end());
        assertNotNull(view.attr().gradient());
        TextColor[] gradient = view.attr().gradient().colors();
        assertNotNull(gradient);
        assertEquals(2, gradient.length);
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.RED).getValue(), gradient[0].getValue());
        assertEquals(TextColor.parseColor("#00ff00").getValue(), gradient[1].getValue());

        assertEquals(1, bundle.maxSpanDepth());
        assertEquals(0, bundle.maxEffectLayers());
    }

    @Test
    void preservesEscapedMarkupCharacters() {
        String markup = new StringBuilder()
            .append("Keep \\ and ")
            .append('\\').append("<tags")
            .append('\\').append('>')
            .append(" <bold>end")
            .append('\\').append('>')
            .append('\\').append('\\')
            .append("</bold>")
            .toString();
        SpanBundle bundle = MarkupService.getInstance()
            .parse(markup, Locale.ROOT, false)
            .orElseThrow();

        assertEquals("Keep \\ and <tags> end>\\", bundle.plainText());
        assertEquals(2, bundle.legacySpans().size());

        TextSpan first = bundle.legacySpans().get(0);
        assertEquals("Keep \\ and <tags> ", first.getContent());
        assertNull(first.getBold());

        TextSpan second = bundle.legacySpans().get(1);
        assertEquals("end>\\", second.getContent());
        assertEquals(Boolean.TRUE, second.getBold());

        TextSpanView secondView = bundle.spans().get(1);
        assertEquals(first.getContent().length(), secondView.start());
        assertEquals(bundle.plainText().length(), secondView.end());
        assertTrue(secondView.attr().style().bold());

        assertEquals(1, bundle.maxSpanDepth());
        assertEquals(0, bundle.maxEffectLayers());
    }

    @Test
    void countsEffectLayers() {
        String markup = "<shake amplitude=\"1\">Wave</shake>";
        SpanBundle bundle = MarkupService.getInstance()
            .parse(markup, Locale.ROOT, false)
            .orElseThrow();

        assertEquals("Wave", bundle.plainText());
        assertEquals(1, bundle.maxSpanDepth());
        assertEquals(1, bundle.maxEffectLayers());
    }
}
