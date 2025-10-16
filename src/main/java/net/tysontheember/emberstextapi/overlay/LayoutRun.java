package net.tysontheember.emberstextapi.overlay;

import net.minecraft.network.chat.Component;

/**
 * Represents a single laid-out text run.
 */
public final class LayoutRun {
    private final Component component;
    private final float x;
    private final float y;
    private final float scale;

    public LayoutRun(Component component, float x, float y, float scale) {
        this.component = component;
        this.x = x;
        this.y = y;
        this.scale = scale;
    }

    public Component component() {
        return component;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float scale() {
        return scale;
    }
}
