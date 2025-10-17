package net.tysontheember.emberstextapi.text;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import org.slf4j.Logger;

import java.util.*;

/**
 * Registry holding attribute factories and built-in definitions.
 */
public final class Attributes {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<ResourceLocation, TextAttributeFactory> REGISTRY = new LinkedHashMap<>();

    public static final ResourceLocation BOLD = resolve("bold");
    public static final ResourceLocation ITALIC = resolve("italic");
    public static final ResourceLocation COLOR = resolve("color");
    public static final ResourceLocation GRAD = resolve("grad");
    public static final ResourceLocation TYPEWRITER = resolve("typewriter");
    public static final ResourceLocation WIGGLE = resolve("wiggle");

    private Attributes() {
    }

    public static ResourceLocation resolve(String id) {
        if (id.contains(":")) {
            ResourceLocation rl = ResourceLocation.tryParse(id);
            return rl == null ? new ResourceLocation(EmbersTextAPI.MODID, id) : rl;
        }
        return new ResourceLocation(EmbersTextAPI.MODID, id);
    }

    public static void register(TextAttributeFactory factory) {
        REGISTRY.put(factory.id(), factory);
    }

    public static TextAttributeFactory get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    public static Optional<TextAttributeFactory> maybe(ResourceLocation id) {
        return Optional.ofNullable(get(id));
    }

    public static CompiledEffect compile(Attribute attribute, int start, int end, CompileContext context) {
        TextAttributeFactory factory = REGISTRY.get(attribute.id());
        if (factory == null) {
            LOGGER.debug("Unknown attribute {}", attribute.id());
            return null;
        }
        Params params = factory.spec().bind(attribute.id(), attribute.params());
        try {
            TextEffect effect = factory.compile(params, context);
            if (effect == null) {
                return null;
            }
            return new CompiledEffect(effect, new TextEffect.SpanCtx(attribute, params, start, end));
        } catch (CompileException e) {
            LOGGER.debug("Failed to compile attribute {}: {}", attribute.id(), e.getMessage());
            return null;
        }
    }

    public record CompiledEffect(TextEffect effect, TextEffect.SpanCtx span) {
    }

    static {
        register(new BoldAttribute());
        register(new ItalicAttribute());
        register(new ColorAttribute());
        register(new GradAttribute());
        register(new TypewriterAttribute());
        register(new WiggleAttribute());
    }

    private static final class BoldAttribute implements TextAttributeFactory {
        private final ParamSpec spec = ParamSpec.builder().build();

        @Override
        public ResourceLocation id() {
            return BOLD;
        }

        @Override
        public ParamSpec spec() {
            return spec;
        }

        @Override
        public TextEffect compile(Params params, CompileContext context) {
            return (glyph, span, time) -> glyph.scale *= 1.1f;
        }
    }

    private static final class ItalicAttribute implements TextAttributeFactory {
        private final ParamSpec spec = ParamSpec.builder().build();

        @Override
        public ResourceLocation id() {
            return ITALIC;
        }

        @Override
        public ParamSpec spec() {
            return spec;
        }

        @Override
        public TextEffect compile(Params params, CompileContext context) {
            return (glyph, span, time) -> glyph.xOffset += glyph.indexInSpan * 0.5f;
        }
    }

    private static final class ColorAttribute implements TextAttributeFactory {
        private final ParamSpec spec = ParamSpec.builder()
            .optional("value", ParamSpec.ParamType.COLOR, 0xFFFFFFFF)
            .build();

        @Override
        public ResourceLocation id() {
            return COLOR;
        }

        @Override
        public ParamSpec spec() {
            return spec;
        }

        @Override
        public TextEffect compile(Params params, CompileContext context) {
            final int colour = params.getColor("value", 0xFFFFFFFF);
            return (glyph, span, time) -> glyph.color = colour;
        }
    }

    private static final class GradAttribute implements TextAttributeFactory {
        private final ParamSpec spec = ParamSpec.builder()
            .optional("from", ParamSpec.ParamType.COLOR, 0xFFFFFFFF)
            .optional("to", ParamSpec.ParamType.COLOR, 0xFFFFFFFF)
            .optional("hue", ParamSpec.ParamType.BOOLEAN, false)
            .optional("f", ParamSpec.ParamType.FLOAT, 0f)
            .optional("sp", ParamSpec.ParamType.FLOAT, 20f)
            .optional("uni", ParamSpec.ParamType.BOOLEAN, false)
            .build();

        @Override
        public ResourceLocation id() {
            return GRAD;
        }

        @Override
        public ParamSpec spec() {
            return spec;
        }

        @Override
        public TextEffect compile(Params params, CompileContext context) {
            final int from = params.getColor("from", 0xFFFFFFFF);
            final int to = params.getColor("to", 0xFFFFFFFF);
            return (glyph, span, time) -> {
                int length = Math.max(1, span.length());
                float t = glyph.indexInSpan / (float) length;
                glyph.color = lerpColor(from, to, t);
            };
        }

        private int lerpColor(int a, int b, float t) {
            int ar = (a >> 16) & 0xFF;
            int ag = (a >> 8) & 0xFF;
            int ab = a & 0xFF;
            int aa = (a >> 24) & 0xFF;
            int br = (b >> 16) & 0xFF;
            int bg = (b >> 8) & 0xFF;
            int bb = b & 0xFF;
            int ba = (b >> 24) & 0xFF;
            int r = ar + Math.round((br - ar) * t);
            int g = ag + Math.round((bg - ag) * t);
            int bl = ab + Math.round((bb - ab) * t);
            int al = aa + Math.round((ba - aa) * t);
            return (al << 24) | (r << 16) | (g << 8) | bl;
        }
    }

    private static final class TypewriterAttribute implements TextAttributeFactory {
        private final ParamSpec spec = ParamSpec.builder()
            .optional("speed", ParamSpec.ParamType.FLOAT, 24f)
            .optionalEnum("by", "char", "char", "word")
            .optional("delay", ParamSpec.ParamType.FLOAT, 0f)
            .build();

        @Override
        public ResourceLocation id() {
            return TYPEWRITER;
        }

        @Override
        public ParamSpec spec() {
            return spec;
        }

        @Override
        public TextEffect compile(Params params, CompileContext context) {
            final float speed = params.getFloat("speed", 24f);
            final float delay = params.getFloat("delay", 0f);
            return (glyph, span, time) -> {
                float elapsed = Math.max(0f, time - delay);
                float progress = elapsed * speed;
                int threshold = glyph.indexInSpan;
                if (progress < threshold) {
                    glyph.alpha = 0f;
                }
            };
        }
    }

    private static final class WiggleAttribute implements TextAttributeFactory {
        private final ParamSpec spec = ParamSpec.builder()
            .optional("a", ParamSpec.ParamType.FLOAT, 1f)
            .optional("f", ParamSpec.ParamType.FLOAT, 2f)
            .optional("w", ParamSpec.ParamType.FLOAT, 1f)
            .build();

        @Override
        public ResourceLocation id() {
            return WIGGLE;
        }

        @Override
        public ParamSpec spec() {
            return spec;
        }

        @Override
        public TextEffect compile(Params params, CompileContext context) {
            final float amplitude = params.getFloat("a", 1f);
            final float frequency = params.getFloat("f", 2f);
            final float wave = params.getFloat("w", 1f);
            return (glyph, span, time) -> {
                float phase = glyph.indexInSpan / Math.max(1f, span.length()) * wave;
                glyph.yOffset += (float) Math.sin((time + phase) * frequency) * amplitude;
            };
        }
    }
}
