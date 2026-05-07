package net.tysontheember.emberstextapi.immersivemessages.effects.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeSet;

public class TypewriterTrack {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypewriterTrack.class);

    private static final long MIN_SOUND_INTERVAL_MS = 30;
    private static final long FRAME_THRESHOLD_NS = 1_000_000;
    private static final long DEFAULT_RESET_DELAY_MS = 1000;
    private static final int DEFAULT_SPEED_MS = 20;
    private static final int DEFAULT_MAX_PLAYS = -1;

    public long startedAt;
    public long changedSince;
    public int index;

    private int interval;
    private String sound;
    private long lastSoundMs;
    private int shadowRenderCounter;
    private int mainRenderCounter;
    private long lastShadowRenderFrame;
    private long lastMainRenderFrame;
    private long lastAccessTime;
    private long resetDelayMs;
    private TreeSet<Integer> currentFramePositions;
    private TreeSet<Integer> previousFramePositions;
    private long lastPositionFrame;
    private int[] sortedPositionsCache;
    private int maxPlays;
    private int playCount;
    private int totalChars;
    private boolean currentPlayCounted;
    private Object cacheKey;

    public TypewriterTrack() {
        long now = System.currentTimeMillis();
        this.startedAt = now;
        this.changedSince = now;
        this.index = 0;
        this.interval = DEFAULT_SPEED_MS;
        this.sound = null;
        this.lastSoundMs = 0;
        this.shadowRenderCounter = 0;
        this.mainRenderCounter = 0;
        this.lastShadowRenderFrame = 0;
        this.lastMainRenderFrame = 0;
        this.lastAccessTime = now;
        this.resetDelayMs = DEFAULT_RESET_DELAY_MS;
        this.currentFramePositions = new TreeSet<>();
        this.previousFramePositions = new TreeSet<>();
        this.lastPositionFrame = 0;
        this.sortedPositionsCache = new int[0];
        this.maxPlays = DEFAULT_MAX_PLAYS;
        this.playCount = 0;
        this.totalChars = -1;
        this.currentPlayCounted = false;
    }

    public static TypewriterTrack createCompleted() {
        TypewriterTrack track = new TypewriterTrack();
        track.maxPlays = 1;
        track.playCount = 1;
        track.currentPlayCounted = true;
        track.index = Integer.MAX_VALUE;
        return track;
    }

    public synchronized void update() {
        long now = System.currentTimeMillis();
        int previousIndex = index;

        while (now - changedSince >= interval) {
            changedSince += interval;
            index++;
        }

        if (index > previousIndex && sound != null) {
            if (now - lastSoundMs >= MIN_SOUND_INTERVAL_MS) {
                playSound();
                lastSoundMs = now;
            }
        }

        if (totalChars > 0 && index >= totalChars && !currentPlayCounted) {
            playCount++;
            currentPlayCounted = true;
            LOGGER.debug("Play completed: playCount={}, maxPlays={}, totalChars={}, index={}",
                    playCount, maxPlays, totalChars, index);

            if (maxPlays != -1 && playCount >= maxPlays && cacheKey != null) {
                TypewriterTracks.getInstance().markCompleted(cacheKey);
            }
        }
    }

    public void setInterval(int ms) {
        this.interval = Math.max(1, ms);
    }

    public int getInterval() {
        return interval;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        if (sound == null || "off".equalsIgnoreCase(sound) || sound.isEmpty()) {
            this.sound = null;
        } else {
            this.sound = sound;
        }
    }

    public void setResetDelayMs(long delayMs) {
        this.resetDelayMs = Math.max(0, delayMs);
    }

    public long getResetDelayMs() {
        return resetDelayMs;
    }

    public boolean checkAndResetIfNeeded() {
        long now = System.currentTimeMillis();
        long timeSinceAccess = now - lastAccessTime;

        if (timeSinceAccess > resetDelayMs) {
            if (maxPlays != -1 && playCount >= maxPlays) {
                lastAccessTime = now;
                return false;
            }
            reset();
            return true;
        }

        lastAccessTime = now;
        return false;
    }

    public boolean isCompleted() {
        return maxPlays != -1 && playCount >= maxPlays && currentPlayCounted;
    }

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
        this.currentPlayCounted = false;

    }

    public int getMaxPlays() {
        return maxPlays;
    }

    public void setMaxPlays(int maxPlays) {
        this.maxPlays = maxPlays < 0 ? -1 : Math.max(1, maxPlays);
    }

    public int getTotalChars() {
        return totalChars;
    }

    public void setTotalChars(int totalChars) {
        this.totalChars = Math.max(0, totalChars);
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setCacheKey(Object key) {
        this.cacheKey = key;
    }

    public synchronized int nextRenderIndex(long frameTime, boolean isShadow) {
        if (isShadow) {
            if (Math.abs(frameTime - lastShadowRenderFrame) > FRAME_THRESHOLD_NS) {
                shadowRenderCounter = 0;
                lastShadowRenderFrame = frameTime;
            }
            return shadowRenderCounter++;
        } else {
            if (Math.abs(frameTime - lastMainRenderFrame) > FRAME_THRESHOLD_NS) {
                mainRenderCounter = 0;
                lastMainRenderFrame = frameTime;
            }
            return mainRenderCounter++;
        }
    }

    public synchronized int getSequentialOrdinal(int positionOrdinal, long frameTime) {
        if (Math.abs(frameTime - lastPositionFrame) > FRAME_THRESHOLD_NS) {
            previousFramePositions.clear();
            previousFramePositions.addAll(currentFramePositions);
            currentFramePositions.clear();
            lastPositionFrame = frameTime;

            sortedPositionsCache = previousFramePositions.stream().mapToInt(Integer::intValue).toArray();
        }

        currentFramePositions.add(positionOrdinal);

        if (sortedPositionsCache.length > 0) {
            int idx = java.util.Arrays.binarySearch(sortedPositionsCache, positionOrdinal);
            if (idx >= 0) {
                return idx;
            }
            return Math.min(-idx - 1, sortedPositionsCache.length - 1);
        }

        return currentFramePositions.headSet(positionOrdinal, true).size() - 1;
    }

    private void playSound() {
        if (sound == null) {
            return;
        }

        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.getSoundManager() != null) {
                Identifier soundId = Identifier.tryParse(sound);
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
            LOGGER.debug("Failed to play typewriter sound '{}': {}", sound, e.getMessage());
            this.sound = null;
        }
    }

    public long getElapsedMs() {
        return System.currentTimeMillis() - startedAt;
    }

    @Override
    public String toString() {
        return "TypewriterTrack{index=" + index + ", interval=" + interval + "ms, sound=" + sound + "}";
    }
}
