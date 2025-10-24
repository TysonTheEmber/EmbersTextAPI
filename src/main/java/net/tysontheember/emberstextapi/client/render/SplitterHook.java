package net.tysontheember.emberstextapi.client.render;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.tysontheember.emberstextapi.client.cache.TextLayoutCache;
import net.tysontheember.emberstextapi.client.hook.SpanAwareFontHook;
import net.tysontheember.emberstextapi.client.markup.MarkupService;
import net.tysontheember.emberstextapi.client.spans.SpanBundle;
import net.tysontheember.emberstextapi.client.spans.SpanifiedSequence;
import net.tysontheember.emberstextapi.config.ClientSettings;

import java.util.Locale;
import java.util.Optional;

/**
 * Helper invoked from StringSplitter mixins to normalise markup-aware text before width
 * measurements and wrapping calculations.
 */
public final class SplitterHook {
    private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

    private SplitterHook() {
    }

    public static void captureContext(int width, Style baseStyle) {
        CONTEXT.set(new Context(width, baseStyle));
    }

    public static FormattedText preprocess(FormattedText text, Style baseStyle, int width) {
        captureContext(width, baseStyle);
        return preprocess(text);
    }

    public static FormattedText preprocess(FormattedText text) {
        if (text == null) {
            return null;
        }

        if (ClientSettings.shouldBypassCurrentScreen()) {
            return text;
        }

        Context context = CONTEXT.get();
        String raw = text.getString();
        if (!SpanAwareFontHook.looksLikeMarkup(raw)) {
            if (context != null) {
                CONTEXT.remove();
            }
            return text;
        }

        if (context == null) {
            context = new Context(Integer.MIN_VALUE, Style.EMPTY);
        }
        CONTEXT.remove();

        Locale locale = resolveLocale();
        Optional<SpanBundle> parsed = MarkupService.getInstance().parse(raw, locale, true);
        if (parsed.isEmpty()) {
            return text;
        }

        SpanBundle bundle = parsed.get();
        if (bundle.hasErrors()) {
            return text;
        }

        if (bundle.maxSpanDepth() > ClientSettings.maxSpanDepth()) {
            return text;
        }

        if (bundle.maxEffectLayers() > ClientSettings.maxEffectsPerGlyph()) {
            return text;
        }

        Style effectiveStyle = context.baseStyle != null ? context.baseStyle : Style.EMPTY;
        long seed = SpanAwareFontHook.computeSeed(raw, bundle.plainText());
        TextLayoutCache.Key key = new TextLayoutCache.Key(
            raw,
            bundle.plainText(),
            context.width,
            1.0f,
            locale,
            seed,
            ClientSettings.effectsVersion()
        );

        TextLayoutCache cache = TextLayoutCache.getInstance();
        if (cache.get(key).isEmpty()) {
            SpanifiedSequence.EvalContext evalContext = new SpanifiedSequence.EvalContext(Util::getMillis, seed, locale);
            cache.put(
                key,
                TextLayoutCache.CachedLayout.deferred(
                    bundle,
                    () -> SpanifiedSequence.of(
                        FormattedCharSequence.forward(bundle.plainText(), effectiveStyle),
                        bundle,
                        evalContext
                    )
                )
            );
        }

        return FormattedText.of(bundle.plainText());
    }

    private record Context(int width, Style baseStyle) {
    }

    private static Locale resolveLocale() {
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null && minecraft.getLanguageManager() != null) {
                String selected = minecraft.getLanguageManager().getSelected();
                if (selected != null && !selected.isEmpty()) {
                    String tag = selected.replace('_', '-');
                    Locale locale = Locale.forLanguageTag(tag);
                    if (locale != null) {
                        return locale;
                    }
                }
            }
        } catch (Throwable ignored) {
            // Tests or headless contexts may not have Minecraft available.
        }
        return Locale.ROOT;
    }
}
