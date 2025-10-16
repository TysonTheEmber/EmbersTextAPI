package net.tysontheember.emberstextapi.markup;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EmberParserTest {
    @Test
    void parsesPlainText() {
        RNode node = EmberParser.parse("Hello World");
        assertInstanceOf(RNode.RText.class, node);
        assertEquals("Hello World", ((RNode.RText) node).text());
    }

    @Test
    void parsesNestedTags() {
        RNode node = EmberParser.parse("<bold>Hello <color from=#ff0000>World</color></bold>");
        RNode.RSpan root = (RNode.RSpan) node;
        assertEquals("bold", root.tag());
        assertEquals(2, root.children().size());
        assertInstanceOf(RNode.RText.class, root.children().get(0));
        RNode.RSpan color = (RNode.RSpan) root.children().get(1);
        assertEquals("color", color.tag());
        assertEquals("#ff0000", color.attrs().get("from"));
    }

    @Test
    void handlesSelfClosingTags() {
        RNode node = EmberParser.parse("Value <shake/>done");
        RNode.RSpan root = (RNode.RSpan) node;
        assertEquals("root", root.tag());
        assertEquals(3, root.children().size());
        assertInstanceOf(RNode.RText.class, root.children().get(0));
        assertInstanceOf(RNode.RSpan.class, root.children().get(1));
    }

    @Test
    void escapesLiteralLessThan() {
        RNode node = EmberParser.parse("Spell \\\<fire>");
        RNode.RSpan root = (RNode.RSpan) node;
        assertEquals("Spell <fire>", ((RNode.RText) root.children().get(0)).text());
    }

    @Test
    void recoversFromMissingClosingTag() {
        RNode node = EmberParser.parse("<bold>Unclosed");
        RNode.RSpan root = (RNode.RSpan) node;
        assertEquals("bold", root.tag());
    }
}
