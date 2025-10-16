package net.tysontheember.emberstextapi.overlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Groups overlay entries by tag to simplify rendering strategies.
 */
public final class OverlayBatches {
    private final Map<String, List<OverlayQueue.Entry>> batches = new HashMap<>();

    public void add(OverlayQueue.Entry entry) {
        batches.computeIfAbsent(entry.tag(), tag -> new ArrayList<>()).add(entry);
    }

    public Map<String, List<OverlayQueue.Entry>> batches() {
        return batches;
    }
}
