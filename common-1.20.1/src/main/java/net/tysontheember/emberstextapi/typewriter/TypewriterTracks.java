package net.tysontheember.emberstextapi.typewriter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Singleton manager for typewriter animation tracks.
 * <p>
 * Uses a Guava {@link Cache} with automatic expiration to manage track instances.
 * Tracks expire 1 second after last access, automatically cleaning up when
 * tooltips close, screens change, etc.
 * </p>
 * <p>
 * This follows the TextAnimator pattern where tracks are keyed by content identity
 * and context, allowing independent animations for the same text in different locations.
 * </p>
 *
 * @see TypewriterTrack
 * @deprecated This class is part of the legacy typewriter API.
 *             Use {@link net.tysontheember.emberstextapi.immersivemessages.effects.animation.TypewriterEffect} instead.
 *             This class will be removed in version 3.0.0.
 */
@Deprecated(forRemoval = true, since = "2.0.0")
public class TypewriterTracks {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypewriterTracks.class);

    /** Singleton instance. */
    private static final TypewriterTracks INSTANCE = new TypewriterTracks();

    /** Maximum number of tracks to cache to prevent unbounded memory growth. */
    private static final int MAX_CACHE_SIZE = 1000;

    /** Cache of track keys to track instances. Expires 1 second after last access. */
    private final Cache<Object, TypewriterTrack> cache;

    /**
     * Set of keys for tracks that have completed their max plays.
     * This persists independently of the cache so that completed state survives cache expiration.
     */
    private final Set<Object> completedKeys = ConcurrentHashMap.newKeySet();

    /**
     * Private constructor - use {@link #getInstance()}.
     */
    private TypewriterTracks() {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfterAccess(1, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Get the singleton instance.
     *
     * @return the TypewriterTracks manager
     */
    public static TypewriterTracks getInstance() {
        return INSTANCE;
    }

    /**
     * Get or create a track for the given key.
     * <p>
     * If no track exists for the key, a new one is created with default settings.
     * Each access refreshes the expiration timer.
     * </p>
     * <p>
     * If this key was previously marked as completed (max plays reached), the returned
     * track will already be in a completed state, even if the original track was evicted
     * from the cache.
     * </p>
     *
     * @param key the track key (typically context + effect identity hash)
     * @return the track for this key (never null)
     */
    public TypewriterTrack get(Object key) {
        try {
            TypewriterTrack track = cache.get(key, () -> {
                // Check if this key was previously completed
                if (completedKeys.contains(key)) {
                    LOGGER.debug("Creating completed track for key (was in completedKeys): {}", key);
                    TypewriterTrack completedTrack = TypewriterTrack.createCompleted();
                    completedTrack.setCacheKey(key);
                    return completedTrack;
                }
                LOGGER.debug("Creating fresh track for key: {}", key);
                TypewriterTrack newTrack = new TypewriterTrack();
                newTrack.setCacheKey(key);
                return newTrack;
            });
            return track;
        } catch (ExecutionException e) {
            // Should never happen since TypewriterTrack() doesn't throw
            LOGGER.error("Failed to create TypewriterTrack for key: {}", key, e);
            return new TypewriterTrack();
        }
    }

    /**
     * Mark a key as having completed its max plays.
     * <p>
     * This ensures that even if the track is evicted from the cache, the completed
     * state persists and the animation won't replay.
     * </p>
     *
     * @param key the track key to mark as completed
     */
    public void markCompleted(Object key) {
        LOGGER.debug("Marking key as completed: {}", key);
        completedKeys.add(key);
    }

    /**
     * Check if a key has been marked as completed.
     *
     * @param key the track key
     * @return true if this key's animation has completed
     */
    public boolean isCompleted(Object key) {
        return completedKeys.contains(key);
    }

    /**
     * Check if a track exists for the given key without creating one.
     *
     * @param key the track key
     * @return true if a track exists, false otherwise
     */
    public boolean has(Object key) {
        return cache.getIfPresent(key) != null;
    }

    /**
     * Remove a specific track.
     *
     * @param key the track key to remove
     */
    public void remove(Object key) {
        cache.invalidate(key);
    }

    /**
     * Clear all cached tracks and completed keys.
     * <p>
     * Useful for complete reset (e.g., world change, debug commands).
     * </p>
     */
    public void clear() {
        cache.invalidateAll();
        completedKeys.clear();
        LOGGER.debug("Cleared all typewriter tracks and completed keys");
    }

    /**
     * Get the number of currently cached tracks.
     * <p>
     * Useful for debugging and monitoring.
     * </p>
     *
     * @return number of active tracks
     */
    public long size() {
        return cache.size();
    }

    /**
     * Force cleanup of expired entries.
     * <p>
     * Normally handled automatically, but can be called explicitly if needed.
     * </p>
     */
    public void cleanup() {
        cache.cleanUp();
    }
}
