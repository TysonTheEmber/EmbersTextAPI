package net.tysontheember.emberstextapi.client.cache;

import net.minecraft.util.FormattedCharSequence;
import net.tysontheember.emberstextapi.client.spans.SpanBundle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * LRU cache for span-aware text layouts and formatted sequences.
 */
public final class TextLayoutCache {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int DEFAULT_MAX_ENTRIES = 256;
    private static final TextLayoutCache GLOBAL = new TextLayoutCache(DEFAULT_MAX_ENTRIES);

    private final Map<Key, CachedLayout> cache;
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();

    public TextLayoutCache() {
        this(DEFAULT_MAX_ENTRIES);
    }

    public TextLayoutCache(int maxEntries) {
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("maxEntries must be positive");
        }
        this.cache = Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Key, CachedLayout> eldest) {
                return size() > maxEntries;
            }
        });
    }

    public static TextLayoutCache getInstance() {
        return GLOBAL;
    }

    public Optional<CachedLayout> get(Key key) {
        Objects.requireNonNull(key, "key");
        CachedLayout layout = cache.get(key);
        if (layout != null) {
            long hit = hits.incrementAndGet();
            logStats(hit, misses.get(), true, key);
            return Optional.of(layout);
        }
        long miss = misses.incrementAndGet();
        logStats(hits.get(), miss, false, key);
        return Optional.empty();
    }

    public void put(Key key, CachedLayout layout) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(layout, "layout");
        cache.put(key, layout);
    }

    public void clear() {
        cache.clear();
        hits.set(0);
        misses.set(0);
    }

    private void logStats(long hitCount, long missCount, boolean hit, Key key) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Span TextLayoutCache {} for key {} (hits={}, misses={})",
                hit ? "hit" : "miss",
                key,
                hitCount,
                missCount);
        }
    }

    public record Key(String cleanText, int width, float scale, Locale lang, long seed, int effectsVersion) {
        public Key {
            cleanText = cleanText != null ? cleanText : "";
            lang = lang != null ? lang : Locale.ROOT;
        }
    }

    public static final class CachedLayout {
        private final SpanBundle spanBundle;
        private final Supplier<FormattedCharSequence> sequenceFactory;
        private volatile FormattedCharSequence sequence;

        private CachedLayout(SpanBundle spanBundle, Supplier<FormattedCharSequence> sequenceFactory, FormattedCharSequence eager) {
            this.spanBundle = Objects.requireNonNull(spanBundle, "spanBundle");
            this.sequenceFactory = Objects.requireNonNull(sequenceFactory, "sequenceFactory");
            this.sequence = eager;
        }

        public static CachedLayout eager(SpanBundle bundle, FormattedCharSequence sequence) {
            Objects.requireNonNull(sequence, "sequence");
            return new CachedLayout(bundle, () -> sequence, sequence);
        }

        public static CachedLayout deferred(SpanBundle bundle, Supplier<FormattedCharSequence> factory) {
            return new CachedLayout(bundle, factory, null);
        }

        public SpanBundle spanBundle() {
            return spanBundle;
        }

        public FormattedCharSequence sequence() {
            FormattedCharSequence result = sequence;
            if (result != null) {
                return result;
            }
            synchronized (this) {
                result = sequence;
                if (result == null) {
                    result = Objects.requireNonNull(sequenceFactory.get(), "sequenceFactory returned null sequence");
                    sequence = result;
                }
                return result;
            }
        }
    }
}
