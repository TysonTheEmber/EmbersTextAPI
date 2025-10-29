package net.tysontheember.emberstextapi.debug;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DebugConfigTest {

    @Test
    void defaultsAreApplied() {
        DebugFlags.reload();
        assertFalse(DebugFlags.isDebugEnabled());
        for (DebugFlags.TraceChannel channel : DebugFlags.TraceChannel.values()) {
            assertFalse(DebugFlags.isTraceEnabled(channel), "Trace channel should be disabled by default: " + channel);
        }
        assertFalse(DebugFlags.isOverlayEnabled());
        assertEquals(0, DebugFlags.getOverlayLevel());
        assertFalse(DebugFlags.isPerfEnabled());
        assertTrue(DebugFlags.isFailSafeOnError());
        assertFalse(DebugFlags.isSpanEverywhere());
        assertEquals(2, DebugFlags.getEffectsVersion());
    }

    @Test
    void reloadReadsConfigValues() {
        DebugFlags.reloadFrom(
                true,  // debug enabled
                true,  // parse trace
                false,
                false,
                false,
                false,
                true,  // overlay enabled
                2,
                true,  // perf enabled
                false, // fail safe
                true,  // span everywhere
                5);

        assertTrue(DebugFlags.isDebugEnabled());
        assertTrue(DebugFlags.isTraceEnabled(DebugFlags.TraceChannel.PARSE));
        assertTrue(DebugFlags.isOverlayEnabled());
        assertEquals(2, DebugFlags.getOverlayLevel());
        assertTrue(DebugFlags.isPerfEnabled());
        assertFalse(DebugFlags.isFailSafeOnError());
        assertTrue(DebugFlags.isSpanEverywhere());
        assertEquals(5, DebugFlags.getEffectsVersion());

        // Reset to defaults for other tests
        DebugFlags.reloadFrom(false, false, false, false, false, false, false, 0, false, true, false, 2);
    }
}
