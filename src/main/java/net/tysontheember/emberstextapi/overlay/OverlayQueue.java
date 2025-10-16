package net.tysontheember.emberstextapi.overlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thread-local queue holding overlay rendering tasks.
 */
public final class OverlayQueue {
    private final List<OverlayTask> tasks = new ArrayList<>();

    public void add(OverlayTask task) {
        tasks.add(task);
    }

    public List<OverlayTask> tasks() {
        return Collections.unmodifiableList(tasks);
    }

    public void clear() {
        tasks.clear();
    }

    public record OverlayTask(String effect, LayoutRun run, Object payload) {
    }
}
