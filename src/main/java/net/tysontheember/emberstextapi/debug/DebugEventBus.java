package net.tysontheember.emberstextapi.debug;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Minimal synchronous event bus for debug events. The implementation is intentionally simple
 * to avoid dependencies on Forge internals in the debug pipeline.
 */
public final class DebugEventBus {
    private final List<Consumer<DebugEvent>> listeners = new CopyOnWriteArrayList<>();

    public void register(Consumer<DebugEvent> listener) {
        listeners.add(listener);
    }

    public void unregister(Consumer<DebugEvent> listener) {
        listeners.remove(listener);
    }

    public void post(DebugEvent event) {
        for (Consumer<DebugEvent> listener : listeners) {
            listener.accept(event);
        }
    }
}
