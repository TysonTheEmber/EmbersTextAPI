package net.tysontheember.emberstextapi.debug;

/**
 * Global accessor for debug-related state. Acts as the handshake point between the mod entry
 * point and the rest of the span system.
 */
public final class DebugEnvironment {
    private static final DebugEnvironment INSTANCE = new DebugEnvironment();
    private static final DebugEventBus EVENT_BUS = new DebugEventBus();

    private DebugEnvironment() {
    }

    public static DebugEnvironment get() {
        return INSTANCE;
    }

    public static DebugEventBus getEventBus() {
        return EVENT_BUS;
    }

    public boolean isEnabled() {
        return DebugFlags.isDebugEnabled();
    }

    public boolean isTraceEnabled(DebugFlags.TraceChannel channel) {
        return DebugFlags.isTraceEnabled(channel);
    }

    public boolean isOverlayEnabled() {
        return DebugFlags.isOverlayEnabled();
    }

    public boolean isPerfEnabled() {
        return DebugFlags.isPerfEnabled();
    }

    public boolean isSpanEverywhere() {
        return DebugFlags.isSpanEverywhere();
    }

    public boolean isFailSafeOnError() {
        return DebugFlags.isFailSafeOnError();
    }

    public int getEffectsVersion() {
        return DebugFlags.getEffectsVersion();
    }
}
