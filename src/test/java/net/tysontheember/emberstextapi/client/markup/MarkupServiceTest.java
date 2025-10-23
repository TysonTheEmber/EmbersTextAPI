package net.tysontheember.emberstextapi.client.markup;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.client.spans.SpanBundle;
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
        assertEquals(2, bundle.spans().size());

        TextSpan first = bundle.spans().get(0);
        assertEquals("Hello ", first.getContent());
        assertEquals(Boolean.TRUE, first.getBold());
        assertNull(first.getItalic());

        TextSpan second = bundle.spans().get(1);
        assertEquals("World", second.getContent());
        assertEquals(Boolean.TRUE, second.getBold());
        assertEquals(Boolean.TRUE, second.getItalic());
    }

    @Test
    void parsesGradientAttributes() {
        String markup = "<grad values=\"red, #00ff00\">Hi</grad>";
        SpanBundle bundle = MarkupService.getInstance()
            .parse(markup, Locale.ROOT, false)
            .orElseThrow();

        assertEquals("Hi", bundle.plainText());
        assertEquals(1, bundle.spans().size());
        TextSpan span = bundle.spans().get(0);
        TextColor[] colors = span.getGradientColors();
        assertNotNull(colors);
        assertEquals(2, colors.length);
        assertEquals(TextColor.fromLegacyFormat(ChatFormatting.RED).getValue(), colors[0].getValue());
        assertEquals(TextColor.parseColor("#00ff00").getValue(), colors[1].getValue());
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
        assertEquals(2, bundle.spans().size());

        TextSpan first = bundle.spans().get(0);
        assertEquals("Keep \\ and <tags> ", first.getContent());
        assertNull(first.getBold());

        TextSpan second = bundle.spans().get(1);
        assertEquals("end>\\", second.getContent());
        assertEquals(Boolean.TRUE, second.getBold());
    }
}
