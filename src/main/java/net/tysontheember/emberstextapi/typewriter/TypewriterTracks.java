package net.tysontheember.emberstextapi.typewriter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 */
public class TypewriterTracks {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypewriterTracks.class);

    /** Singleton instance. */
    private static final TypewriterTracks INSTANCE = new TypewriterTracks();

    /** Cache of track keys to track instances. Expires 1 second after last access. */
    private final Cache<Object, TypewriterTrack> cache;

    /**
     * Private constructor - use {@link #getInstance()}.
     */
    private TypewriterTracks() {
        this.cache = CacheBuilder.newBuilder()
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
     *
     * @param key the track key (typically context + effect identity hash)
     * @return the track for this key (never null)
     */
    public TypewriterTrack get(Object key) {
        try {
            return cache.get(key, TypewriterTrack::new);
        } catch (ExecutionException e) {
            // Should never happen since TypewriterTrack() doesn't throw
            LOGGER.error("Failed to create TypewriterTrack for key: {}", key, e);
            return new TypewriterTrack();
        }
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
     * Clear all cached tracks.
     * <p>
     * Useful for complete reset (e.g., world change, debug commands).
     * </p>
     */
    public void clear() {
        cache.invalidateAll();
        LOGGER.debug("Cleared all typewriter tracks");
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
