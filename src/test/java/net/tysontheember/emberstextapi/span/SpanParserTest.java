package net.tysontheember.emberstextapi.span;

import net.tysontheember.emberstextapi.debug.DebugEvent;
import net.tysontheember.emberstextapi.debug.DebugEventBus;
import net.tysontheember.emberstextapi.debug.DebugEvents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpanParserTest {
    private SpanParser parser;
    private DebugEventBus bus;
    private List<DebugEvent> events;

    @BeforeEach
    void setUp() {
        parser = new SpanParser();
        bus = new DebugEventBus();
        events = new ArrayList<>();
        bus.register(events::add);
    }

    @Test
    void plainTextBypassesParser() {
        SpanDocument document = parser.parse("Hello world", bus);
        assertTrue(document.noSpans());
        assertEquals("Hello world", document.flattenText());
        assertEquals(2, events.size());
        assertTrue(events.get(0) instanceof DebugEvents.ParseStarted);
        assertTrue(events.get(1) instanceof DebugEvents.ParseCompleted);
    }

    @Test
    void parsesSimpleSpan() {
        SpanDocument document = parser.parse("<color=#ff0000>Hi</color>", bus);
        assertFalse(document.noSpans());
        assertEquals("Hi", document.flattenText());
        assertEquals(1, document.getChildren().size());
        assertTrue(document.getChildren().get(0) instanceof SpanElementNode);
        SpanElementNode element = (SpanElementNode) document.getChildren().get(0);
        assertEquals("color", element.getTagName());
        assertEquals("#ff0000", element.getAttributes().get("value"));
        assertEquals(1, element.getChildren().size());
        assertTrue(element.getChildren().get(0) instanceof TextRunNode);
    }

    @Test
    void supportsSelfClosingTag() {
        SpanDocument document = parser.parse("Start<br/>End", bus);
        assertFalse(document.noSpans());
        assertEquals("StartEnd", document.flattenText());
        assertEquals(3, document.getChildren().size());
        assertTrue(document.getChildren().get(1) instanceof SpanElementNode);
        SpanElementNode element = (SpanElementNode) document.getChildren().get(1);
        assertEquals("br", element.getTagName());
        assertTrue(element.getChildren().isEmpty());
    }

    @Test
    void decodesEntitiesInText() {
        SpanDocument document = parser.parse("1 &lt; 2 &amp; 3", bus);
        assertEquals("1 < 2 & 3", document.flattenText());
        assertTrue(document.noSpans());
    }

    @Test
    void recoversFromUnexpectedClosing() {
        SpanDocument document = parser.parse("Hello</oops>", bus);
        assertEquals("Hello</oops>", document.flattenText());
        assertTrue(events.stream().anyMatch(event -> event instanceof DebugEvents.ParseRecovered));
        assertTrue(document.noSpans());
    }
}
