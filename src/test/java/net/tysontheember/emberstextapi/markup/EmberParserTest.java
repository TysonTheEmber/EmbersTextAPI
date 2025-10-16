package net.tysontheember.emberstextapi.markup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmberParserTest {
    @Test
    void parsesNestedTags() {
        RSpan root = EmberParser.parse("<bold>Hello <color value=#ff0000>world</color></bold>");
        assertEquals(1, root.children().size());
        RSpan bold = (RSpan) root.children().get(0);
        assertEquals("bold", bold.tag());
        assertEquals(2, bold.children().size());
        assertTrue(bold.children().get(0) instanceof RText);
        RSpan color = (RSpan) bold.children().get(1);
        assertEquals("color", color.tag());
        assertEquals("#ff0000", color.attr("value"));
    }

    @Test
    void escapesLiteralAngleBracket() {
        RSpan root = EmberParser.parse("Hello \\<<bold>world</bold>");
        assertTrue(root.children().get(0) instanceof RText);
        RText text = (RText) root.children().get(0);
        assertTrue(text.text().contains("<"));
    }

    @Test
    void ignoresMismatchedClosingTag() {
        RSpan root = EmberParser.parse("<bold>Bold</italic>");
        assertEquals(1, root.children().size());
        RSpan bold = (RSpan) root.children().get(0);
        assertEquals("bold", bold.tag());
    }
}
