package net.tysontheember.emberstextapi.client;

import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * LRU cache for text layout computations.
 * <p>
 * Caches the results of {@link net.minecraft.client.font.Font#split} and related
 * layout calculations so that repeated rendering of the same text (e.g., per-frame
 * tooltip or message re-renders) does not re-compute character widths and line breaks.
 * </p>
 * <p>
 * The cache is invalidated automatically on GUI scale changes (handled by
 * {@link ClientMessageManager}) and is safe for concurrent access.
 * </p>
 */
public final class TextLayoutCache {
    /** Maximum number of cached layout entries before LRU eviction begins. */
    private static final int MAX_ENTRIES = 256;
    private static final Map<Key, Layout> CACHE = Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Key, Layout> eldest) {
            return size() > MAX_ENTRIES;
        }
    });

    private TextLayoutCache() {
    }

    public static Layout getOrCompute(Key key, Supplier<Layout> supplier) {
        Layout existing = CACHE.get(key);
        if (existing != null) {
            return existing;
        }
        Layout computed = supplier.get();
        CACHE.put(key, computed);
        return computed;
    }

    public static void clear() {
        CACHE.clear();
    }

    public record Key(Component component, int colour, float scale, int wrapWidth, String fontKey) {
    }

    public record Layout(List<FormattedCharSequence> lines, FormattedCharSequence visualOrder, int width, int height) {
        public Layout {
            lines = lines == null ? null : List.copyOf(lines);
        }
    }
}
