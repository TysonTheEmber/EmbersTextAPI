package net.tysontheember.emberstextapi.core.markup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.core.style.GlyphEffect;
import net.tysontheember.emberstextapi.core.style.InlineAttachment;
import net.tysontheember.emberstextapi.core.style.ShakeState;
import net.tysontheember.emberstextapi.core.style.SpanEffectState;
import net.tysontheember.emberstextapi.core.style.SpanGradient;
import net.tysontheember.emberstextapi.core.style.TypewriterState;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;
import net.tysontheember.emberstextapi.immersivemessages.api.ObfuscateMode;
import net.tysontheember.emberstextapi.immersivemessages.api.ShakeType;
import net.tysontheember.emberstextapi.immersivemessages.api.TextAnchor;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;

/**
 * Central registry that maps markup tags to span effect factories.
 */
public final class SpanEffectRegistry {
    private static final Map<String, SpanEffectFactory> FACTORIES = new ConcurrentHashMap<>();

    private static final ResourceLocation ATTACHMENT_ITEM = ResourceLocation.fromNamespaceAndPath("emberstextapi", "item");
    private static final ResourceLocation ATTACHMENT_ENTITY = ResourceLocation.fromNamespaceAndPath("emberstextapi", "entity");
    private static final ResourceLocation EFFECT_OBFUSCATE = ResourceLocation.fromNamespaceAndPath("emberstextapi", "obfuscate");

    static {
        registerDefaults();
    }

    private SpanEffectRegistry() {
    }

    private static void registerDefaults() {
        registerSimple("bold", span -> span.bold(true));
        registerAlias("bold", "b");

        registerSimple("italic", span -> span.italic(true));
        registerAlias("italic", "i");

        registerSimple("underline", span -> span.underline(true));
        registerAlias("underline", "u");

        registerSimple("strikethrough", span -> span.strikethrough(true));
        registerAlias("strikethrough", "s");

        registerSimple("obfuscated", span -> span.obfuscated(true));
        registerAlias("obfuscated", "obf");

        register("color", context -> {
            String value = firstNonNull(context.attribute("value"), context.attribute("color"));
            TextColor color = parseTextColor(value);
            if (color == null) {
                return ActiveSpanEffect.noop();
            }
            return new SimpleActiveSpanEffect(span -> span.color(color));
        }, "c");

        register("font", context -> {
            String value = firstNonNull(context.attribute("value"), context.attribute("font"));
            ResourceLocation font = value != null ? ResourceLocation.tryParse(value) : null;
            if (font == null) {
                return ActiveSpanEffect.noop();
            }
            return new SimpleActiveSpanEffect(span -> span.font(font));
        });

        register("grad", SpanEffectRegistry::createGradientEffect, "gradient");

        register("typewriter", context -> {
            float speed = context.floatAttribute("speed", 1.0f);
            boolean center = context.booleanAttribute("center", false);
            TypewriterState state = new TypewriterState(speed, center, 0, 0);
            return new SimpleActiveSpanEffect(span -> span.globalTypewriter(speed, center), spanState -> {
                if (spanState != null) {
                    spanState.setTypewriter(state);
                }
            });
        }, "type");

        register("shake", context -> createShakeEffect(context, false, null));
        register("wave", context -> createShakeEffect(context, false, ShakeType.WAVE));
        register("wiggle", context -> createShakeEffect(context, true, null), "charshake");

        register("obfuscate", SpanEffectRegistry::createObfuscateEffect, "scramble");

        register("background", SpanEffectRegistry::createBackgroundEffect, "bg");
        register("backgroundgradient", SpanEffectRegistry::createBackgroundGradientEffect, "bggradient");

        register("scale", context -> {
            float scale = context.floatAttribute("value", 1.0f);
            return new SimpleActiveSpanEffect(span -> span.globalScale(scale));
        });

        register("offset", context -> {
            float x = context.floatAttribute("x", 0.0f);
            float y = context.floatAttribute("y", 0.0f);
            return new SimpleActiveSpanEffect(span -> span.globalOffset(x, y));
        });

        register("anchor", context -> {
            TextAnchor anchor = parseAnchor(context.attribute("value"));
            if (anchor == null) {
                return ActiveSpanEffect.noop();
            }
            return new SimpleActiveSpanEffect(span -> span.globalAnchor(anchor));
        });

        register("align", context -> {
            TextAnchor align = parseAnchor(context.attribute("value"));
            if (align == null) {
                return ActiveSpanEffect.noop();
            }
            return new SimpleActiveSpanEffect(span -> span.globalAlign(align));
        });

        register("shadow", context -> {
            boolean enabled = context.booleanAttribute("value", true);
            return new SimpleActiveSpanEffect(span -> span.globalShadow(enabled));
        });

        register("fade", context -> {
            int fadeIn = context.intAttribute("in", -1);
            int fadeOut = context.intAttribute("out", -1);
            return new SimpleActiveSpanEffect(span -> {
                if (fadeIn >= 0) {
                    span.globalFadeIn(fadeIn);
                }
                if (fadeOut >= 0) {
                    span.globalFadeOut(fadeOut);
                }
            });
        });

        register("item", SpanEffectRegistry::createItemEffect);
        register("entity", SpanEffectRegistry::createEntityEffect);
    }

    private static ActiveSpanEffect createGradientEffect(TagContext context) {
        String values = context.attribute("values");
        List<TextColor> textColors = new ArrayList<>();
        List<Integer> argb = new ArrayList<>();
        if (values != null) {
            String[] splits = values.split(",");
            for (String entry : splits) {
                TextColor color = parseTextColor(entry.trim());
                if (color == null) {
                    return ActiveSpanEffect.noop();
                }
                textColors.add(color);
                argb.add(expandAlpha(color.getValue()));
            }
        } else {
            String from = context.attribute("from");
            String to = context.attribute("to");
            if (from == null || to == null) {
                return ActiveSpanEffect.noop();
            }
            TextColor fromColor = parseTextColor(from);
            TextColor toColor = parseTextColor(to);
            if (fromColor == null || toColor == null) {
                return ActiveSpanEffect.noop();
            }
            textColors.add(fromColor);
            textColors.add(toColor);
            argb.add(expandAlpha(fromColor.getValue()));
            argb.add(expandAlpha(toColor.getValue()));
        }

        if (textColors.size() < 2) {
            return ActiveSpanEffect.noop();
        }

        TextColor[] gradient = textColors.toArray(new TextColor[0]);
        SpanGradient spanGradient = new SpanGradient(argb, false, 0.0f, 0.0f);
        return new SimpleActiveSpanEffect(span -> span.gradient(gradient), state -> {
            if (state != null) {
                state.setGradient(spanGradient);
            }
        });
    }

    private static ActiveSpanEffect createShakeEffect(TagContext context, boolean perGlyph, ShakeType forcedType) {
        final ShakeType type = forcedType != null ? forcedType : parseShakeType(context.attribute("type"));
        final float amplitude = context.floatAttribute("amplitude", 1.0f);
        final Optional<Float> speed = context.optionalFloatAttribute("speed");
        final Optional<Float> wavelength = context.optionalFloatAttribute("wavelength");

        Consumer<TextSpan> consumer;
        if (perGlyph) {
            consumer = span -> {
                if (speed.isPresent() && wavelength.isPresent()) {
                    float speedValue = speed.get();
                    float wavelengthValue = wavelength.get();
                    span.charShake(type, amplitude, speedValue, wavelengthValue);
                } else if (speed.isPresent()) {
                    float speedValue = speed.get();
                    span.charShake(type, amplitude, speedValue);
                } else {
                    span.charShake(type, amplitude);
                }
            };
        } else {
            consumer = span -> {
                if (speed.isPresent() && wavelength.isPresent()) {
                    float speedValue = speed.get();
                    float wavelengthValue = wavelength.get();
                    span.shake(type, amplitude, speedValue, wavelengthValue);
                } else if (speed.isPresent()) {
                    float speedValue = speed.get();
                    span.shake(type, amplitude, speedValue);
                } else {
                    span.shake(type, amplitude);
                }
            };
        }

        return new SimpleActiveSpanEffect(consumer, state -> {
            if (state != null) {
                float speedValue = speed.orElse(0.0f);
                float wavelengthValue = wavelength.orElse(0.0f);
                state.shakes().add(new ShakeState(type, amplitude, speedValue, wavelengthValue, perGlyph));
            }
        });
    }

    private static ActiveSpanEffect createObfuscateEffect(TagContext context) {
        final ObfuscateMode mode = parseObfuscateMode(context.attribute("mode"));
        final float speed = context.floatAttribute("speed", 1.0f);
        return new SimpleActiveSpanEffect(span -> span.obfuscate(mode, speed), state -> {
            if (state != null) {
                CompoundTag payload = new CompoundTag();
                payload.putString("mode", mode.name().toLowerCase(Locale.ROOT));
                payload.putFloat("speed", speed);
                state.glyphEffects().add(new GlyphEffect(EFFECT_OBFUSCATE, payload));
            }
        });
    }

    private static ActiveSpanEffect createBackgroundEffect(TagContext context) {
        String colorStr = context.attribute("color");
        String borderColorStr = context.attribute("bordercolor");
        String borderStartStr = context.attribute("borderstart");
        String borderEndStr = context.attribute("borderend");

        ImmersiveColor color = parseImmersiveColor(colorStr);
        ImmersiveColor borderColor = parseImmersiveColor(borderColorStr);
        ImmersiveColor borderStart = parseImmersiveColor(borderStartStr);
        ImmersiveColor borderEnd = parseImmersiveColor(borderEndStr);

        return new SimpleActiveSpanEffect(span -> {
            if (color != null) {
                span.globalBackgroundColor(color);
            } else {
                span.globalBackground(true);
            }
            if (borderColor != null) {
                span.globalBorder(borderColor, borderColor);
            }
            if (borderStart != null && borderEnd != null) {
                span.globalBorder(borderStart, borderEnd);
            }
        });
    }

    private static ActiveSpanEffect createBackgroundGradientEffect(TagContext context) {
        ImmersiveColor from = parseImmersiveColor(context.attribute("from"));
        ImmersiveColor to = parseImmersiveColor(context.attribute("to"));
        if (from == null || to == null) {
            return ActiveSpanEffect.noop();
        }
        return new SimpleActiveSpanEffect(span -> span.globalBackgroundGradient(from, to));
    }

    private static ActiveSpanEffect createItemEffect(TagContext context) {
        String itemId = firstNonNull(context.attribute("value"), context.attribute("id"));
        if (itemId == null) {
            return ActiveSpanEffect.noop();
        }
        int count = context.intAttribute("size", context.intAttribute("count", 1));
        float offsetX = context.floatAttribute("offsetx", context.floatAttribute("x", 0.0f));
        float offsetY = context.floatAttribute("offsety", context.floatAttribute("y", 0.0f));

        return new SimpleActiveSpanEffect(span -> {
            span.item(itemId, count);
            if (offsetX != 0.0f || offsetY != 0.0f) {
                span.itemOffset(offsetX, offsetY);
            }
        }, state -> {
            if (state != null) {
                CompoundTag payload = new CompoundTag();
                payload.putString("id", itemId);
                payload.putInt("count", Math.max(count, 1));
                payload.putFloat("offset_x", offsetX);
                payload.putFloat("offset_y", offsetY);
                state.attachments().add(new InlineAttachment(ATTACHMENT_ITEM, payload));
            }
        }, closeContext -> {
            TextSpan span = new TextSpan("");
            span.item(itemId, count);
            if (offsetX != 0.0f || offsetY != 0.0f) {
                span.itemOffset(offsetX, offsetY);
            }
            closeContext.addSpan(span);
        });
    }

    private static ActiveSpanEffect createEntityEffect(TagContext context) {
        String entityId = firstNonNull(context.attribute("value"), context.attribute("id"));
        if (entityId == null) {
            return ActiveSpanEffect.noop();
        }
        float scale = context.floatAttribute("scale", 1.0f);
        float offsetX = context.floatAttribute("offsetx", context.floatAttribute("x", 0.0f));
        float offsetY = context.floatAttribute("offsety", context.floatAttribute("y", 0.0f));
        float yaw = context.floatAttribute("yaw", 45.0f);
        float pitch = context.floatAttribute("pitch", 0.0f);
        String animation = firstNonNull(context.attribute("animation"), context.attribute("anim"));

        return new SimpleActiveSpanEffect(span -> {
            span.entity(entityId, scale);
            if (offsetX != 0.0f || offsetY != 0.0f) {
                span.entityOffset(offsetX, offsetY);
            }
            span.entityRotation(yaw, pitch);
            if (animation != null && !animation.isEmpty()) {
                span.entityAnimation(animation);
            }
        }, state -> {
            if (state != null) {
                CompoundTag payload = new CompoundTag();
                payload.putString("id", entityId);
                payload.putFloat("scale", scale);
                payload.putFloat("offset_x", offsetX);
                payload.putFloat("offset_y", offsetY);
                payload.putFloat("yaw", yaw);
                payload.putFloat("pitch", pitch);
                if (animation != null && !animation.isEmpty()) {
                    payload.putString("animation", animation);
                }
                state.attachments().add(new InlineAttachment(ATTACHMENT_ENTITY, payload));
            }
        }, closeContext -> {
            TextSpan span = new TextSpan("");
            span.entity(entityId, scale);
            if (offsetX != 0.0f || offsetY != 0.0f) {
                span.entityOffset(offsetX, offsetY);
            }
            span.entityRotation(yaw, pitch);
            if (animation != null && !animation.isEmpty()) {
                span.entityAnimation(animation);
            }
            closeContext.addSpan(span);
        });
    }

    private static void registerSimple(String name, Consumer<TextSpan> consumer) {
        register(name, context -> new SimpleActiveSpanEffect(consumer));
    }

    private static void registerAlias(String existing, String alias) {
        SpanEffectFactory factory = FACTORIES.get(existing.toLowerCase(Locale.ROOT));
        if (factory != null) {
            FACTORIES.put(alias.toLowerCase(Locale.ROOT), factory);
        }
    }

    public static void register(String name, SpanEffectFactory factory, String... aliases) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(factory, "factory");
        FACTORIES.put(name.toLowerCase(Locale.ROOT), factory);
        if (aliases != null) {
            for (String alias : aliases) {
                if (alias != null && !alias.isEmpty()) {
                    FACTORIES.put(alias.toLowerCase(Locale.ROOT), factory);
                }
            }
        }
    }

    public static SpanEffectFactory getFactory(String name) {
        if (name == null) {
            return null;
        }
        return FACTORIES.get(name.toLowerCase(Locale.ROOT));
    }

    public interface SpanEffectFactory {
        ActiveSpanEffect create(TagContext context);
    }

    public interface ActiveSpanEffect {
        void applyToTextSpan(TextSpan span);

        default void applyToEffectState(SpanEffectState state) {
        }

        default void onClose(SpanEffectCloseContext context) {
        }

        static ActiveSpanEffect noop() {
            return new ActiveSpanEffect() {
                @Override
                public void applyToTextSpan(TextSpan span) {
                }
            };
        }
    }

    public static final class SimpleActiveSpanEffect implements ActiveSpanEffect {
        private final Consumer<TextSpan> spanConsumer;
        private final Consumer<SpanEffectState> stateConsumer;
        private final Consumer<SpanEffectCloseContext> closeConsumer;

        public SimpleActiveSpanEffect(Consumer<TextSpan> spanConsumer) {
            this(spanConsumer, null, null);
        }

        public SimpleActiveSpanEffect(Consumer<TextSpan> spanConsumer, Consumer<SpanEffectState> stateConsumer) {
            this(spanConsumer, stateConsumer, null);
        }

        public SimpleActiveSpanEffect(Consumer<TextSpan> spanConsumer, Consumer<SpanEffectState> stateConsumer,
                Consumer<SpanEffectCloseContext> closeConsumer) {
            this.spanConsumer = spanConsumer == null ? span -> {
            } : spanConsumer;
            this.stateConsumer = stateConsumer;
            this.closeConsumer = closeConsumer;
        }

        @Override
        public void applyToTextSpan(TextSpan span) {
            if (spanConsumer != null && span != null) {
                spanConsumer.accept(span);
            }
        }

        @Override
        public void applyToEffectState(SpanEffectState state) {
            if (stateConsumer != null && state != null) {
                stateConsumer.accept(state);
            }
        }

        @Override
        public void onClose(SpanEffectCloseContext context) {
            if (closeConsumer != null && context != null) {
                closeConsumer.accept(context);
            }
        }
    }

    public static final class TagContext {
        private final String name;
        private final Map<String, String> attributes;

        public TagContext(String name, Map<String, String> attributes) {
            this.name = Objects.requireNonNull(name, "name");
            this.attributes = attributes == null ? Collections.emptyMap() : new HashMap<>(attributes);
        }

        public String name() {
            return name;
        }

        public Map<String, String> attributes() {
            return Collections.unmodifiableMap(attributes);
        }

        public String attribute(String key) {
            if (key == null) {
                return null;
            }
            return attributes.get(key.toLowerCase(Locale.ROOT));
        }

        public boolean booleanAttribute(String key, boolean defaultValue) {
            String value = attribute(key);
            if (value == null) {
                return defaultValue;
            }
            return Boolean.parseBoolean(value);
        }

        public float floatAttribute(String key, float defaultValue) {
            String value = attribute(key);
            if (value == null) {
                return defaultValue;
            }
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException ex) {
                return defaultValue;
            }
        }

        public Optional<Float> optionalFloatAttribute(String key) {
            String value = attribute(key);
            if (value == null) {
                return Optional.empty();
            }
            try {
                return Optional.of(Float.parseFloat(value));
            } catch (NumberFormatException ex) {
                return Optional.empty();
            }
        }

        public int intAttribute(String key, int defaultValue) {
            String value = attribute(key);
            if (value == null) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                return defaultValue;
            }
        }
    }

    public static final class SpanEffectCloseContext {
        private final TagContext tagContext;
        private final List<TextSpan> output;

        public SpanEffectCloseContext(TagContext tagContext, List<TextSpan> output) {
            this.tagContext = tagContext;
            this.output = output;
        }

        public TagContext tag() {
            return tagContext;
        }

        public void addSpan(TextSpan span) {
            if (span != null) {
                output.add(span);
            }
        }
    }

    private static ShakeType parseShakeType(String value) {
        if (value == null || value.isEmpty()) {
            return ShakeType.RANDOM;
        }
        try {
            return ShakeType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ShakeType.RANDOM;
        }
    }

    private static ObfuscateMode parseObfuscateMode(String value) {
        if (value == null || value.isEmpty()) {
            return ObfuscateMode.RANDOM;
        }
        try {
            return ObfuscateMode.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return ObfuscateMode.RANDOM;
        }
    }

    private static TextColor parseTextColor(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        String trimmed = value.trim();
        ChatFormatting formatting = ChatFormatting.getByName(trimmed);
        if (formatting != null && formatting.getColor() != null) {
            return TextColor.fromLegacyFormat(formatting);
        }
        return TextColor.parseColor(trimmed);
    }

    private static ImmersiveColor parseImmersiveColor(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String v = value.trim();
        try {
            if (v.startsWith("#")) {
                v = v.substring(1);
            }
            if (v.startsWith("0x")) {
                v = v.substring(2);
            }
            if (v.length() == 8) {
                return new ImmersiveColor((int) Long.parseLong(v, 16));
            } else if (v.length() == 6) {
                return new ImmersiveColor(0xFF000000 | Integer.parseInt(v, 16));
            }
        } catch (NumberFormatException ignored) {
        }

        ChatFormatting fmt = ChatFormatting.getByName(value);
        if (fmt != null && fmt.getColor() != null) {
            return new ImmersiveColor(0xFF000000 | fmt.getColor());
        }

        TextColor parsed = TextColor.parseColor(value);
        if (parsed != null) {
            int c = parsed.getValue();
            if ((c & 0xFF000000) == 0) {
                c |= 0xFF000000;
            }
            return new ImmersiveColor(c);
        }
        return null;
    }

    private static TextAnchor parseAnchor(String value) {
        if (value == null) {
            return null;
        }
        try {
            return TextAnchor.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static String firstNonNull(String first, String second) {
        return first != null ? first : second;
    }

    private static int expandAlpha(int value) {
        return (value & 0xFF000000) == 0 ? value | 0xFF000000 : value;
    }
}
