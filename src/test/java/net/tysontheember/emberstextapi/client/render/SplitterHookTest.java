package net.tysontheember.emberstextapi.client.render;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.client.cache.TextLayoutCache;
import net.tysontheember.emberstextapi.config.ClientSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SplitterHookTest {
    @BeforeEach
    void clearCache() {
        TextLayoutCache.getInstance().clear();
        ClientSettings.resetToDefaults();
    }

    @Test
    void preprocessStripsMarkupAndCachesLayout() {
        FormattedText result = SplitterHook.preprocess(Component.literal("<bold>Hello</bold> world"), Style.EMPTY, 80);

        assertNotNull(result);
        assertEquals("Hello world", result.getString());

        TextLayoutCache.Key key = new TextLayoutCache.Key(
            "<bold>Hello</bold> world",
            "Hello world",
            80,
            1.0f,
            Locale.ROOT,
            0L,
            ClientSettings.effectsVersion()
        );
        assertTrue(TextLayoutCache.getInstance().get(key).isPresent(), "Expected cached layout for parsed markup");
    }

    @Test
    void preprocessPassesThroughPlainText() {
        FormattedText text = Component.literal("No markup here");
        FormattedText processed = SplitterHook.preprocess(text, Style.EMPTY, 120);

        assertSame(text, processed);
        TextLayoutCache.Key key = new TextLayoutCache.Key(
            "No markup here",
            "No markup here",
            120,
            1.0f,
            Locale.ROOT,
            0L,
            ClientSettings.effectsVersion()
        );
        assertTrue(TextLayoutCache.getInstance().get(key).isEmpty(), "Plain text should not populate the cache");
    }

    @Test
    void respectsDisableToggle() {
        ClientSettings.setStyledRenderingEnabled(false);

        FormattedText text = Component.literal("<bold>Disabled</bold>");
        FormattedText processed = SplitterHook.preprocess(text, Style.EMPTY, 42);

        assertSame(text, processed, "Disabled toggle should bypass preprocessing");
    }
}
