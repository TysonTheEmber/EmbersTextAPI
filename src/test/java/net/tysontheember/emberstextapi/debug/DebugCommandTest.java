package net.tysontheember.emberstextapi.debug;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class DebugCommandTest {
    private final List<DebugEvent> events = new ArrayList<>();
    private final Consumer<DebugEvent> listener = events::add;

    @BeforeEach
    void setUp() {
        events.clear();
        DebugFlags.reloadFrom(false, false, false, false, false, false, false, 0, false, true, false, 2);
        DebugEnvironment.getEventBus().register(listener);
    }

    @AfterEach
    void tearDown() {
        DebugEnvironment.getEventBus().unregister(listener);
        DebugFlags.reloadFrom(false, false, false, false, false, false, false, 0, false, true, false, 2);
        events.clear();
    }

    @Test
    void debugToggleEmitsEvents() {
        DebugCommand.applyDebugToggle(true, "test");
        assertTrue(DebugFlags.isDebugEnabled());
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof DebugEvents.DebugModeChanged);
        assertEquals("test", ((DebugEvents.DebugModeChanged) events.get(0)).getSource());

        DebugCommand.applyDebugToggle(false, "test");
        assertFalse(DebugFlags.isDebugEnabled());
        assertEquals(2, events.size());
        assertTrue(events.get(1) instanceof DebugEvents.DebugModeChanged);
        assertEquals("test", ((DebugEvents.DebugModeChanged) events.get(1)).getSource());
    }

    @Test
    void overlayToggleEnablesDebug() {
        DebugCommand.applyOverlayToggle(true, "test");
        assertTrue(DebugFlags.isOverlayFlagEnabled());
        assertTrue(DebugFlags.isDebugEnabled());
        assertTrue(events.stream().anyMatch(event -> event instanceof DebugEvents.DebugModeChanged));
        assertTrue(events.stream().anyMatch(event -> event instanceof DebugEvents.OverlayVisibilityChanged));
        DebugEvents.OverlayVisibilityChanged overlayEvent = (DebugEvents.OverlayVisibilityChanged) events.stream()
                .filter(event -> event instanceof DebugEvents.OverlayVisibilityChanged)
                .reduce((first, second) -> second)
                .orElseThrow();
        assertEquals("test", overlayEvent.getSource());

        DebugCommand.applyOverlayToggle(false, "test");
        assertFalse(DebugFlags.isOverlayFlagEnabled());
        assertTrue(DebugFlags.isDebugEnabled());
    }

    @Test
    void overlayLevelClampsAndEmitsEvent() {
        DebugCommand.applyOverlayLevel(5, "test");
        assertEquals(3, DebugFlags.getOverlayLevel());
        assertFalse(events.isEmpty());
        DebugEvent last = events.get(events.size() - 1);
        assertTrue(last instanceof DebugEvents.OverlayLevelChanged);
        assertEquals(3, ((DebugEvents.OverlayLevelChanged) last).getLevel());
    }

    @Test
    void traceTogglePersists() {
        DebugCommand.applyTraceToggle(DebugFlags.TraceChannel.PARSE, true, "test");
        assertTrue(DebugFlags.getTraceFlag(DebugFlags.TraceChannel.PARSE));
        DebugEvents.TraceChannelToggled traceEvent = (DebugEvents.TraceChannelToggled) events.get(events.size() - 1);
        assertEquals("test", traceEvent.getSource());
    }

    @Test
    void perfToggleUpdatesFlag() {
        DebugCommand.applyPerfToggle(true, "test");
        assertTrue(DebugFlags.isPerfFlagEnabled());
        DebugCommand.applyPerfToggle(false, "test");
        assertFalse(DebugFlags.isPerfFlagEnabled());
        DebugEvents.PerfModeChanged perfEvent = (DebugEvents.PerfModeChanged) events.get(events.size() - 1);
        assertEquals("test", perfEvent.getSource());
    }

    @Test
    void failSafeToggleUpdatesFlag() {
        DebugCommand.applyFailSafeToggle(false, "test");
        assertFalse(DebugFlags.isFailSafeOnError());
        DebugCommand.applyFailSafeToggle(true, "test");
        assertTrue(DebugFlags.isFailSafeOnError());
        DebugEvents.FailSafeModeChanged failSafeEvent = (DebugEvents.FailSafeModeChanged) events.get(events.size() - 1);
        assertEquals("test", failSafeEvent.getSource());
    }

    @Test
    void dumpRequestEmitsEvent() {
        DebugCommand.requestDump("test");
        assertEquals(1, events.size());
        DebugEvents.DebugDumpRequested dumpEvent = (DebugEvents.DebugDumpRequested) events.get(0);
        assertEquals("test", dumpEvent.getSource());
    }
}
