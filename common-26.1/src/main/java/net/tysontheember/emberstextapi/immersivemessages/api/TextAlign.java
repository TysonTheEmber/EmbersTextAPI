package net.tysontheember.emberstextapi.immersivemessages.api;

public enum TextAlign {
    LEFT(0f),
    CENTER(0.5f),
    RIGHT(1f);

    public final float xFactor;

    TextAlign(float xFactor) {
        this.xFactor = xFactor;
    }
}
