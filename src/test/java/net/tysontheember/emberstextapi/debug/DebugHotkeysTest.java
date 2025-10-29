package net.tysontheember.emberstextapi.debug;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class DebugHotkeysTest {
    private final List<DebugEvent> events = new ArrayList<>();
    private final Consumer<DebugEvent> listener = events::add;

    @BeforeEach
    void setUp() {
        DebugFlags.reloadFrom(false, false, false, false, false, false, false, 0, false, true, false, 2);
        events.clear();
        DebugEnvironment.getEventBus().register(listener);
    }

    @AfterEach
    void tearDown() {
        DebugEnvironment.getEventBus().unregister(listener);
        DebugFlags.reloadFrom(false, false, false, false, false, false, false, 0, false, true, false, 2);
    }

    @Test
    void overlayHotkeyTogglesOverlayAndDebug() {
        DebugHotkeys.triggerOverlayToggle();
        assertTrue(DebugFlags.isOverlayFlagEnabled());
        assertTrue(DebugFlags.isDebugEnabled());
        assertTrue(events.stream().anyMatch(event -> event instanceof DebugEvents.DebugModeChanged));
        DebugEvent last = events.get(events.size() - 1);
        assertTrue(last instanceof DebugEvents.OverlayVisibilityChanged);
        assertEquals("hotkey", ((DebugEvents.OverlayVisibilityChanged) last).getSource());
    }

    @Test
    void overlayHotkeyCyclesLevel() {
        for (int i = 0; i < DebugOverlay.MAX_LEVEL + 2; i++) {
            DebugHotkeys.triggerOverlayLevelCycle();
        }
        assertEquals((DebugOverlay.MAX_LEVEL + 2) % (DebugOverlay.MAX_LEVEL + 1), DebugFlags.getOverlayLevel());
        assertFalse(events.isEmpty());
        DebugEvent last = events.get(events.size() - 1);
        assertTrue(last instanceof DebugEvents.OverlayLevelChanged);
        assertEquals("hotkey", ((DebugEvents.OverlayLevelChanged) last).getSource());
    }
}
