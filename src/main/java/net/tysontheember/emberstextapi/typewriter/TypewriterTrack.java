package net.tysontheember.emberstextapi.typewriter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeSet;

/**
 * Represents the animation state for a single typewriter effect instance.
 * <p>
 * Each {@code <typewriter>} tag in a unique context gets its own track,
 * which manages timing, revealed character count, and optional sound playback.
 * </p>
 * <p>
 * Timing is wall-clock based (milliseconds) rather than tick-based, providing
 * smooth animation independent of frame rate or tick rate.
 * </p>
 *
 * @see TypewriterTracks
 */
public class TypewriterTrack {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypewriterTrack.class);

    /** Minimum interval between sound plays to prevent audio spam. */
    private static final long MIN_SOUND_INTERVAL_MS = 30;

    /** Timestamp when this track was created/reset (milliseconds). */
    public long startedAt;

    /** Timestamp when the index was last advanced (milliseconds). */
    public long changedSince;

    /** Current number of revealed characters. */
    public int index;

    /** Milliseconds per character (configurable per track). */
    private int interval;

    /** Sound resource ID to play per character, or null for silent. */
    private String sound;

    /** Timestamp of last sound play to enforce minimum interval. */
    private long lastSoundMs;

    /** Render counter for shadow pass - increments for each character processed. */
    private int shadowRenderCounter;

    /** Render counter for main pass - increments for each character processed. */
    private int mainRenderCounter;

    /** Last frame time for shadow render counter reset detection. */
    private long lastShadowRenderFrame;

    /** Last frame time for main render counter reset detection. */
    private long lastMainRenderFrame;

    /** Last time this track was accessed (for reset delay checking). */
    private long lastAccessTime;

    /** Reset delay in milliseconds. When access gap exceeds this, track resets. Default 1000ms. */
    private long resetDelayMs;

    /** Position ordinals seen in current frame (for position-based ordering). */
    private TreeSet<Integer> currentFramePositions;

    /** Position ordinals from previous frame (used for stable index computation). */
    private TreeSet<Integer> previousFramePositions;

    /** Last frame time for position tracking reset. */
    private long lastPositionFrame;

    /** Cached array of sorted positions from previous frame for fast index lookup. */
    private int[] sortedPositionsCache;

    /**
     * Maximum number of times to play the animation.
     * -1 = infinite, 1 = play once, N = play N times.
     */
    private int maxPlays;

    /**
     * Number of times the animation has completed (index reached totalChars).
     */
    private int playCount;

    /**
     * Total number of characters in the text. Used to detect when a play completes.
     * -1 means unknown (not yet set).
     */
    private int totalChars;

    /**
     * Whether we've already counted the current play as complete.
     * Prevents counting the same play multiple times.
     */
    private boolean currentPlayCounted;

    /**
     * The cache key for this track, used to mark completion in TypewriterTracks.
     * Set by TypewriterTracks when creating the track.
     */
    private Object cacheKey;

    /**
     * Create a new typewriter track with default settings.
     * <p>
     * Initializes with current time and uses the global default speed.
     * </p>
     */
    public TypewriterTrack() {
        long now = System.currentTimeMillis();
        this.startedAt = now;
        this.changedSince = now;
        this.index = 0;
        this.interval = TypewriterConfig.getDefaultSpeedMs();
        this.sound = null;
        this.lastSoundMs = 0;
        this.shadowRenderCounter = 0;
        this.mainRenderCounter = 0;
        this.lastShadowRenderFrame = 0;
        this.lastMainRenderFrame = 0;
        this.lastAccessTime = now;
        this.resetDelayMs = 1000; // Default 1 second
        this.currentFramePositions = new TreeSet<>();
        this.previousFramePositions = new TreeSet<>();
        this.lastPositionFrame = 0;
        this.sortedPositionsCache = new int[0];
        this.maxPlays = TypewriterConfig.getDefaultMaxPlays();
        this.playCount = 0;
        this.totalChars = -1;
        this.currentPlayCounted = false;
    }

    /**
     * Create a track that's already in a completed state.
     * <p>
     * Used when restoring a track for a key that was previously marked as completed.
     * This ensures the animation doesn't replay even after cache expiration.
     * </p>
     *
     * @return a track with completed state (all text visible, won't replay)
     */
    public static TypewriterTrack createCompleted() {
        TypewriterTrack track = new TypewriterTrack();
        track.maxPlays = 1;
        track.playCount = 1;
        track.currentPlayCounted = true;
        track.index = Integer.MAX_VALUE; // Ensure all characters are visible
        return track;
    }

    /**
     * Update the track state based on elapsed time.
     * <p>
     * Advances the revealed character count based on time elapsed since last change.
     * Each interval that passes reveals one additional character. Sound is played
     * when characters are revealed if configured.
     * </p>
     * <p>
     * This method should be called during rendering to update the animation state.
     * </p>
     * <p>
     * This method is synchronized to ensure thread-safe updates when called from
     * multiple render passes (shadow and main).
     * </p>
     */
    public synchronized void update() {
        long now = System.currentTimeMillis();
        int previousIndex = index;

        // Advance one character per interval elapsed
        while (now - changedSince >= interval) {
            changedSince += interval;
            index++;
        }

        // Play sound if index advanced and sound is configured
        if (index > previousIndex && sound != null) {
            if (now - lastSoundMs >= MIN_SOUND_INTERVAL_MS) {
                playSound();
                lastSoundMs = now;
            }
        }

        // Check if this play has completed (all characters revealed)
        if (totalChars > 0 && index >= totalChars && !currentPlayCounted) {
            playCount++;
            currentPlayCounted = true;
            LOGGER.debug("Play completed: playCount={}, maxPlays={}, totalChars={}, index={}",
                    playCount, maxPlays, totalChars, index);

            // If we've reached max plays, mark this key as permanently completed
            if (maxPlays != -1 && playCount >= maxPlays && cacheKey != null) {
                LOGGER.debug("Max plays reached, marking as completed");
                TypewriterTracks.getInstance().markCompleted(cacheKey);
            }
        }
    }

    /**
     * Set the interval (milliseconds per character) for this track.
     *
     * @param ms milliseconds between character reveals (minimum 1)
     */
    public void setInterval(int ms) {
        this.interval = Math.max(1, ms);
    }

    /**
     * Get the current interval setting.
     *
     * @return milliseconds per character
     */
    public int getInterval() {
        return interval;
    }

    /**
     * Get the configured sound resource ID.
     *
     * @return sound resource ID, or null if silent
     */
    public String getSound() {
        return sound;
    }

    /**
     * Set the sound to play when each character is revealed.
     * <p>
     * Use a Minecraft resource location string like "minecraft:block.note_block.hat"
     * or "minecraft:ui.button.click". Pass null or "off" to disable sound.
     * </p>
     *
     * @param sound sound resource ID, null, or "off" to disable
     */
    public void setSound(String sound) {
        if (sound == null || "off".equalsIgnoreCase(sound) || sound.isEmpty()) {
            this.sound = null;
        } else {
            this.sound = sound;
        }
    }

    /**
     * Set the reset delay in milliseconds.
     * <p>
     * When the track isn't accessed for longer than this delay,
     * it will automatically reset on next access.
     * </p>
     *
     * @param delayMs reset delay in milliseconds (minimum 0)
     */
    public void setResetDelayMs(long delayMs) {
        this.resetDelayMs = Math.max(0, delayMs);
    }

    /**
     * Get the current reset delay in milliseconds.
     *
     * @return reset delay in milliseconds
     */
    public long getResetDelayMs() {
        return resetDelayMs;
    }

    /**
     * Check if the track should reset based on time since last access.
     * <p>
     * If the time since last access exceeds the reset delay, this method
     * resets the track and returns true. However, if max plays has been
     * reached, the track will not reset.
     * </p>
     *
     * @return true if the track was reset, false otherwise
     */
    public boolean checkAndResetIfNeeded() {
        long now = System.currentTimeMillis();
        long timeSinceAccess = now - lastAccessTime;

        if (timeSinceAccess > resetDelayMs) {
            // Don't reset if we've reached max plays
            if (maxPlays != -1 && playCount >= maxPlays) {
                lastAccessTime = now;
                return false;
            }
            reset();
            return true;
        }

        // Update last access time
        lastAccessTime = now;
        return false;
    }

    /**
     * Check if the animation has completed all allowed plays.
     * <p>
     * When this returns true, all characters should remain visible
     * and the animation should not restart.
     * </p>
     *
     * @return true if max plays reached and animation is complete
     */
    public boolean isCompleted() {
        return maxPlays != -1 && playCount >= maxPlays && currentPlayCounted;
    }

    /**
     * Reset the track to start the animation over.
     * <p>
     * Used for repeat mode when the UI element reappears.
     * Note: playCount is NOT reset - it persists to track total plays.
     * </p>
     */
    public void reset() {
        long now = System.currentTimeMillis();
        this.startedAt = now;
        this.changedSince = now;
        this.index = 0;
        this.shadowRenderCounter = 0;
        this.mainRenderCounter = 0;
        this.lastAccessTime = now;
        this.currentFramePositions.clear();
        this.previousFramePositions.clear();
        this.sortedPositionsCache = new int[0];
        this.currentPlayCounted = false; // Allow next play to be counted
        // Note: playCount is intentionally NOT reset
    }

    /**
     * Get the maximum number of plays allowed.
     *
     * @return -1 for infinite, or a positive number
     */
    public int getMaxPlays() {
        return maxPlays;
    }

    /**
     * Set the maximum number of plays allowed.
     * <p>
     * Use -1 for infinite repeats, or a positive number to limit plays.
     * </p>
     *
     * @param maxPlays -1 for infinite, or a positive number
     */
    public void setMaxPlays(int maxPlays) {
        this.maxPlays = maxPlays < 0 ? -1 : Math.max(1, maxPlays);
    }

    /**
     * Get the total number of characters in the text.
     *
     * @return total characters, or -1 if unknown
     */
    public int getTotalChars() {
        return totalChars;
    }

    /**
     * Set the total number of characters in the text.
     * <p>
     * This is used to detect when a play has completed.
     * </p>
     *
     * @param totalChars total character count
     */
    public void setTotalChars(int totalChars) {
        this.totalChars = Math.max(0, totalChars);
    }

    /**
     * Get the number of times the animation has completed.
     *
     * @return play count
     */
    public int getPlayCount() {
        return playCount;
    }

    /**
     * Set the cache key for this track.
     * <p>
     * This is used to mark the track as permanently completed in TypewriterTracks
     * when max plays is reached.
     * </p>
     *
     * @param key the cache key
     */
    public void setCacheKey(Object key) {
        this.cacheKey = key;
    }

    /**
     * Get the next render index for a character being rendered.
     * <p>
     * This counter resets at the start of each frame and increments
     * for each character rendered. It's used to assign sequential
     * indices to characters across line wraps.
     * </p>
     * <p>
     * Separate counters are maintained for shadow and main passes
     * so both passes see the same sequence of indices.
     * </p>
     * <p>
     * Frame detection uses a time threshold (1ms) to group characters
     * rendered close together as part of the same frame.
     * </p>
     * <p>
     * This method is synchronized to ensure thread-safe counter updates.
     * </p>
     *
     * @param frameTime current frame time for reset detection (use System.nanoTime())
     * @param isShadow whether this is the shadow pass
     * @return the render index for this character
     */
    public synchronized int nextRenderIndex(long frameTime, boolean isShadow) {
        // Use 1ms threshold for frame detection (1,000,000 nanoseconds)
        // Characters rendered within 1ms are considered part of the same frame
        long frameThresholdNs = 1_000_000;

        if (isShadow) {
            // Reset shadow counter at frame boundaries
            if (Math.abs(frameTime - lastShadowRenderFrame) > frameThresholdNs) {
                shadowRenderCounter = 0;
                lastShadowRenderFrame = frameTime;
            }
            return shadowRenderCounter++;
        } else {
            // Reset main counter at frame boundaries
            if (Math.abs(frameTime - lastMainRenderFrame) > frameThresholdNs) {
                mainRenderCounter = 0;
                lastMainRenderFrame = frameTime;
            }
            return mainRenderCounter++;
        }
    }

    /**
     * Get the sequential ordinal for a position-based ordinal.
     * <p>
     * This method maps position ordinals (computed from Y*10000+X) to sequential
     * indices (0, 1, 2, 3...) based on sorted position order. This ensures
     * characters are revealed in visual reading order (top-to-bottom, left-to-right)
     * regardless of render order.
     * </p>
     * <p>
     * Uses a two-frame approach: positions from the previous frame are used to
     * compute stable indices for the current frame. This handles arbitrary render
     * order (e.g., chat rendering bottom-to-top).
     * </p>
     * <p>
     * This method is synchronized to ensure thread-safe position tracking.
     * </p>
     *
     * @param positionOrdinal position-based ordinal (Y*10000+X)
     * @param frameTime current frame time for reset detection (use System.nanoTime())
     * @return sequential index based on position order
     */
    public synchronized int getSequentialOrdinal(int positionOrdinal, long frameTime) {
        // Use 1ms threshold for frame detection
        long frameThresholdNs = 1_000_000;

        // Check for frame boundary
        if (Math.abs(frameTime - lastPositionFrame) > frameThresholdNs) {
            // Swap current to previous, start fresh current
            previousFramePositions.clear();
            previousFramePositions.addAll(currentFramePositions);
            currentFramePositions.clear();
            lastPositionFrame = frameTime;

            // Build cache for fast lookup
            sortedPositionsCache = previousFramePositions.stream().mapToInt(Integer::intValue).toArray();
        }

        // Always add to current frame for next frame's reference
        currentFramePositions.add(positionOrdinal);

        // Look up index in previous frame's sorted positions
        if (sortedPositionsCache.length > 0) {
            // Binary search for the position
            int idx = java.util.Arrays.binarySearch(sortedPositionsCache, positionOrdinal);
            if (idx >= 0) {
                return idx; // Exact match
            }
            // Not found - use insertion point as approximate index
            // This handles new positions not seen in previous frame
            return Math.min(-idx - 1, sortedPositionsCache.length - 1);
        }

        // First frame - fall back to counting positions seen so far this frame
        // This gives reasonable ordering on the first frame
        return currentFramePositions.headSet(positionOrdinal, true).size() - 1;
    }

    /**
     * Play the configured sound effect.
     */
    private void playSound() {
        if (sound == null) {
            return;
        }

        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.getSoundManager() != null) {
                ResourceLocation soundId = ResourceLocation.tryParse(sound);
                if (soundId == null) {
                    LOGGER.debug("Invalid typewriter sound ID '{}', disabling", sound);
                    this.sound = null;
                    return;
                }
                SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(soundId);
                SimpleSoundInstance soundInstance = SimpleSoundInstance.forUI(soundEvent, 1.0f, 0.5f);
                minecraft.getSoundManager().play(soundInstance);
            }
        } catch (Exception e) {
            // Sound system not ready - log once and disable
            LOGGER.debug("Failed to play typewriter sound '{}': {}", sound, e.getMessage());
            this.sound = null; // Disable to prevent repeated errors
        }
    }

    /**
     * Get elapsed time since this track started.
     *
     * @return milliseconds since track creation/reset
     */
    public long getElapsedMs() {
        return System.currentTimeMillis() - startedAt;
    }

    @Override
    public String toString() {
        return "TypewriterTrack{index=" + index + ", interval=" + interval + "ms, sound=" + sound + "}";
    }
}
