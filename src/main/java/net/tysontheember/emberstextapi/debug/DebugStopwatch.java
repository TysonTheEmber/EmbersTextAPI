package net.tysontheember.emberstextapi.debug;

/**
 * Lightweight stopwatch used by the debug overlay to report instrumentation metrics.
 * The implementation intentionally avoids allocations so it can be used inside hot paths
 * once performance tracing is enabled.
 */
public final class DebugStopwatch {
    private long startNanos;
    private long elapsedNanos;
    private boolean running;

    public DebugStopwatch() {
        this.startNanos = -1L;
        this.elapsedNanos = 0L;
        this.running = false;
    }

    public void reset() {
        startNanos = -1L;
        elapsedNanos = 0L;
        running = false;
    }

    public void start() {
        if (running) {
            return;
        }
        running = true;
        startNanos = System.nanoTime();
    }

    public void stop() {
        if (!running) {
            return;
        }
        long end = System.nanoTime();
        elapsedNanos += Math.max(0L, end - startNanos);
        running = false;
        startNanos = -1L;
    }

    public long getElapsedNanos() {
        if (running) {
            long now = System.nanoTime();
            return elapsedNanos + Math.max(0L, now - startNanos);
        }
        return elapsedNanos;
    }

    public double getElapsedMillis() {
        return getElapsedNanos() / 1_000_000.0D;
    }

    public boolean isRunning() {
        return running;
    }
}
