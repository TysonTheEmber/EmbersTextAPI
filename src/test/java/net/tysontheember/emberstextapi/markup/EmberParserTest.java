package net.tysontheember.emberstextapi.markup;

import net.tysontheember.emberstextapi.markup.RNode.RSpan;
import net.tysontheember.emberstextapi.markup.RNode.RText;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmberParserTest {
    @Test
    void parsesNestedTags() {
        RSpan root = EmberParser.parse("<bold>Hello <color value=#ff0000>World</color></bold>");
        assertEquals("root", root.tag());
        assertEquals(1, root.children().size());
        RSpan bold = (RSpan) root.children().get(0);
        assertEquals("bold", bold.tag());
        assertEquals(2, bold.children().size());
        assertEquals("Hello ", ((RText) bold.children().get(0)).text());
        RSpan color = (RSpan) bold.children().get(1);
        assertEquals("color", color.tag());
        assertEquals("#ff0000", color.attrs().get("value"));
        assertEquals("World", ((RText) color.children().get(0)).text());
    }

    @Test
    void escapeSequenceKeepsLiteral() {
        RSpan root = EmberParser.parse("Spell uses \\<fire> rune");
        assertEquals("Spell uses <fire> rune", ((RText) root.children().get(0)).text());
    }

    @Test
    void missingClosingTagClosesAtEnd() {
        RSpan root = EmberParser.parse("<shake amp=1.2>Alert!");
        assertEquals(1, root.children().size());
        RSpan shake = (RSpan) root.children().get(0);
        assertEquals("shake", shake.tag());
        assertEquals("1.2", shake.attrs().get("amp"));
        assertEquals(List.of(new RText("Alert!")), shake.children());
    }

    @Test
    void escapedLessThanIsLiteral() {
        RSpan root = EmberParser.parse("Value is \\<10");
        assertEquals("Value is <10", ((RText) root.children().get(0)).text());
    }
}
