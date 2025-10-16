package net.tysontheember.emberstextapi.overlay;

import javax.annotation.Nullable;

/**
 * Options controlling how fallback rendering behaves. The advanced overlay
 * renderer currently uses these values for layout hints.
 */
public record DrawOptions(float scale, boolean shadow, int colorOverride, @Nullable Integer wrapWidth) {
    public static DrawOptions defaults() {
        return new DrawOptions(1.0f, true, 0xFFFFFFFF, null);
    }
}
