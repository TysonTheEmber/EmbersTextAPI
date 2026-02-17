package net.tysontheember.emberstextapi.immersivemessages.api;

/**
 * 9-position screen anchor for positioning text on the screen.
 * Each anchor represents a normalized x/y pair where 0 is the
 * left/top of the screen and 1 is the right/bottom.
 */
public enum TextAnchor {
    TOP_LEFT(0f, 0f),
    TOP_CENTER(0.5f, 0f),
    TOP_RIGHT(1f, 0f),
    MIDDLE_LEFT(0f, 0.5f),
    MIDDLE(0.5f, 0.5f),
    MIDDLE_RIGHT(1f, 0.5f),
    BOTTOM_LEFT(0f, 1f),
    BOTTOM_CENTER(0.5f, 1f),
    BOTTOM_RIGHT(1f, 1f);

    public final float xFactor;
    public final float yFactor;

    TextAnchor(float xFactor, float yFactor) {
        this.xFactor = xFactor;
        this.yFactor = yFactor;
    }
}
