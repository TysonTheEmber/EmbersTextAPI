package net.tysontheember.emberstextapi.attributes;

import net.tysontheember.emberstextapi.markup.RSpan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Context object shared between attribute handlers and emitters.
 */
public final class AttributeContext {
    private final List<RSpan> overlaySpans = new ArrayList<>();

    public void markOverlay(RSpan span) {
        if (span != null) {
            overlaySpans.add(span);
        }
    }

    public boolean requiresOverlay() {
        return !overlaySpans.isEmpty();
    }

    public List<RSpan> overlaySpans() {
        return Collections.unmodifiableList(overlaySpans);
    }
}
