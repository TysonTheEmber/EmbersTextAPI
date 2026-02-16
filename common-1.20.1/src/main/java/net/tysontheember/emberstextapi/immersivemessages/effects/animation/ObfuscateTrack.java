package net.tysontheember.emberstextapi.immersivemessages.effects.animation;

/**
 * Minimal state holder for obfuscate animations so they persist across renders
 * (tooltips, GUI reopen) similarly to TypewriterTrack.
 */
public class ObfuscateTrack {
    public long startTimeMs;
    public long lastAccessMs;
    public int playCount;
    public boolean currentPlayCounted;
    public boolean completed;

    // Animation config/state
    public long completedAtMs;
    public long resetDelayMs;
    public long intervalMs;
    public boolean repeat;

    // Order/cache data (per-span)
    public int length;
    public int textHash;
    public String directionKey;
    public int[] order; // progression order -> char index
    public int[] ranks; // char index -> progression order

    // Track identity (set by ObfuscateTracks)
    public Object cacheKey;

    // Random mode state
    public java.util.Set<Integer> currentlyObfuscatedIndices; // which chars are obfuscated now
    public java.util.Map<Integer, Long> obfuscateUntilMs; // when each should reveal
    public long lastRandomUpdateMs; // when we last picked new random chars

    public ObfuscateTrack() {
        long now = System.currentTimeMillis();
        this.startTimeMs = 0;
        this.lastAccessMs = now;
        this.playCount = 0;
        this.currentPlayCounted = false;
        this.completed = false;
        this.completedAtMs = 0;
        this.resetDelayMs = 0;
        this.intervalMs = 20; // default ms per step (matches TypewriterConfig default)
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

    /**
     * Check if this track should reset based on time since last access.
     * Similar to TypewriterTrack.checkAndResetIfNeeded().
     *
     * @return true if track was reset
     */
    public boolean checkAndResetIfNeeded() {
        long now = System.currentTimeMillis();
        long timeSinceAccess = now - this.lastAccessMs;

        // If reset delay is configured and we've exceeded it, reset
        if (this.resetDelayMs > 0 && timeSinceAccess > this.resetDelayMs) {
            resetTimers();
            // Also clear random mode state
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
