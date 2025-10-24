package net.tysontheember.emberstextapi.client.text;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

/**
 * Carries typewriter playback information for a component span.
 */
public final class TypewriterTrack {
    public static final TypewriterTrack OFF = new TypewriterTrack(Mode.OFF, 1.0f, null);

    private final Mode mode;
    private final float speedMultiplier;
    private final @Nullable String trackId;

    public TypewriterTrack(Mode mode, float speedMultiplier, @Nullable String trackId) {
        this.mode = Objects.requireNonNull(mode, "mode");
        this.speedMultiplier = speedMultiplier <= 0.0f ? 1.0f : speedMultiplier;
        this.trackId = trackId;
    }

    public Mode mode() {
        return this.mode;
    }

    public float speedMultiplier() {
        return this.speedMultiplier;
    }

    public @Nullable String trackId() {
        return this.trackId;
    }

    public boolean isActive() {
        return this.mode != Mode.OFF;
    }

    @Override
    public String toString() {
        return "TypewriterTrack{" + "mode=" + this.mode + ", speedMultiplier=" + this.speedMultiplier + ", trackId='" + this.trackId + '\'' + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.mode, this.speedMultiplier, this.trackId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TypewriterTrack track)) {
            return false;
        }
        return this.mode == track.mode && Float.compare(this.speedMultiplier, track.speedMultiplier) == 0 && Objects.equals(this.trackId, track.trackId);
    }

    public enum Mode {
        OFF,
        CHAR,
        WORD;

        public boolean isWordBased() {
            return this == WORD;
        }
    }
}
