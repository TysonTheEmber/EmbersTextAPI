package net.tysontheember.emberstextapi.markup;

import net.tysontheember.emberstextapi.immersivemessages.api.MarkupParser;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClickHoverInteractionTest {

    @BeforeAll
    static void stubResolver() {
        MarkupParser.setLangResolver((key, args) -> "Diamond");
    }

    @AfterAll
    static void resetResolver() {
        MarkupParser.setLangResolver(null);
    }

    @Test
    void clickWrappingHover() {
        List<TextSpan> spans = MarkupParser.parse(
                "<click link=\"https://example.com\"><hover text=\"open\">go</hover></click>");
        assertEquals(1, spans.size());
        assertEquals("go", spans.get(0).getContent());
        assertEquals("open_url", spans.get(0).getClickAction());
        assertEquals("https://example.com", spans.get(0).getClickValue());
        assertEquals("show_text", spans.get(0).getHoverAction());
        assertEquals("open", spans.get(0).getHoverValue());
    }

    @Test
    void hoverWrappingClick() {
        List<TextSpan> spans = MarkupParser.parse(
                "<hover text=\"open\"><click link=\"https://example.com\">go</click></hover>");
        assertEquals(1, spans.size());
        assertEquals("show_text", spans.get(0).getHoverAction());
        assertEquals("open_url", spans.get(0).getClickAction());
    }

    @Test
    void clickAroundLangResolvedText() {
        List<TextSpan> spans = MarkupParser.parse(
                "<click link=\"https://wiki.example/diamond\"><lang:item.minecraft.diamond></click>");
        assertEquals(1, spans.size());
        assertEquals("Diamond", spans.get(0).getContent());
        assertEquals("open_url", spans.get(0).getClickAction());
    }
}
