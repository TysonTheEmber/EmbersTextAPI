package net.tysontheember.emberstextapi.client.hook;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.tysontheember.emberstextapi.client.cache.TextLayoutCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class SpanAwareFontHookTest {
    @BeforeEach
    void setUp() {
        TextLayoutCache.getInstance().clear();
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

    private static String toString(FormattedCharSequence sequence) {
        StringBuilder builder = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            return true;
        });
        return builder.toString();
    }
}
