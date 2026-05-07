package net.tysontheember.emberstextapi.markup;

import net.tysontheember.emberstextapi.immersivemessages.api.MarkupParser;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MarkupParserPositionalTest {

    @Test
    void colorAcceptsPositionalFirstArg() {
        List<TextSpan> spans = MarkupParser.parse("<color red>hi</color>");
        assertEquals(1, spans.size());
        assertNotNull(spans.get(0).getColor(), "expected positional <color red> to set color");
    }

    @Test
    void colorAcceptsExplicitColorAttr() {
        List<TextSpan> spans = MarkupParser.parse("<color color=red>hi</color>");
        assertEquals(1, spans.size());
        assertNotNull(spans.get(0).getColor());
    }

    @Test
    void bareAttributeAfterFirstStaysTrue() {
        List<TextSpan> spans = MarkupParser.parse("<color red bold>hi</color>");
        assertEquals(1, spans.size());
        assertNotNull(spans.get(0).getColor(), "first bare token is value");
        assertNull(spans.get(0).getBold(), "color tag ignores stray bold attribute (it lives in attrs map but isn't applied here)");
    }
}
