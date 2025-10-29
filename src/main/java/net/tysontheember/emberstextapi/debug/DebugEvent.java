package net.tysontheember.emberstextapi.debug;

import java.time.Instant;
import java.util.Objects;

/**
 * Base type for debug events flowing through the {@link DebugEventBus}. Events are immutable
 * and timestamped when created.
 */
public abstract class DebugEvent {
    private final Instant timestamp;

    protected DebugEvent() {
        this.timestamp = Instant.now();
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Utility to format a human readable description of the event.
     */
    public abstract String describe();

    @Override
    public final String toString() {
        return describe();
    }

    @Override
    public final int hashCode() {
        return Objects.hash(timestamp, getClass());
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DebugEvent other = (DebugEvent) obj;
        return timestamp.equals(other.timestamp);
    }
}
