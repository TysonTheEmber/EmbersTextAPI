package net.tysontheember.emberstextapi.client.cache;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.tysontheember.emberstextapi.client.spans.SpanBundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TextLayoutCacheTest {
    @AfterEach
    void resetCache() {
        TextLayoutCache.getInstance().clear();
    }

    @Test
    void returnsSameLayoutOnRepeatedLookups() {
        TextLayoutCache cache = TextLayoutCache.getInstance();
        SpanBundle bundle = emptyBundle("Hello");
        TextLayoutCache.Key key = new TextLayoutCache.Key(bundle.plainText(), 120, 1.0f, Locale.ROOT, 0L, 1);
        FormattedCharSequence sequence = Component.literal(bundle.plainText()).getVisualOrderText();

        TextLayoutCache.CachedLayout layout = TextLayoutCache.CachedLayout.eager(bundle, sequence);
        cache.put(key, layout);

        Optional<TextLayoutCache.CachedLayout> first = cache.get(key);
        Optional<TextLayoutCache.CachedLayout> second = cache.get(key);

        assertTrue(first.isPresent(), "Expected cache hit on first lookup");
        assertTrue(second.isPresent(), "Expected cache hit on second lookup");
        assertSame(layout, first.get());
        assertSame(first.get(), second.get());
    }

    @Test
    void deferredSequenceFactoryInvokedOnce() {
        TextLayoutCache cache = TextLayoutCache.getInstance();
        SpanBundle bundle = emptyBundle("Spans");
        TextLayoutCache.Key key = new TextLayoutCache.Key(bundle.plainText(), 200, 0.75f, Locale.ROOT, 42L, 2);

        AtomicInteger invocations = new AtomicInteger();
        TextLayoutCache.CachedLayout layout = TextLayoutCache.CachedLayout.deferred(bundle, () -> {
            invocations.incrementAndGet();
            return FormattedCharSequence.forward(bundle.plainText(), Style.EMPTY);
        });
        cache.put(key, layout);

        FormattedCharSequence first = cache.get(key).orElseThrow().sequence();
        FormattedCharSequence second = cache.get(key).orElseThrow().sequence();

        assertSame(first, second, "Expected cached sequence instance");
        assertEquals(1, invocations.get(), "Factory should only run once");
    }

    @Test
    void clearRemovesEntriesAndStats() {
        TextLayoutCache cache = TextLayoutCache.getInstance();
        SpanBundle bundle = emptyBundle("Reset");
        TextLayoutCache.Key key = new TextLayoutCache.Key(bundle.plainText(), 50, 1.0f, Locale.ROOT, 1L, 0);

        cache.put(key, TextLayoutCache.CachedLayout.eager(bundle,
            FormattedCharSequence.forward(bundle.plainText(), Style.EMPTY)));

        assertTrue(cache.get(key).isPresent());
        cache.clear();
        assertTrue(cache.get(key).isEmpty(), "Cache should miss after clear");
    }

    private static SpanBundle emptyBundle(String text) {
        return new SpanBundle(text, List.of(), List.of(), List.of(), List.of());
    }
}
