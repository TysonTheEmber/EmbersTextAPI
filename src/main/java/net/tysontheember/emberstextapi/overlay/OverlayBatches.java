package net.tysontheember.emberstextapi.overlay;

import net.tysontheember.emberstextapi.markup.RSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple container tracking overlay spans queued during rendering.
 */
public final class OverlayBatches {
    private final List<RSpan> spans = new ArrayList<>();

    public void add(RSpan span) {
        if (span != null) {
            spans.add(span);
        }
    }

    public List<RSpan> spans() {
        return Collections.unmodifiableList(spans);
    }

    public void clear() {
        spans.clear();
    }
}
