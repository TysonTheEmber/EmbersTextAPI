package net.tysontheember.emberstextapi.immersivemessages.effects.animation;

public class ObfuscateTrack {
    public long startTimeMs;
    public long lastAccessMs;
    public int playCount;
    public boolean currentPlayCounted;
    public boolean completed;

    public long completedAtMs;
    public long resetDelayMs;
    public long intervalMs;
    public boolean repeat;

    public int length;
    public int textHash;
    public String directionKey;
    public int[] order;
    public int[] ranks;

    public Object cacheKey;

    public java.util.Set<Integer> currentlyObfuscatedIndices;
    public java.util.Map<Integer, Long> obfuscateUntilMs;
    public long lastRandomUpdateMs;

    public ObfuscateTrack() {
        long now = System.currentTimeMillis();
        this.startTimeMs = 0;
        this.lastAccessMs = now;
        this.playCount = 0;
        this.currentPlayCounted = false;
        this.completed = false;
        this.completedAtMs = 0;
        this.resetDelayMs = 0;
        this.intervalMs = 20;
        this.repeat = false;
        this.length = 0;
        this.textHash = 0;
        this.directionKey = "";
        this.order = null;
        this.ranks = null;
        this.cacheKey = null;
        this.currentlyObfuscatedIndices = new java.util.HashSet<>();
        this.obfuscateUntilMs = new java.util.HashMap<>();
        this.lastRandomUpdateMs = 0;
    }

    public void resetTimers() {
        long now = System.currentTimeMillis();
        this.startTimeMs = now;
        this.lastAccessMs = now;
        this.currentPlayCounted = false;
        this.completed = false;
        this.completedAtMs = 0;
    }

    public boolean checkAndResetIfNeeded() {
        long now = System.currentTimeMillis();
        long timeSinceAccess = now - this.lastAccessMs;

        if (this.resetDelayMs > 0 && timeSinceAccess > this.resetDelayMs) {
            resetTimers();

            if (this.currentlyObfuscatedIndices != null) {
                this.currentlyObfuscatedIndices.clear();
            }
            if (this.obfuscateUntilMs != null) {
                this.obfuscateUntilMs.clear();
            }
            this.lastRandomUpdateMs = 0;
            return true;
        }

        return false;
    }
}
