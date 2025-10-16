package net.tysontheember.emberstextapi.overlay;

import net.tysontheember.emberstextapi.markup.RNode.RSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Basic queue that accumulates overlay runs per frame.
 */
public final class OverlayQueue {
    private final List<Entry> entries = new ArrayList<>();

    public void enqueue(String tag, RSpan span, LayoutRun run) {
        entries.add(new Entry(tag, span, run));
    }

    public List<Entry> entries() {
        return Collections.unmodifiableList(entries);
    }

    public void clear() {
        entries.clear();
    }

    public record Entry(String tag, RSpan span, LayoutRun run) {
    }
}
