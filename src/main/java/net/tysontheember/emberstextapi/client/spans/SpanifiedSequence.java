package net.tysontheember.emberstextapi.client.spans;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * FormattedCharSequence wrapper that emits per-character styles derived from markup spans.
 */
public final class SpanifiedSequence implements FormattedCharSequence {
    private final FormattedCharSequence base;
    private final SpanEntry[] entries;
    private final EvalContext context;

    private SpanifiedSequence(FormattedCharSequence base, SpanEntry[] entries, EvalContext context) {
        this.base = base;
        this.entries = entries;
        this.context = context;
    }

    public static FormattedCharSequence of(FormattedCharSequence base, SpanBundle bundle, EvalContext context) {
        Objects.requireNonNull(base, "base");
        Objects.requireNonNull(bundle, "bundle");
        if (bundle.spans().isEmpty()) {
            return base;
        }
        EvalContext ctx = context != null ? context : EvalContext.EMPTY;
        int length = Math.max(0, bundle.plainText().length());
        SpanEntry[] entries = buildEntries(bundle.spans(), length);
        return new SpanifiedSequence(base, entries, ctx);
    }

    @Override
    public boolean accept(FormattedCharSink sink) {
        return base.accept((index, style, codePoint) -> {
            SpanEntry entry = index >= 0 && index < entries.length ? entries[index] : null;
            Style applied = apply(entry, style);
            return sink.accept(index, applied, codePoint);
        });
    }

    private Style apply(SpanEntry entry, Style baseStyle) {
        if (entry == null || entry.attr == null) {
            return baseStyle;
        }

        SpanAttr attr = entry.attr;
        Style result = baseStyle;
        SpanAttr.StyleFlags flags = attr.style();
        if (flags != null) {
            result = result.withBold(flags.bold());
            result = result.withItalic(flags.italic());
            result = result.withUnderlined(flags.underline());
            result = result.withStrikethrough(flags.strikethrough());
            result = result.withObfuscated(flags.obfuscated());
            if (flags.font() != null) {
                result = result.withFont(flags.font());
            }
        }

        TextColor colour = null;
        SpanAttr.Gradient gradient = attr.gradient();
        if (gradient != null) {
            colour = sampleGradient(gradient, entry.offset, Math.max(1, entry.length));
        }
        if (colour == null && attr.color() != null) {
            colour = attr.color().value();
        }
        if (colour != null) {
            result = result.withColor(colour);
        }

        // Effects are evaluated but currently unused until render hooks consume deltas.
        EffectAdapter.StyleDelta delta = EffectAdapter.apply(entry.globalIndex, entry.lineIndex, context.timeMs(), context.seed(), attr);
        if (delta.styleOverride() != null) {
            SpanAttr.StyleFlags override = delta.styleOverride();
            result = result
                .withBold(override.bold())
                .withItalic(override.italic())
                .withUnderlined(override.underline())
                .withStrikethrough(override.strikethrough())
                .withObfuscated(override.obfuscated());
            if (override.font() != null) {
                result = result.withFont(override.font());
            }
        }
        if (delta.colorOverride() != null) {
            result = result.withColor(delta.colorOverride());
        }

        return result;
    }

    private static SpanEntry[] buildEntries(List<TextSpanView> spans, int length) {
        SpanEntry[] entries = new SpanEntry[length];
        for (TextSpanView view : spans) {
            int start = Math.max(0, view.start());
            int end = Math.min(length, view.end());
            for (int i = start; i < end; i++) {
                int offset = i - start;
                entries[i] = new SpanEntry(view.attr(), offset, Math.max(1, end - start), i, 0);
            }
        }
        return entries;
    }

    private static TextColor sampleGradient(SpanAttr.Gradient gradient, int offset, int spanLength) {
        TextColor[] colors = gradient.colors();
        if (colors == null || colors.length == 0) {
            return null;
        }
        if (colors.length == 1 || spanLength <= 1) {
            return colors[0];
        }

        float t = spanLength <= 1 ? 0f : offset / (float) (spanLength - 1);
        int segments = colors.length - 1;
        float scaled = t * segments;
        int segIndex = Mth.clamp((int) Math.floor(scaled), 0, segments - 1);
        float local = scaled - segIndex;

        int start = colors[segIndex].getValue();
        int end = colors[segIndex + 1].getValue();
        int rgb = lerpColor(start, end, local);
        return TextColor.fromRgb(rgb);
    }

    private static int lerpColor(int start, int end, float t) {
        int sr = (start >> 16) & 0xFF;
        int sg = (start >> 8) & 0xFF;
        int sb = start & 0xFF;

        int er = (end >> 16) & 0xFF;
        int eg = (end >> 8) & 0xFF;
        int eb = end & 0xFF;

        int r = sr + Math.round((er - sr) * t);
        int g = sg + Math.round((eg - sg) * t);
        int b = sb + Math.round((eb - sb) * t);
        return (r << 16) | (g << 8) | b;
    }

    public record EvalContext(long timeMs, long seed, Locale locale) {
        public static final EvalContext EMPTY = new EvalContext(0L, 0L, Locale.ROOT);

        public EvalContext {
            if (locale == null) {
                locale = Locale.ROOT;
            }
        }
    }

    private static final class SpanEntry {
        private final SpanAttr attr;
        private final int offset;
        private final int length;
        private final int globalIndex;
        private final int lineIndex;

        private SpanEntry(SpanAttr attr, int offset, int length, int globalIndex, int lineIndex) {
            this.attr = attr;
            this.offset = offset;
            this.length = length;
            this.globalIndex = globalIndex;
            this.lineIndex = lineIndex;
        }
    }
}
