package net.tysontheember.emberstextapi.sdf;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PreBakedMSDFCache {

    private static final ConcurrentHashMap<CacheKey, Map<Integer, PreBakedMSDF>> CACHE = new ConcurrentHashMap<>();

    private PreBakedMSDFCache() {}

    public static Map<Integer, PreBakedMSDF> getOrCreate(CacheKey key) {
        return CACHE.computeIfAbsent(key, k -> new ConcurrentHashMap<>());
    }

    public record CacheKey(
            long fontHash,
            int sdfResolution,
            int padding,
            float spread,
            float fontSize,
            float oversample,
            float pxRange,
            float angleThreshold,
            float shiftX,
            float shiftY,
            String skip) {}
}
