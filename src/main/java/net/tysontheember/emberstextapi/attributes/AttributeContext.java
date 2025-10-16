package net.tysontheember.emberstextapi.attributes;

import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.overlay.OverlayQueue;

/**
 * Context provided to attribute handlers when applying styles.
 */
public final class AttributeContext {
    private final boolean overlayEnabled;
    private final ResourceLocation source;
    private final OverlayQueue overlayQueue;

    public AttributeContext(boolean overlayEnabled, ResourceLocation source) {
        this(overlayEnabled, source, null);
    }

    public AttributeContext(boolean overlayEnabled, ResourceLocation source, OverlayQueue queue) {
        this.overlayEnabled = overlayEnabled;
        this.source = source;
        this.overlayQueue = queue;
    }

    public boolean overlayEnabled() {
        return overlayEnabled;
    }

    public ResourceLocation source() {
        return source;
    }

    public OverlayQueue overlayQueue() {
        return overlayQueue;
    }
}
