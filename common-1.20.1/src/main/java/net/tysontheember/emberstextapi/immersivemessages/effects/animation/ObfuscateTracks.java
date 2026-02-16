package net.tysontheember.emberstextapi.immersivemessages.effects.animation;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Lightweight cache for {@link ObfuscateTrack} instances keyed by context (style, message, etc.).
 * Mirrors the TypewriterTracks behavior with a short expiry to avoid leaking tooltip sessions.
 */
public class ObfuscateTracks {
    private static final ObfuscateTracks INSTANCE = new ObfuscateTracks();
    private static final int MAX_CACHE_SIZE = 512;

    private final Cache<Object, ObfuscateTrack> cache;

    private ObfuscateTracks() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfterAccess(1, TimeUnit.SECONDS)
                .build();
    }

    public static ObfuscateTracks getInstance() {
        return INSTANCE;
    }

    public ObfuscateTrack get(Object key) {
        if (key == null) {
            return new ObfuscateTrack();
        }
        try {
            return cache.get(key, () -> {
                ObfuscateTrack t = new ObfuscateTrack();
                t.cacheKey = key;
                return t;
            });
        } catch (ExecutionException e) {
            return new ObfuscateTrack();
        }
    }
}
