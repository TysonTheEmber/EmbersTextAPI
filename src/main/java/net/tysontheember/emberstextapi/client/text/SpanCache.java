package net.tysontheember.emberstextapi.client.text;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntFunction;

/**
 * Placeholder cache for storing span graphs and derived data.
 */
public final class SpanCache {
    private static final Map<SpanNode, Map<Integer, int[]>> GRADIENT_CACHE = new ConcurrentHashMap<>();

    private SpanCache() {
    }

    public static SpanCache create() {
        return new SpanCache();
    }

    public static int[] getGradientColors(SpanNode node, int length, IntFunction<int[]> factory) {
        Map<Integer, int[]> lengths = GRADIENT_CACHE.computeIfAbsent(node, ignored -> new ConcurrentHashMap<>());
        return lengths.computeIfAbsent(length, key -> factory.apply(key));
    }
}
