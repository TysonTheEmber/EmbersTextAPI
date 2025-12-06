package net.tysontheember.emberstextapi.typewriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple controller for typewriter animations.
 */
public class TypewriterController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypewriterController.class);
    private static final TypewriterController INSTANCE = new TypewriterController();

    private final Map<String, TypewriterState> states = new ConcurrentHashMap<>();
    private long lastTickTime = System.currentTimeMillis();

    private TypewriterController() {}

    public static TypewriterController getInstance() {
        return INSTANCE;
    }

    /**
     * Get or create a typewriter state for the given context.
     */
    public TypewriterState getState(String context, int speedMs) {
        return states.computeIfAbsent(context, k -> new TypewriterState(speedMs));
    }

    /**
     * Reset animation for a context (used when tooltip/screen reopens).
     */
    public void resetContext(String context) {
        TypewriterState state = states.get(context);
        if (state != null) {
            state.reset();
            LOGGER.debug("Reset typewriter for context: {}", context);
        }
    }

    /**
     * Tick all active typewriter states.
     */
    public void tick() {
        long now = System.currentTimeMillis();
        int deltaTicks = (int) ((now - lastTickTime) / 50);

        if (deltaTicks > 0) {
            lastTickTime = now;
            states.values().forEach(state -> state.tick(deltaTicks));
        }

        // Cleanup stale states periodically
        if (states.size() > 100) {
            states.entrySet().removeIf(e -> e.getValue().isStale(60000));
        }
    }

    /**
     * Remove a context (used when tooltip/screen closes).
     */
    public void removeContext(String context) {
        states.remove(context);
    }

    public int getStateCount() {
        return states.size();
    }
}
