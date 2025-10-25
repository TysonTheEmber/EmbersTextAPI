package net.tysontheember.emberstextapi.client;

import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class TextLayoutCache {
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

    public record Key(Component component, String literal, String spanSignature, int colour, float scale, int wrapWidth, String fontKey) {
    }

    public record Layout(List<FormattedCharSequence> lines, FormattedCharSequence visualOrder, int width, int height) {
        public Layout {
            lines = lines == null ? null : List.copyOf(lines);
        }
    }
}
