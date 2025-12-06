package net.tysontheember.emberstextapi.typewriter;

/**
 * Simple state for a single typewriter animation.
 */
public class TypewriterState {
    private int ticks = 0;
    private final int speedMs;
    private long lastAccessTime = System.currentTimeMillis();

    public TypewriterState(int speedMs) {
        this.speedMs = speedMs;
    }

    public void tick(int deltaTicks) {
        ticks += deltaTicks;
    }

    public void reset() {
        ticks = 0;
    }

    public boolean isCharVisible(int charIndex) {
        lastAccessTime = System.currentTimeMillis();
        // Calculate revealed chars: (ticks * 50ms) / speedMs
        int revealedChars = (ticks * 50) / speedMs;
        return charIndex < revealedChars;
    }

    public boolean isStale(long ttlMs) {
        return System.currentTimeMillis() - lastAccessTime > ttlMs;
    }

    public int getTicks() {
        return ticks;
    }
}
