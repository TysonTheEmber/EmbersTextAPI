package net.tysontheember.emberstextapi.client.text;

import java.util.Objects;

/**
 * Describes typewriter playback state shared across render passes.
 */
public final class TypewriterTrack {
    private float progress;
    private float speedCps;
    private boolean wordMode;
    private long lastTick;
    private int targetLength;
    private final String key;

    public TypewriterTrack(String key, float speedCps, boolean wordMode, int targetLength) {
        this.key = key;
        this.speedCps = speedCps;
        this.wordMode = wordMode;
        this.targetLength = Math.max(0, targetLength);
        this.progress = this.targetLength <= 0 ? Float.MAX_VALUE : 0.0F;
        this.lastTick = -1L;
    }

    public String getKey() {
        return key;
    }

    public void updateParameters(float speed, boolean wordMode, int length) {
        boolean changed = this.wordMode != wordMode || this.targetLength != length;
        this.speedCps = speed;
        this.wordMode = wordMode;
        this.targetLength = Math.max(0, length);
        if (changed) {
            reset(-1L);
        }
    }

    public void reset(long tick) {
        this.progress = this.targetLength <= 0 ? Float.MAX_VALUE : 0.0F;
        this.lastTick = tick;
        if (tick <= 0L) {
            this.lastTick = -1L;
        }
    }

    public void tick(long clientTicks) {
        if (this.targetLength <= 0) {
            this.progress = Float.MAX_VALUE;
            this.lastTick = clientTicks;
            return;
        }
        if (this.lastTick < 0L) {
            this.lastTick = clientTicks;
            return;
        }
        long delta = clientTicks - this.lastTick;
        if (delta <= 0L) {
            return;
        }
        float increment = (this.speedCps / 20.0F) * delta;
        this.progress = Math.min(this.progress + increment, this.targetLength);
        this.lastTick = clientTicks;
    }

    public int allowCount() {
        if (this.targetLength <= 0) {
            return 0;
        }
        if (this.progress == Float.MAX_VALUE) {
            return this.targetLength;
        }
        int allowed = (int) Math.floor(this.progress + 1.0E-4F);
        return Math.max(0, Math.min(this.targetLength, allowed));
    }

    public boolean isWordMode() {
        return wordMode;
    }

    public int getTargetLength() {
        return targetLength;
    }

    public float getSpeedCps() {
        return speedCps;
    }

    public void setProgressComplete() {
        this.progress = this.targetLength;
    }

    @Override
    public String toString() {
        return "TypewriterTrack{" +
            "key='" + key + '\'' +
            ", progress=" + progress +
            ", speedCps=" + speedCps +
            ", wordMode=" + wordMode +
            ", targetLength=" + targetLength +
            '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TypewriterTrack other)) {
            return false;
        }
        return Objects.equals(this.key, other.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
