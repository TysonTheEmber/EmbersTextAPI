package net.tysontheember.emberstextapi.immersivemessages.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ItemTagParserTest {

    @Test
    public void testSelfClosingItemTag() {
        String markup = "<item id=minecraft:stick/>";
        List<TextSpan> spans = MarkupParser.parse(markup);

        assertNotNull(spans, "Spans should not be null");
        assertEquals(1, spans.size(), "Should have exactly 1 span");

        TextSpan span = spans.get(0);
        assertEquals("minecraft:stick", span.getItemId(), "Item ID should be minecraft:stick");
        assertEquals("", span.getContent(), "Content should be empty for self-closing tag");
    }

    @Test
    public void testClosingItemTag() {
        String markup = "<item id=minecraft:diamond></item>";
        List<TextSpan> spans = MarkupParser.parse(markup);

        assertNotNull(spans, "Spans should not be null");
        assertEquals(1, spans.size(), "Should have exactly 1 span");

        TextSpan span = spans.get(0);
        assertEquals("minecraft:diamond", span.getItemId(), "Item ID should be minecraft:diamond");
        assertEquals("", span.getContent(), "Content should be empty for closing tag");
    }

    @Test
    public void testItemTagWithCount() {
        String markup = "<item id=minecraft:emerald count=5/>";
        List<TextSpan> spans = MarkupParser.parse(markup);

        assertNotNull(spans, "Spans should not be null");
        assertEquals(1, spans.size(), "Should have exactly 1 span");

        TextSpan span = spans.get(0);
        assertEquals("minecraft:emerald", span.getItemId(), "Item ID should be minecraft:emerald");
        assertEquals(5, span.getItemCount(), "Item count should be 5");
    }

    @Test
    public void testItemTagWithUnderscore() {
        String markup = "<item id=minecraft:nether_star/>";
        List<TextSpan> spans = MarkupParser.parse(markup);

        assertNotNull(spans, "Spans should not be null");
        assertEquals(1, spans.size(), "Should have exactly 1 span");

        TextSpan span = spans.get(0);
        assertEquals("minecraft:nether_star", span.getItemId(), "Item ID should be minecraft:nether_star");
        assertEquals("", span.getContent(), "Content should be empty for self-closing tag");
    }

    @Test
    public void testItemTagWithTextBefore() {
        String markup = "Found: <item id=minecraft:diamond/>";
        List<TextSpan> spans = MarkupParser.parse(markup);

        assertNotNull(spans, "Spans should not be null");
        assertEquals(2, spans.size(), "Should have 2 spans (text + item)");

        TextSpan textSpan = spans.get(0);
        assertEquals("Found: ", textSpan.getContent(), "First span should be 'Found: '");
        assertNull(textSpan.getItemId(), "First span should not have item ID");

        TextSpan itemSpan = spans.get(1);
        assertEquals("minecraft:diamond", itemSpan.getItemId(), "Second span should have item ID");
        assertEquals("", itemSpan.getContent(), "Second span should have empty content");
    }

    @Test
    public void testItemTagWithTextAfter() {
        String markup = "<item id=minecraft:stick/> and stuff";
        List<TextSpan> spans = MarkupParser.parse(markup);

        assertNotNull(spans, "Spans should not be null");
        assertEquals(2, spans.size(), "Should have 2 spans (item + text)");

        TextSpan itemSpan = spans.get(0);
        assertEquals("minecraft:stick", itemSpan.getItemId(), "First span should have item ID");
        assertEquals("", itemSpan.getContent(), "First span should have empty content");

        TextSpan textSpan = spans.get(1);
        assertEquals(" and stuff", textSpan.getContent(), "Second span should be ' and stuff'");
        assertNull(textSpan.getItemId(), "Second span should not have item ID");
    }

    @Test
    public void testMultipleItemTags() {
        String markup = "<item id=minecraft:diamond/> + <item id=minecraft:stick/>";
        List<TextSpan> spans = MarkupParser.parse(markup);

        assertNotNull(spans, "Spans should not be null");
        assertEquals(3, spans.size(), "Should have 3 spans (item + text + item)");

        assertEquals("minecraft:diamond", spans.get(0).getItemId());
        assertEquals(" + ", spans.get(1).getContent());
        assertEquals("minecraft:stick", spans.get(2).getItemId());
    }
}
