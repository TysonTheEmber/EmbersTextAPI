package net.tysontheember.emberstextapi.attributes;

import net.tysontheember.emberstextapi.overlay.OverlayQueue;

import javax.annotation.Nullable;

/**
 * Shared contextual data passed to attribute handlers.
 */
public final class AttributeContext {
    private final boolean overlayEnabled;
    @Nullable
    private final OverlayQueue overlayQueue;

    private AttributeContext(boolean overlayEnabled, @Nullable OverlayQueue overlayQueue) {
        this.overlayEnabled = overlayEnabled;
        this.overlayQueue = overlayQueue;
    }

    public static AttributeContext vanillaOnly() {
        return new AttributeContext(false, null);
    }

    public static AttributeContext withOverlay(OverlayQueue queue) {
        return new AttributeContext(true, queue);
    }

    public boolean overlayEnabled() {
        return overlayEnabled;
    }

    @Nullable
    public OverlayQueue overlayQueue() {
        return overlayQueue;
    }
}
