package net.tysontheember.emberstextapi.immersivemessages.effects.animation;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class TypewriterTracks {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypewriterTracks.class);

    private static final TypewriterTracks INSTANCE = new TypewriterTracks();

    private static final int MAX_CACHE_SIZE = 1000;

    private final Cache<Object, TypewriterTrack> cache;

    private final Cache<Object, Boolean> completedKeys;

    private TypewriterTracks() {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfterAccess(1, TimeUnit.SECONDS)
                .build();
        this.completedKeys = CacheBuilder.newBuilder()
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();
    }

    public static TypewriterTracks getInstance() {
        return INSTANCE;
    }

    public TypewriterTrack get(Object key) {
        try {
            TypewriterTrack track = cache.get(key, () -> {

                if (completedKeys.getIfPresent(key) != null) {
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

            LOGGER.error("Failed to create TypewriterTrack for key: {}", key, e);
            return new TypewriterTrack();
        }
    }

    public void markCompleted(Object key) {
        LOGGER.debug("Marking key as completed: {}", key);
        completedKeys.put(key, Boolean.TRUE);
    }

    public boolean isCompleted(Object key) {
        return completedKeys.getIfPresent(key) != null;
    }

    public boolean has(Object key) {
        return cache.getIfPresent(key) != null;
    }

    public void remove(Object key) {
        cache.invalidate(key);
    }

    public void clear() {
        cache.invalidateAll();
        completedKeys.invalidateAll();
        LOGGER.debug("Cleared all typewriter tracks and completed keys");
    }

    public long size() {
        return cache.size();
    }

    public void cleanup() {
        cache.cleanUp();
    }
}
