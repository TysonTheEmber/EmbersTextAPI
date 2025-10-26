package net.tysontheember.emberstextapi.core.style;

import java.util.Objects;

/**
 * Captures the runtime configuration for typewriter effects.
 */
public final class TypewriterState {
    private final float speed;
    private final boolean centered;
    private final int track;
    private final int index;

    public TypewriterState(float speed, boolean centered, int track, int index) {
        this.speed = speed;
        this.centered = centered;
        this.track = track;
        this.index = index;
    }

    public float speed() {
        return speed;
    }

    public boolean centered() {
        return centered;
    }

    public int track() {
        return track;
    }

    public int index() {
        return index;
    }

    public TypewriterState withIndex(int newIndex) {
        return new TypewriterState(speed, centered, track, newIndex);
    }

    public TypewriterState withTrack(int newTrack) {
        return new TypewriterState(speed, centered, newTrack, index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypewriterState that)) {
            return false;
        }
        return Float.compare(speed, that.speed) == 0
                && centered == that.centered
                && track == that.track
                && index == that.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(speed, centered, track, index);
    }

    @Override
    public String toString() {
        return "TypewriterState{" +
                "speed=" + speed +
                ", centered=" + centered +
                ", track=" + track +
                ", index=" + index +
                '}';
    }
}
