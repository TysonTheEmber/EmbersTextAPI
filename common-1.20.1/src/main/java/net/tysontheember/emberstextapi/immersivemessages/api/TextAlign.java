package net.tysontheember.emberstextapi.immersivemessages.api;

/**
 * Horizontal text alignment within an anchored position.
 * Controls how text is aligned relative to its anchor point.
 */
public enum TextAlign {
    LEFT(0f),
    CENTER(0.5f),
    RIGHT(1f);

    public final float xFactor;

    TextAlign(float xFactor) {
        this.xFactor = xFactor;
    }
}
