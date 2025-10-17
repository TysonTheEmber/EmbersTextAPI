package net.tysontheember.emberstextapi.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry containing all known {@link TextAttributeFactory} instances as well
 * as helpers to compile {@link Attribute} definitions into runtime
 * {@link TextEffect}s.
 */
public final class Effects {
    private static final Map<EmbersKey, TextAttributeFactory> FACTORIES = new ConcurrentHashMap<>();
    private static final Map<CacheKey, TextEffect> CACHE = new ConcurrentHashMap<>();

    private static final Random SHARED_RANDOM = new Random();

    private Effects() {
    }

    public static void register(TextAttributeFactory factory) {
        Objects.requireNonNull(factory, "factory");
        EmbersKey id = factory.id();
        if (id == null) {
            throw new IllegalArgumentException("Factory provided null id");
        }
        FACTORIES.put(id, factory);
    }

    public static TextEffect compile(Attribute attribute, TextEffect.CompileContext context) {
        Objects.requireNonNull(attribute, "attribute");
        TextAttributeFactory factory = FACTORIES.get(attribute.id());
        if (factory == null) {
            return (ctx, state) -> {};
        }
        ParamSpec spec = factory.spec();
        Params validated = spec.validate(attribute.params().raw(), context.warningSink());
        CacheKey key = new CacheKey(attribute.id(), validated, spanHash(context.span(), context.text()));
        return CACHE.computeIfAbsent(key, cacheKey -> factory.compile(validated, context));
    }

    public static List<TextEffect> compileAll(List<Attribute> attributes, TextEffect.CompileContext context) {
        if (attributes == null || attributes.isEmpty()) {
            return List.of();
        }
        List<TextEffect> effects = new ArrayList<>(attributes.size());
        for (Attribute attribute : attributes) {
            effects.add(compile(attribute, context));
        }
        return Collections.unmodifiableList(effects);
    }

    static {
        register(new BoldFactory());
        register(new ItalicFactory());
        register(new ColorFactory());
        register(new GradientFactory());
        register(new TypewriterFactory());
        register(new WiggleFactory("wiggle", false));
        register(new WiggleFactory("shake", true));
        register(new FadeFactory());
        register(new ShadowFactory());
    }

    private record CacheKey(EmbersKey id, Params params, int spanHash) {
    }

    private static int spanHash(Span span, AttributedText text) {
        if (span == null || text == null) {
            return 0;
        }
        int start = Math.max(0, span.start());
        int end = Math.max(start, Math.min(text.length(), span.end()));
        int len = end - start;
        int hash = 31 * len;
        if (len > 0) {
            hash = 31 * hash + text.text().substring(start, end).hashCode();
        }
        return hash;
    }

    private abstract static class BaseFactory implements TextAttributeFactory {
        private final EmbersKey id;
        private final ParamSpec spec;

        BaseFactory(String path, ParamSpec spec) {
            this.id = path.contains(":") ? EmbersKey.parse(path) : EmbersKey.of(EmbersKey.DEFAULT_NAMESPACE, path);
            this.spec = spec == null ? ParamSpec.builder().build() : spec;
        }

        @Override
        public EmbersKey id() {
            return id;
        }

        @Override
        public ParamSpec spec() {
            return spec;
        }
    }

    private static class BoldFactory extends BaseFactory {
        BoldFactory() {
            super("bold", ParamSpec.builder().build());
        }

        @Override
        public TextEffect compile(Params params, TextEffect.CompileContext context) {
            return (glyphContext, state) -> state.setBold(true);
        }
    }

    private static class ItalicFactory extends BaseFactory {
        ItalicFactory() {
            super("italic", ParamSpec.builder().build());
        }

        @Override
        public TextEffect compile(Params params, TextEffect.CompileContext context) {
            return (glyphContext, state) -> state.setItalic(true);
        }
    }

    private static class ColorFactory extends BaseFactory {
        ColorFactory() {
            super("color", ParamSpec.builder()
                    .add("value", ParamType.COLOR, 0xFFFFFFFF)
                    .alias("value", "v")
                    .alias("value", "color")
                    .build());
        }

        @Override
        public TextEffect compile(Params params, TextEffect.CompileContext context) {
            int fallback = context.environment().baseColor();
            int color = params.getColor("value", fallback);
            return (glyphContext, state) -> state.setColor(color);
        }
    }

    private static class GradientFactory extends BaseFactory {
        GradientFactory() {
            super("grad", ParamSpec.builder()
                    .add("from", ParamType.COLOR, 0xFFFFFFFF)
                    .add("to", ParamType.COLOR, 0xFFFFFFFF)
                    .add("hue", ParamType.BOOLEAN, Boolean.FALSE)
                    .add("f", ParamType.FLOAT, 0f)
                    .add("sp", ParamType.FLOAT, 1f)
                    .add("uni", ParamType.BOOLEAN, Boolean.FALSE)
                    .build());
        }

        @Override
        public TextEffect compile(Params params, TextEffect.CompileContext context) {
            final int from = params.getColor("from", context.environment().baseColor());
            final int to = params.getColor("to", context.environment().baseColor());
            final boolean hue = params.getBoolean("hue", false);
            final float flow = params.getFloat("f", 0f);
            final float spanScale = Math.max(0.0001f, params.getFloat("sp", 1f));
            final boolean uniform = params.getBoolean("uni", false);
            return (glyphContext, state) -> {
                float t = uniform ? 0f : glyphContext.spanProgress();
                t *= spanScale;
                float elapsed = glyphContext.timeSeconds() - context.environment().animationStartTime();
                if (Float.isFinite(elapsed)) {
                    t += elapsed * flow;
                }
                t = Math.max(0f, Math.min(1f, t));
                int color = hue ? ColorUtil.lerpHsv(from, to, t) : ColorUtil.lerpRgb(from, to, t);
                state.setColor(color);
            };
        }
    }

    private static class TypewriterFactory extends BaseFactory {
        TypewriterFactory() {
            super("typewriter", ParamSpec.builder()
                    .add("speed", ParamType.FLOAT, 30f)
                    .add("by", ParamType.STRING, "char")
                    .add("delay", ParamType.FLOAT, 0f)
                    .build());
        }

        @Override
        public TextEffect compile(Params params, TextEffect.CompileContext context) {
            final float speed = Math.max(0f, params.getFloat("speed", 30f));
            final float delay = Math.max(0f, params.getFloat("delay", 0f));
            final String by = params.getString("by", "char").toLowerCase(Locale.ROOT);
            final int[] wordBoundaries = buildWordBoundaries(context.span(), context.text());
            final boolean byWord = "word".equals(by);
            return (glyphContext, state) -> {
                float elapsed = glyphContext.timeSeconds() - context.environment().animationStartTime() - delay;
                if (elapsed < 0f) {
                    state.setVisible(false);
                    return;
                }
                if (speed <= 0f) {
                    return;
                }
                if (byWord) {
                    int visibleWords = Math.max(0, (int) Math.floor(elapsed * speed));
                    int wordIndex = glyphContext.spanLocalIndex() < wordBoundaries.length ? wordBoundaries[glyphContext.spanLocalIndex()] : 0;
                    if (wordIndex >= visibleWords) {
                        state.setVisible(false);
                    }
                } else {
                    int visible = Math.max(0, (int) Math.floor(elapsed * speed));
                    if (glyphContext.spanLocalIndex() > visible) {
                        state.setVisible(false);
                    }
                }
            };
        }

        private static int[] buildWordBoundaries(Span span, AttributedText text) {
            int length = span.end() - span.start();
            int[] result = new int[Math.max(0, length)];
            if (length <= 0) {
                return result;
            }
            String raw = text.text().substring(span.start(), span.end());
            int word = 0;
            boolean inWord = false;
            for (int i = 0; i < raw.length(); i++) {
                char c = raw.charAt(i);
                if (Character.isWhitespace(c)) {
                    inWord = false;
                } else {
                    if (!inWord) {
                        inWord = true;
                        word++;
                    }
                }
                result[i] = word;
            }
            return result;
        }
    }

    private static class WiggleFactory extends BaseFactory {
        private final boolean randomised;

        WiggleFactory(String path, boolean randomised) {
            super(path, ParamSpec.builder()
                    .add("a", ParamType.FLOAT, 1f)
                    .add("f", ParamType.FLOAT, 2f)
                    .add("w", ParamType.FLOAT, 0f)
                    .build());
            this.randomised = randomised;
        }

        @Override
        public TextEffect compile(Params params, TextEffect.CompileContext context) {
            final float amplitude = params.getFloat("a", 1f);
            final float frequency = params.getFloat("f", 2f);
            final float wave = params.getFloat("w", 0f);
            return (glyphContext, state) -> {
                float elapsed = glyphContext.timeSeconds() - context.environment().animationStartTime();
                float offset = glyphContext.spanLocalIndex() * wave;
                if (randomised) {
                    long seed = context.environment().seed() ^ glyphContext.seed() ^ (glyphContext.glyphIndex() * 31L);
                    SHARED_RANDOM.setSeed(seed);
                    float angle = elapsed * frequency;
                    float x = (SHARED_RANDOM.nextFloat() - 0.5f) * 2f;
                    float y = (SHARED_RANDOM.nextFloat() - 0.5f) * 2f;
                    state.setOffsetX(state.offsetX() + x * amplitude);
                    state.setOffsetY(state.offsetY() + y * amplitude);
                } else {
                    float angle = elapsed * frequency + offset;
                    float y = (float) Math.sin(angle) * amplitude;
                    state.setOffsetY(state.offsetY() + y);
                }
            };
        }
    }

    private static class FadeFactory extends BaseFactory {
        FadeFactory() {
            super("fade", ParamSpec.builder()
                    .add("in", ParamType.FLOAT, 0f)
                    .add("out", ParamType.FLOAT, 0f)
                    .build());
        }

        @Override
        public TextEffect compile(Params params, TextEffect.CompileContext context) {
            final float fadeIn = Math.max(0f, params.getFloat("in", 0f));
            final float fadeOut = Math.max(0f, params.getFloat("out", 0f));
            return (glyphContext, state) -> {
                float elapsed = glyphContext.timeSeconds() - context.environment().animationStartTime();
                float alpha = state.alpha();
                if (fadeIn > 0f) {
                    alpha *= Math.min(1f, Math.max(0f, elapsed / fadeIn));
                }
                if (fadeOut > 0f) {
                    float t = Math.max(0f, elapsed);
                    alpha *= Math.max(0f, 1f - t / fadeOut);
                }
                state.setAlpha(alpha);
            };
        }
    }

    private static class ShadowFactory extends BaseFactory {
        ShadowFactory() {
            super("shadow", ParamSpec.builder()
                    .add("offset", ParamType.FLOAT, 1f)
                    .add("alpha", ParamType.FLOAT, 0.7f)
                    .build());
        }

        @Override
        public TextEffect compile(Params params, TextEffect.CompileContext context) {
            final float offset = params.getFloat("offset", 1f);
            final float alpha = Math.max(0f, Math.min(1f, params.getFloat("alpha", 0.7f)));
            return (glyphContext, state) -> {
                state.setShadow(true);
                state.setShadowOffset(offset, offset);
                state.setShadowAlpha(alpha);
            };
        }
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
