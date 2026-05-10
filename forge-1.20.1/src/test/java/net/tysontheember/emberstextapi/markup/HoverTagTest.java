package net.tysontheember.emberstextapi.markup;

import net.tysontheember.emberstextapi.immersivemessages.api.MarkupParser;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HoverTagTest {

    @Test
    void textShorthand() {
        List<TextSpan> spans = MarkupParser.parse(
                "<hover text=\"tooltip\">label</hover>");
        assertEquals(1, spans.size());
        assertEquals("label", spans.get(0).getContent());
        assertEquals("show_text", spans.get(0).getHoverAction());
        assertEquals("tooltip", spans.get(0).getHoverValue());
    }

    @Test
    void fullForm() {
        List<TextSpan> spans = MarkupParser.parse(
                "<hover action=\"show_text\" value=\"tooltip\">label</hover>");
        assertEquals("show_text", spans.get(0).getHoverAction());
        assertEquals("tooltip", spans.get(0).getHoverValue());
    }

    @Test
    void unsupportedActionLeavesNoHover() {
        List<TextSpan> spans = MarkupParser.parse(
                "<hover action=\"show_item\" value=\"minecraft:diamond\">x</hover>");
        assertEquals("x", spans.get(0).getContent());
        assertNull(spans.get(0).getHoverAction());
    }

    @Test
    void emptyHoverIsInert() {
        List<TextSpan> spans = MarkupParser.parse("<hover>label</hover>");
        assertEquals("label", spans.get(0).getContent());
        assertNull(spans.get(0).getHoverAction());
    }

    @Test
    void missingValueLeavesNoHover() {
        List<TextSpan> spans = MarkupParser.parse(
                "<hover action=\"show_text\">label</hover>");
        assertEquals("label", spans.get(0).getContent());
        assertNull(spans.get(0).getHoverAction());
    }

    @Test
    void styleNestedInsideHoverInheritsHover() {
        List<TextSpan> spans = MarkupParser.parse(
                "<hover text=\"hi\"><b>bold</b></hover>");
        assertEquals("bold", spans.get(0).getContent());
        assertEquals(Boolean.TRUE, spans.get(0).getBold());
        assertEquals("show_text", spans.get(0).getHoverAction());
        assertEquals("hi", spans.get(0).getHoverValue());
    }
}
