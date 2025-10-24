package net.tysontheember.emberstextapi.client.cache;

import net.minecraft.util.FormattedCharSequence;
import net.tysontheember.emberstextapi.client.spans.SpanBundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SpanCacheInvalidationTest {

    @BeforeEach
    void setUp() {
        TextLayoutCache.getInstance().clear();
        net.tysontheember.emberstextapi.client.TextLayoutCache.clear();
        SpanCacheInvalidation.resetForTests();
    }

    @AfterEach
    void tearDown() {
        TextLayoutCache.getInstance().clear();
        net.tysontheember.emberstextapi.client.TextLayoutCache.clear();
        SpanCacheInvalidation.resetForTests();
    }

    @Test
    void clearsCacheWhenGuiScaleChanges() {
        TextLayoutCache.Key key = new TextLayoutCache.Key("<b>hi</b>", "hi", 100, 1.0f, Locale.ROOT, 0L, 0);
        TextLayoutCache.CachedLayout layout = TextLayoutCache.CachedLayout.eager(
            new SpanBundle("hi", List.of(), List.of(), List.of(), List.of(), 0, 0),
            FormattedCharSequence.forward("hi", net.minecraft.network.chat.Style.EMPTY)
        );
        TextLayoutCache.getInstance().put(key, layout);
        assertTrue(TextLayoutCache.getInstance().get(key).isPresent(), "Expected entry in cache before GUI scale change");

        SpanCacheInvalidation.handleGuiScaleCandidate(2);
        assertTrue(TextLayoutCache.getInstance().get(key).isEmpty(), "Cache should be cleared after GUI scale change");

        TextLayoutCache.getInstance().put(key, layout);
        SpanCacheInvalidation.handleGuiScaleCandidate(2);
        assertTrue(TextLayoutCache.getInstance().get(key).isPresent(), "Cache should remain when GUI scale unchanged");
    }

    @Test
    void clearsCacheWhenLanguageChanges() {
        TextLayoutCache.Key key = new TextLayoutCache.Key("<i>hola</i>", "hola", 100, 1.0f, Locale.ROOT, 0L, 0);
        TextLayoutCache.CachedLayout layout = TextLayoutCache.CachedLayout.eager(
            new SpanBundle("hola", List.of(), List.of(), List.of(), List.of(), 0, 0),
            FormattedCharSequence.forward("hola", net.minecraft.network.chat.Style.EMPTY)
        );
        TextLayoutCache.getInstance().put(key, layout);
        assertTrue(TextLayoutCache.getInstance().get(key).isPresent(), "Expected entry before language change");

        SpanCacheInvalidation.handleLanguageCandidate("en_us");
        assertTrue(TextLayoutCache.getInstance().get(key).isEmpty(), "Cache should clear after language change");
    }

    @Test
    void clearsCacheWhenConfigFingerprintChanges() {
        TextLayoutCache.Key key = new TextLayoutCache.Key("plain", "plain", 100, 1.0f, Locale.ROOT, 0L, 0);
        SpanBundle bundle = new SpanBundle("plain", List.of(), List.of(), List.of(), List.of(), 0, 0);
        TextLayoutCache.CachedLayout layout = TextLayoutCache.CachedLayout.eager(
            bundle,
            FormattedCharSequence.forward("plain", net.minecraft.network.chat.Style.EMPTY)
        );
        TextLayoutCache.getInstance().put(key, layout);
        assertTrue(TextLayoutCache.getInstance().get(key).isPresent(), "Expected cache entry before config change");

        SpanCacheInvalidation.handleConfigFingerprintCandidate(123);
        assertTrue(TextLayoutCache.getInstance().get(key).isEmpty(), "Cache should clear after config fingerprint change");

        TextLayoutCache.getInstance().put(key, layout);
        SpanCacheInvalidation.handleConfigFingerprintCandidate(123);
        assertTrue(TextLayoutCache.getInstance().get(key).isPresent(), "Cache should remain when fingerprint unchanged");
    }
}
