package net.tysontheember.emberstextapi.client.hook;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.tysontheember.emberstextapi.client.cache.TextLayoutCache;
import net.tysontheember.emberstextapi.config.ClientSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class SpanAwareFontHookTest {
    @BeforeEach
    void setUp() {
        TextLayoutCache.getInstance().clear();
        ClientSettings.resetToDefaults();
    }

    @AfterEach
    void tearDown() {
        ClientSettings.resetToDefaults();
    }

    @Test
    void spanifiesMarkupSequenceAndCachesResult() {
        FormattedCharSequence base = FormattedCharSequence.forward("<bold>Hi</bold>", Style.EMPTY);

        FormattedCharSequence spanified = SpanAwareFontHook.maybeSpanify(base);
        String first = toString(spanified);

        assertNotSame(base, spanified, "Spanified sequence should differ from base when markup present");
        assertEquals("Hi", first, "Markup characters should be stripped in output");

        FormattedCharSequence cached = SpanAwareFontHook.maybeSpanify(base);
        assertSame(spanified, cached, "Repeated lookups should reuse cached spanified sequence");
    }

    @Test
    void returnsOriginalWhenDisabled() {
        ClientSettings.setStyledRenderingEnabled(false);

        FormattedCharSequence base = FormattedCharSequence.forward("<bold>Hi</bold>", Style.EMPTY);

        FormattedCharSequence result = SpanAwareFontHook.maybeSpanify(base);

        assertSame(base, result, "Disabled toggle should bypass spanification");
    }

    @Test
    void honoursSpanDepthGuard() {
        ClientSettings.setMaxSpanDepth(0);

        FormattedCharSequence base = FormattedCharSequence.forward("<bold>Depth</bold>", Style.EMPTY);

        FormattedCharSequence result = SpanAwareFontHook.maybeSpanify(base);

        assertSame(base, result, "Span depth guard should prevent spanification when exceeded");
    }

    @Test
    void honoursEffectLayerGuard() {
        ClientSettings.setMaxEffectsPerGlyph(0);

        FormattedCharSequence base = FormattedCharSequence.forward("<shake amplitude=\"1.0\">Shake</shake>", Style.EMPTY);

        FormattedCharSequence result = SpanAwareFontHook.maybeSpanify(base);

        assertSame(base, result, "Effect guard should prevent spanification when exceeded");
    }

    private static String toString(FormattedCharSequence sequence) {
        StringBuilder builder = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            return true;
        });
        return builder.toString();
    }
}
