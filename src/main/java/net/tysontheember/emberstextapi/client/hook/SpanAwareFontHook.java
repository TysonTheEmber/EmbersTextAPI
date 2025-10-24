package net.tysontheember.emberstextapi.client.hook;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.tysontheember.emberstextapi.client.cache.TextLayoutCache;
import net.tysontheember.emberstextapi.client.markup.MarkupService;
import net.tysontheember.emberstextapi.client.spans.SpanBundle;
import net.tysontheember.emberstextapi.client.spans.SpanifiedSequence;

import java.util.Locale;
import java.util.Optional;

/**
 * Entry point for swapping markup-aware sequences into the font pipeline.
 */
public final class SpanAwareFontHook {
    private SpanAwareFontHook() {
    }

    public static FormattedCharSequence maybeSpanify(FormattedCharSequence sequence) {
        if (sequence == null) {
            return null;
        }
        if (sequence instanceof SpanifiedSequence) {
            return sequence;
        }

        String raw = extractText(sequence);
        if (!looksLikeMarkup(raw)) {
            return sequence;
        }

        Locale locale = resolveLocale();
        Optional<SpanBundle> parsed = MarkupService.getInstance().parse(raw, locale, true);
        if (parsed.isEmpty()) {
            return sequence;
        }

        SpanBundle bundle = parsed.get();
        if (bundle.hasErrors()) {
            return sequence;
        }

        TextLayoutCache.Key key = new TextLayoutCache.Key(
            bundle.plainText(),
            -1,
            1.0f,
            locale,
            0L,
            0
        );

        TextLayoutCache cache = TextLayoutCache.getInstance();
        Optional<TextLayoutCache.CachedLayout> cached = cache.get(key);
        if (cached.isPresent()) {
            return cached.get().sequence();
        }

        FormattedCharSequence base = buildBaseSequence(bundle.plainText(), sequence);
        SpanifiedSequence.EvalContext context = new SpanifiedSequence.EvalContext(Util.getMillis(), 0L, locale);
        FormattedCharSequence spanified = SpanifiedSequence.of(base, bundle, context);
        cache.put(key, TextLayoutCache.CachedLayout.eager(bundle, spanified));
        return spanified;
    }

    private static String extractText(FormattedCharSequence sequence) {
        StringBuilder builder = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            builder.appendCodePoint(codePoint);
            return true;
        });
        return builder.toString();
    }

    private static boolean looksLikeMarkup(String raw) {
        if (raw == null || raw.isEmpty()) {
            return false;
        }
        int open = raw.indexOf('<');
        if (open < 0) {
            return false;
        }
        int close = raw.indexOf('>', open + 1);
        if (close < 0) {
            return false;
        }
        if (close - open < 2) {
            return false;
        }
        char marker = raw.charAt(open + 1);
        return marker == '/' || Character.isLetter(marker);
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

    private static FormattedCharSequence buildBaseSequence(String plainText, FormattedCharSequence original) {
        Style baseStyle = resolveBaseStyle(original);
        return FormattedCharSequence.forward(plainText, baseStyle);
    }

    private static Style resolveBaseStyle(FormattedCharSequence sequence) {
        final Style[] holder = new Style[1];
        sequence.accept((index, style, codePoint) -> {
            if (holder[0] == null) {
                holder[0] = style;
            }
            return holder[0] == null;
        });
        return holder[0] != null ? holder[0] : Style.EMPTY;
    }
}
