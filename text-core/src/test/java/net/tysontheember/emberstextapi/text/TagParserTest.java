package net.tysontheember.emberstextapi.text;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TagParserTest {
    @Test
    void nestedTagsProduceCombinedSpan() {
        AttributedText text = AttributedText.parse("<bold><color value=#ff0000>Hello</color></bold>");
        assertEquals("Hello", text.text());
        List<Span> spans = text.spans();
        assertEquals(1, spans.size());
        Span span = spans.get(0);
        assertEquals(0, span.start());
        assertEquals(5, span.end());
        assertEquals(2, span.attributes().size());
        assertTrue(span.attributes().stream().anyMatch(attr -> attr.id().equals(EmbersKey.of("embers", "bold"))),
                "Missing bold attribute");
        Attribute color = span.attributes().stream()
                .filter(attr -> attr.id().equals(EmbersKey.of("embers", "color")))
                .findFirst()
                .orElseThrow();
        assertEquals("#ff0000", color.params().raw().get("value"));
    }

    @Test
    void selfClosingTagDoesNotAffectOutput() {
        AttributedText text = AttributedText.parse("Start<marker/>End");
        assertEquals("StartEnd", text.text());
        assertTrue(text.spans().isEmpty());
    }

    @Test
    void escapedAngleBracketsAreRespected() {
        AttributedText text = AttributedText.parse("Value: \\<<bold>ignored</bold>");
        assertEquals("Value: <ignored", text.text());
        assertEquals(1, text.spans().size());
        Attribute bold = text.spans().get(0).attributes().get(0);
        assertEquals(EmbersKey.of("embers", "bold"), bold.id());
    }

    @Test
    void malformedClosingTagIsLeftLiteral() {
        AttributedText text = AttributedText.parse("<bold>Hello</color>");
        assertEquals("Hello</color>", text.text());
        assertTrue(text.spans().isEmpty());
    }

    @Test
    void parsesNumericAndBooleanParameters() {
        AttributedText text = AttributedText.parse("<wiggle a=1.5 f=2 w=0.5 enabled=false>Hi</wiggle>");
        Span span = text.spans().get(0);
        Attribute wiggle = span.attributes().get(0);
        assertEquals(1.5, wiggle.params().raw().get("a"));
        assertEquals(2, wiggle.params().raw().get("f"));
        assertEquals(0.5, wiggle.params().raw().get("w"));
        assertEquals(Boolean.FALSE, wiggle.params().raw().get("enabled"));
    }

    @Test
    void looksTaggedDetectsSimpleOpenTag() {
        assertTrue(AttributedText.looksTagged("<bold>Hello"));
        assertTrue(AttributedText.looksTagged("Normal</bold>"));
        assertFalse(AttributedText.looksTagged("No tags here"));
    }
}
