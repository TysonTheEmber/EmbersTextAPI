package net.tysontheember.emberstextapi.client.spans;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.client.spans.effects.ObfuscateEffect;
import net.tysontheember.emberstextapi.client.spans.effects.TypewriterEffect;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

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
        SpanEntry[] entries = buildEntries(bundle.spans(), length, ctx);
        return new SpanifiedSequence(base, entries, ctx);
    }

    @Override
    public boolean accept(FormattedCharSink sink) {
        return base.accept((index, style, codePoint) -> {
            SpanEntry entry = index >= 0 && index < entries.length ? entries[index] : null;
            EffectAdapter.StyleDelta delta = EffectAdapter.apply(entry, context);
            if (delta != null && !delta.visible()) {
                return true;
            }
            Style applied = apply(entry, style, delta);
            return sink.accept(index, applied, codePoint);
        });
    }

    private Style apply(SpanEntry entry, Style baseStyle, EffectAdapter.StyleDelta delta) {
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

        if (delta != null) {
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
            if (delta.obfuscatedOverride() != null) {
                result = result.withObfuscated(delta.obfuscatedOverride());
            }
            if (delta.colorOverride() != null) {
                result = result.withColor(delta.colorOverride());
            }
        }

        return result;
    }

    private static SpanEntry[] buildEntries(List<TextSpanView> spans, int length, EvalContext context) {
        SpanEntry[] entries = new SpanEntry[length];
        AtomicInteger spanIndex = new AtomicInteger();
        for (TextSpanView view : spans) {
            int start = Math.max(0, view.start());
            int end = Math.min(length, view.end());
            SpanState state = SpanState.create(view.attr(), Math.max(1, end - start), context.seed(), spanIndex.getAndIncrement());
            for (int i = start; i < end; i++) {
                int offset = i - start;
                entries[i] = new SpanEntry(view.attr(), offset, Math.max(1, end - start), i, 0, state);
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

    public static final class EvalContext {
        public static final EvalContext EMPTY = new EvalContext(() -> 0L, 0L, Locale.ROOT);

        private final LongSupplier timeSource;
        private final long startTimeMs;
        private final long seed;
        private final Locale locale;

        public EvalContext(LongSupplier timeSource, long seed, Locale locale) {
            this.timeSource = timeSource != null ? timeSource : () -> 0L;
            this.seed = seed;
            this.locale = locale != null ? locale : Locale.ROOT;
            this.startTimeMs = this.timeSource.getAsLong();
        }

        public long seed() {
            return seed;
        }

        public Locale locale() {
            return locale;
        }

        public long elapsedMs() {
            long now = timeSource.getAsLong();
            return Math.max(0L, now - startTimeMs);
        }

        public float elapsedTicks() {
            return elapsedMs() / 50f;
        }
    }

    static final class SpanEntry {
        final SpanAttr attr;
        final int offset;
        final int length;
        final int globalIndex;
        final int lineIndex;
        final SpanState state;

        SpanEntry(SpanAttr attr, int offset, int length, int globalIndex, int lineIndex, SpanState state) {
            this.attr = attr;
            this.offset = offset;
            this.length = length;
            this.globalIndex = globalIndex;
            this.lineIndex = lineIndex;
            this.state = state;
        }
    }

    static final class SpanState {
        final TypewriterEffect.State typewriter;
        final ObfuscateEffect.State obfuscate;

        private SpanState(TypewriterEffect.State typewriter, ObfuscateEffect.State obfuscate) {
            this.typewriter = typewriter;
            this.obfuscate = obfuscate;
        }

        static SpanState create(SpanAttr attr, int length, long seed, int spanIndex) {
            if (attr == null || attr.effect() == null) {
                return new SpanState(null, null);
            }
            SpanAttr.EffectSpec spec = attr.effect();
            long mixedSeed = mixSeed(seed, spanIndex);
            TypewriterEffect.State typewriter = spec.typewriter() != null
                ? TypewriterEffect.create(spec.typewriter(), length)
                : null;
            if (typewriter == null && spec.global() != null && spec.global().typewriterSpeed() != null) {
                SpanAttr.EffectSpec.Typewriter globalTypewriter = new SpanAttr.EffectSpec.Typewriter(
                    spec.global().typewriterSpeed(),
                    Boolean.TRUE.equals(spec.global().typewriterCenter())
                );
                typewriter = TypewriterEffect.create(globalTypewriter, length);
            }
            ObfuscateEffect.State obfuscate = spec.obfuscate() != null
                ? ObfuscateEffect.create(spec.obfuscate(), length, mixedSeed)
                : null;
            return new SpanState(typewriter, obfuscate);
        }

        private static long mixSeed(long seed, int spanIndex) {
            long mixed = seed;
            mixed ^= 0x9E3779B97F4A7C15L * (spanIndex + 1L);
            return mixed;
        }
    }
}
