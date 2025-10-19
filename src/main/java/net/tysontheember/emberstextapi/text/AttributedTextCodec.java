package net.tysontheember.emberstextapi.text;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.tysontheember.emberstextapi.text.effect.Effect;
import net.tysontheember.emberstextapi.text.effect.EffectParameterConverters;
import net.tysontheember.emberstextapi.text.effect.FadeEffect;
import net.tysontheember.emberstextapi.text.effect.GradientEffect;
import net.tysontheember.emberstextapi.text.effect.RainbowEffect;
import net.tysontheember.emberstextapi.text.effect.ShakeEffect;
import net.tysontheember.emberstextapi.text.effect.TypewriterEffect;
import net.tysontheember.emberstextapi.text.effect.WaveEffect;
import net.tysontheember.emberstextapi.text.effect.WiggleEffect;
import org.slf4j.Logger;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Serialises {@link AttributedText} instances to and from NBT / JSON payloads.
 */
public final class AttributedTextCodec {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final int SCHEMA_VERSION = 1;
    private static final String VERSION_KEY = "version";
    private static final String TEXT_KEY = "text";
    private static final String SPANS_KEY = "spans";
    private static final String START_KEY = "start";
    private static final String END_KEY = "end";
    private static final String ATTRIBUTES_KEY = "attributes";
    private static final String COLOR_KEY = "color";
    private static final String GRADIENT_KEY = "gradient";
    private static final String STYLE_KEY = "style";
    private static final String BACKGROUND_KEY = "background";
    private static final String EFFECTS_KEY = "effects";
    private static final String EFFECT_PARAMS_KEY = "effectParams";
    private static final String RESERVED_KEY = "reserved";

    private AttributedTextCodec() {
    }

    /**
     * Encodes the supplied {@link AttributedText} into an NBT structure.
     *
     * @param text attributed text to encode, {@code null} values result in an empty payload
     * @return compound tag containing the encoded representation
     */
    public static CompoundTag toNbt(AttributedText text) {
        CompoundTag root = new CompoundTag();
        root.putInt(VERSION_KEY, SCHEMA_VERSION);
        String value = text != null ? Objects.toString(text.getText(), "") : "";
        root.putString(TEXT_KEY, value);
        ListTag spansTag = new ListTag();
        if (text != null) {
            for (Span span : text.getSpans()) {
                if (span == null) {
                    continue;
                }
                CompoundTag spanTag = new CompoundTag();
                spanTag.putInt(START_KEY, Math.max(0, span.getStart()));
                spanTag.putInt(END_KEY, Math.max(0, span.getEnd()));
                CompoundTag attributes = encodeAttributesNbt(span.getAttributes());
                if (!attributes.isEmpty()) {
                    spanTag.put(ATTRIBUTES_KEY, attributes);
                }
                spanTag.put(RESERVED_KEY, new CompoundTag());
                spansTag.add(spanTag);
            }
        }
        root.put(SPANS_KEY, spansTag);
        root.put(RESERVED_KEY, new CompoundTag());
        return root;
    }

    /**
     * Decodes an {@link AttributedText} instance from the provided NBT payload.
     *
     * @param tag encoded payload, may be {@code null}
     * @return decoded attributed text
     */
    public static AttributedText fromNbt(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return AttributedText.builder().build();
        }
        int version = tag.contains(VERSION_KEY, Tag.TAG_INT) ? tag.getInt(VERSION_KEY) : 0;
        if (version > SCHEMA_VERSION) {
            LOGGER.warn("Encountered future attributed text schema version {} (supported {})", version, SCHEMA_VERSION);
        }
        String text = tag.contains(TEXT_KEY, Tag.TAG_STRING) ? tag.getString(TEXT_KEY) : "";
        AttributedText.Builder builder = AttributedText.builder().text(text);
        if (tag.contains(SPANS_KEY, Tag.TAG_LIST)) {
            ListTag spans = tag.getList(SPANS_KEY, Tag.TAG_COMPOUND);
            for (Tag element : spans) {
                if (!(element instanceof CompoundTag spanTag)) {
                    continue;
                }
                int start = Math.max(0, spanTag.getInt(START_KEY));
                int end = Math.max(start, spanTag.getInt(END_KEY));
                Span.Builder spanBuilder = Span.builder().start(start).end(end);
                if (spanTag.contains(ATTRIBUTES_KEY, Tag.TAG_COMPOUND)) {
                    AttributeSet attributes = decodeAttributesNbt(spanTag.getCompound(ATTRIBUTES_KEY));
                    spanBuilder.attributes(attributes);
                }
                try {
                    builder.addSpan(spanBuilder.build());
                } catch (IllegalStateException ignored) {
                    // Skip invalid spans gracefully
                }
            }
        }
        return builder.build();
    }

    /**
     * Encodes the supplied attributed text into a JSON payload.
     *
     * @param text attributed text to encode
     * @return JSON object describing the attributed text
     */
    public static JsonObject toJson(AttributedText text) {
        JsonObject root = new JsonObject();
        root.addProperty(VERSION_KEY, SCHEMA_VERSION);
        root.addProperty(TEXT_KEY, text != null ? Objects.toString(text.getText(), "") : "");
        JsonArray spans = new JsonArray();
        if (text != null) {
            for (Span span : text.getSpans()) {
                if (span == null) {
                    continue;
                }
                JsonObject spanObject = new JsonObject();
                spanObject.addProperty(START_KEY, Math.max(0, span.getStart()));
                spanObject.addProperty(END_KEY, Math.max(0, span.getEnd()));
                JsonObject attributes = encodeAttributesJson(span.getAttributes());
                if (attributes.size() > 0) {
                    spanObject.add(ATTRIBUTES_KEY, attributes);
                }
                spanObject.add(RESERVED_KEY, new JsonObject());
                spans.add(spanObject);
            }
        }
        root.add(SPANS_KEY, spans);
        root.add(RESERVED_KEY, new JsonObject());
        return root;
    }

    /**
     * Decodes an attributed text instance from a JSON payload.
     *
     * @param json JSON object describing the attributed text
     * @return decoded attributed text
     */
    public static AttributedText fromJson(JsonObject json) {
        if (json == null || json.isJsonNull()) {
            return AttributedText.builder().build();
        }
        int version = json.has(VERSION_KEY) && json.get(VERSION_KEY).isJsonPrimitive()
                ? json.get(VERSION_KEY).getAsInt() : 0;
        if (version > SCHEMA_VERSION) {
            LOGGER.warn("Encountered future attributed text schema version {} (supported {})", version, SCHEMA_VERSION);
        }
        String text = json.has(TEXT_KEY) && json.get(TEXT_KEY).isJsonPrimitive()
                ? json.get(TEXT_KEY).getAsString() : "";
        AttributedText.Builder builder = AttributedText.builder().text(text);
        if (json.has(SPANS_KEY) && json.get(SPANS_KEY).isJsonArray()) {
            JsonArray spans = json.getAsJsonArray(SPANS_KEY);
            for (JsonElement element : spans) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject spanObject = element.getAsJsonObject();
                int start = spanObject.has(START_KEY) && spanObject.get(START_KEY).isJsonPrimitive()
                        ? Math.max(0, spanObject.get(START_KEY).getAsInt()) : 0;
                int end = spanObject.has(END_KEY) && spanObject.get(END_KEY).isJsonPrimitive()
                        ? Math.max(start, spanObject.get(END_KEY).getAsInt()) : start;
                Span.Builder spanBuilder = Span.builder().start(start).end(end);
                if (spanObject.has(ATTRIBUTES_KEY) && spanObject.get(ATTRIBUTES_KEY).isJsonObject()) {
                    AttributeSet attributes = decodeAttributesJson(spanObject.getAsJsonObject(ATTRIBUTES_KEY));
                    spanBuilder.attributes(attributes);
                }
                try {
                    builder.addSpan(spanBuilder.build());
                } catch (IllegalStateException ignored) {
                    // Skip invalid spans gracefully
                }
            }
        }
        return builder.build();
    }

    /**
     * Decodes an attributed text instance from a JSON string payload.
     *
     * @param json encoded JSON string
     * @return decoded attributed text
     */
    public static AttributedText fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return AttributedText.builder().build();
        }
        try {
            JsonElement element = JsonParser.parseString(json);
            if (element.isJsonObject()) {
                return fromJson(element.getAsJsonObject());
            }
        } catch (JsonParseException e) {
            LOGGER.warn("Failed to parse attributed text JSON: {}", json, e);
        }
        return AttributedText.builder().build();
    }

    private static CompoundTag encodeAttributesNbt(AttributeSet attributes) {
        if (attributes == null) {
            return new CompoundTag();
        }
        CompoundTag tag = new CompoundTag();
        if (attributes.getColor() != null) {
            tag.putString(COLOR_KEY, attributes.getColor());
        }
        AttributeSet.Gradient gradient = attributes.getGradient();
        if (gradient != null) {
            CompoundTag gradientTag = new CompoundTag();
            if (gradient.getFrom() != null) {
                gradientTag.putString("from", gradient.getFrom());
            }
            if (gradient.getTo() != null) {
                gradientTag.putString("to", gradient.getTo());
            }
            gradientTag.putBoolean("hsv", gradient.isHsv());
            gradientTag.putDouble("flow", gradient.getFlow());
            gradientTag.putBoolean("span", gradient.isSpan());
            gradientTag.putBoolean("uni", gradient.isUni());
            tag.put(GRADIENT_KEY, gradientTag);
        }
        AttributeSet.Style style = attributes.getStyle();
        if (style != null) {
            CompoundTag styleTag = new CompoundTag();
            styleTag.putBoolean("bold", style.isBold());
            styleTag.putBoolean("italic", style.isItalic());
            styleTag.putBoolean("underlined", style.isUnderlined());
            styleTag.putBoolean("strikethrough", style.isStrikethrough());
            styleTag.putBoolean("obfuscated", style.isObfuscated());
            tag.put(STYLE_KEY, styleTag);
        }
        AttributeSet.Background background = attributes.getBackground();
        if (background != null) {
            CompoundTag backgroundTag = new CompoundTag();
            backgroundTag.putBoolean("on", background.isOn());
            if (background.getColor() != null) {
                backgroundTag.putString("color", background.getColor());
            }
            if (background.getBorder() != null) {
                backgroundTag.putString("border", background.getBorder());
            }
            backgroundTag.putDouble("alpha", background.getAlpha());
            tag.put(BACKGROUND_KEY, backgroundTag);
        }
        Map<String, Object> effectParams = new LinkedHashMap<>(attributes.getEffectParams());
        if (!attributes.getEffects().isEmpty()) {
            CompoundTag effectsTag = new CompoundTag();
            for (Map.Entry<String, Effect> entry : attributes.getEffects().entrySet()) {
                Map<String, Object> params = encodeEffect(entry.getValue());
                if (params.isEmpty()) {
                    effectParams.putIfAbsent(entry.getKey(), Boolean.TRUE);
                    continue;
                }
                effectsTag.put(entry.getKey(), writeObjectMap(params));
            }
            if (!effectsTag.isEmpty()) {
                tag.put(EFFECTS_KEY, effectsTag);
            }
        }
        if (!effectParams.isEmpty()) {
            tag.put(EFFECT_PARAMS_KEY, writeObjectMap(effectParams));
        }
        tag.put(RESERVED_KEY, new CompoundTag());
        return tag;
    }

    private static AttributeSet decodeAttributesNbt(CompoundTag tag) {
        AttributeSet.Builder builder = AttributeSet.builder();
        if (tag == null || tag.isEmpty()) {
            return builder.build();
        }
        if (tag.contains(COLOR_KEY, Tag.TAG_STRING)) {
            builder.color(tag.getString(COLOR_KEY));
        }
        if (tag.contains(GRADIENT_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag gradientTag = tag.getCompound(GRADIENT_KEY);
            AttributeSet.Gradient.Builder gradient = AttributeSet.Gradient.builder();
            if (gradientTag.contains("from", Tag.TAG_STRING)) {
                gradient.from(gradientTag.getString("from"));
            }
            if (gradientTag.contains("to", Tag.TAG_STRING)) {
                gradient.to(gradientTag.getString("to"));
            }
            gradient.hsv(gradientTag.getBoolean("hsv"));
            gradient.flow(gradientTag.contains("flow", Tag.TAG_DOUBLE)
                    ? gradientTag.getDouble("flow") : 0d);
            gradient.span(gradientTag.getBoolean("span"));
            gradient.uni(gradientTag.getBoolean("uni"));
            builder.gradient(gradient.build());
        }
        if (tag.contains(STYLE_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag styleTag = tag.getCompound(STYLE_KEY);
            AttributeSet.Style style = AttributeSet.Style.builder()
                    .bold(styleTag.getBoolean("bold"))
                    .italic(styleTag.getBoolean("italic"))
                    .underlined(styleTag.getBoolean("underlined"))
                    .strikethrough(styleTag.getBoolean("strikethrough"))
                    .obfuscated(styleTag.getBoolean("obfuscated"))
                    .build();
            builder.style(style);
        }
        if (tag.contains(BACKGROUND_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag backgroundTag = tag.getCompound(BACKGROUND_KEY);
            AttributeSet.Background.Builder background = AttributeSet.Background.builder()
                    .on(backgroundTag.getBoolean("on"))
                    .alpha(backgroundTag.contains("alpha", Tag.TAG_DOUBLE)
                            ? backgroundTag.getDouble("alpha") : 1.0d);
            if (backgroundTag.contains("color", Tag.TAG_STRING)) {
                background.color(backgroundTag.getString("color"));
            }
            if (backgroundTag.contains("border", Tag.TAG_STRING)) {
                background.border(backgroundTag.getString("border"));
            }
            builder.background(background.build());
        }
        if (tag.contains(EFFECTS_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag effectsTag = tag.getCompound(EFFECTS_KEY);
            for (String key : effectsTag.getAllKeys()) {
                if (!effectsTag.contains(key, Tag.TAG_COMPOUND)) {
                    continue;
                }
                Map<String, Object> params = readObjectMap(effectsTag.getCompound(key));
                if (!applyEffect(builder, key, params)) {
                    if (params.isEmpty()) {
                        builder.effectParam(key, Boolean.TRUE);
                    } else {
                        builder.effectParam(key, params);
                    }
                }
            }
        }
        if (tag.contains(EFFECT_PARAMS_KEY, Tag.TAG_COMPOUND)) {
            Map<String, Object> params = readObjectMap(tag.getCompound(EFFECT_PARAMS_KEY));
            builder.effectParams(params);
        }
        return builder.build();
    }

    private static JsonObject encodeAttributesJson(AttributeSet attributes) {
        JsonObject object = new JsonObject();
        if (attributes == null) {
            return object;
        }
        if (attributes.getColor() != null) {
            object.addProperty(COLOR_KEY, attributes.getColor());
        }
        AttributeSet.Gradient gradient = attributes.getGradient();
        if (gradient != null) {
            JsonObject gradientObject = new JsonObject();
            if (gradient.getFrom() != null) {
                gradientObject.addProperty("from", gradient.getFrom());
            }
            if (gradient.getTo() != null) {
                gradientObject.addProperty("to", gradient.getTo());
            }
            gradientObject.addProperty("hsv", gradient.isHsv());
            gradientObject.addProperty("flow", gradient.getFlow());
            gradientObject.addProperty("span", gradient.isSpan());
            gradientObject.addProperty("uni", gradient.isUni());
            object.add(GRADIENT_KEY, gradientObject);
        }
        AttributeSet.Style style = attributes.getStyle();
        if (style != null) {
            JsonObject styleObject = new JsonObject();
            styleObject.addProperty("bold", style.isBold());
            styleObject.addProperty("italic", style.isItalic());
            styleObject.addProperty("underlined", style.isUnderlined());
            styleObject.addProperty("strikethrough", style.isStrikethrough());
            styleObject.addProperty("obfuscated", style.isObfuscated());
            object.add(STYLE_KEY, styleObject);
        }
        AttributeSet.Background background = attributes.getBackground();
        if (background != null) {
            JsonObject backgroundObject = new JsonObject();
            backgroundObject.addProperty("on", background.isOn());
            if (background.getColor() != null) {
                backgroundObject.addProperty("color", background.getColor());
            }
            if (background.getBorder() != null) {
                backgroundObject.addProperty("border", background.getBorder());
            }
            backgroundObject.addProperty("alpha", background.getAlpha());
            object.add(BACKGROUND_KEY, backgroundObject);
        }
        Map<String, Object> effectParams = new LinkedHashMap<>(attributes.getEffectParams());
        if (!attributes.getEffects().isEmpty()) {
            JsonObject effectsObject = new JsonObject();
            for (Map.Entry<String, Effect> entry : attributes.getEffects().entrySet()) {
                Map<String, Object> params = encodeEffect(entry.getValue());
                if (params.isEmpty()) {
                    effectParams.putIfAbsent(entry.getKey(), Boolean.TRUE);
                    continue;
                }
                effectsObject.add(entry.getKey(), writeObjectMapJson(params));
            }
            if (effectsObject.size() > 0) {
                object.add(EFFECTS_KEY, effectsObject);
            }
        }
        if (!effectParams.isEmpty()) {
            object.add(EFFECT_PARAMS_KEY, writeObjectMapJson(effectParams));
        }
        object.add(RESERVED_KEY, new JsonObject());
        return object;
    }

    private static AttributeSet decodeAttributesJson(JsonObject object) {
        AttributeSet.Builder builder = AttributeSet.builder();
        if (object == null || object.isJsonNull()) {
            return builder.build();
        }
        if (object.has(COLOR_KEY) && object.get(COLOR_KEY).isJsonPrimitive()) {
            builder.color(object.get(COLOR_KEY).getAsString());
        }
        if (object.has(GRADIENT_KEY) && object.get(GRADIENT_KEY).isJsonObject()) {
            JsonObject gradientObject = object.getAsJsonObject(GRADIENT_KEY);
            AttributeSet.Gradient.Builder gradient = AttributeSet.Gradient.builder();
            if (gradientObject.has("from") && gradientObject.get("from").isJsonPrimitive()) {
                gradient.from(gradientObject.get("from").getAsString());
            }
            if (gradientObject.has("to") && gradientObject.get("to").isJsonPrimitive()) {
                gradient.to(gradientObject.get("to").getAsString());
            }
            gradient.hsv(asBoolean(gradientObject.get("hsv"), false));
            gradient.flow(asDouble(gradientObject.get("flow"), 0d));
            gradient.span(asBoolean(gradientObject.get("span"), false));
            gradient.uni(asBoolean(gradientObject.get("uni"), false));
            builder.gradient(gradient.build());
        }
        if (object.has(STYLE_KEY) && object.get(STYLE_KEY).isJsonObject()) {
            JsonObject styleObject = object.getAsJsonObject(STYLE_KEY);
            AttributeSet.Style style = AttributeSet.Style.builder()
                    .bold(asBoolean(styleObject.get("bold"), false))
                    .italic(asBoolean(styleObject.get("italic"), false))
                    .underlined(asBoolean(styleObject.get("underlined"), false))
                    .strikethrough(asBoolean(styleObject.get("strikethrough"), false))
                    .obfuscated(asBoolean(styleObject.get("obfuscated"), false))
                    .build();
            builder.style(style);
        }
        if (object.has(BACKGROUND_KEY) && object.get(BACKGROUND_KEY).isJsonObject()) {
            JsonObject backgroundObject = object.getAsJsonObject(BACKGROUND_KEY);
            AttributeSet.Background.Builder background = AttributeSet.Background.builder()
                    .on(asBoolean(backgroundObject.get("on"), false))
                    .alpha(asDouble(backgroundObject.get("alpha"), 1.0d));
            if (backgroundObject.has("color") && backgroundObject.get("color").isJsonPrimitive()) {
                background.color(backgroundObject.get("color").getAsString());
            }
            if (backgroundObject.has("border") && backgroundObject.get("border").isJsonPrimitive()) {
                background.border(backgroundObject.get("border").getAsString());
            }
            builder.background(background.build());
        }
        if (object.has(EFFECTS_KEY) && object.get(EFFECTS_KEY).isJsonObject()) {
            JsonObject effectsObject = object.getAsJsonObject(EFFECTS_KEY);
            for (Map.Entry<String, JsonElement> entry : effectsObject.entrySet()) {
                if (!entry.getValue().isJsonObject()) {
                    continue;
                }
                Map<String, Object> params = readObjectMap(entry.getValue().getAsJsonObject());
                if (!applyEffect(builder, entry.getKey(), params)) {
                    if (params.isEmpty()) {
                        builder.effectParam(entry.getKey(), Boolean.TRUE);
                    } else {
                        builder.effectParam(entry.getKey(), params);
                    }
                }
            }
        }
        if (object.has(EFFECT_PARAMS_KEY) && object.get(EFFECT_PARAMS_KEY).isJsonObject()) {
            Map<String, Object> params = readObjectMap(object.getAsJsonObject(EFFECT_PARAMS_KEY));
            builder.effectParams(params);
        }
        return builder.build();
    }

    private static boolean applyEffect(AttributeSet.Builder builder, String name, Map<String, Object> params) {
        return EffectParameterConverters.convert(name, params)
                .map(effect -> {
                    builder.effect(name, effect);
                    return true;
                })
                .orElse(false);
    }

    private static Map<String, Object> encodeEffect(Effect effect) {
        Map<String, Object> params = new LinkedHashMap<>();
        if (effect instanceof TypewriterEffect typewriter) {
            params.put("sp", typewriter.getSpeed());
        } else if (effect instanceof WaveEffect wave) {
            params.put("a", wave.getAmplitude());
            params.put("f", wave.getFrequency());
            params.put("w", wave.getWavelength());
        } else if (effect instanceof WiggleEffect wiggle) {
            params.put("a", wiggle.getAmplitude());
            params.put("f", wiggle.getFrequency());
            params.put("w", wiggle.getWavelength());
        } else if (effect instanceof ShakeEffect shake) {
            params.put("a", shake.getAmplitude());
            params.put("f", shake.getFrequency());
        } else if (effect instanceof GradientEffect gradient) {
            if (gradient.getFrom() != null) {
                params.put("from", formatColor(gradient.getFrom()));
            }
            if (gradient.getTo() != null) {
                params.put("to", formatColor(gradient.getTo()));
            }
            params.put("hsv", gradient.isHsv());
            params.put("f", gradient.getFlow());
            params.put("sp", gradient.isSpan());
            params.put("uni", gradient.isUniform());
        } else if (effect instanceof RainbowEffect rainbow) {
            params.put("hue", rainbow.getBaseHue());
            params.put("f", rainbow.getFrequency());
            params.put("sp", rainbow.getSpeed());
        } else if (effect instanceof FadeEffect fade) {
            params.put("sp", fade.getSpeed());
            params.put("f", fade.getFrequency());
        }
        return params;
    }

    private static String formatColor(Effect.Color color) {
        int argb = color.toArgb();
        return String.format(Locale.ROOT, "#%08X", argb);
    }

    private static CompoundTag writeObjectMap(Map<String, Object> map) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nested) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) nested;
                tag.put(key, writeObjectMap(nestedMap));
            } else if (value instanceof Boolean bool) {
                tag.putBoolean(key, bool);
            } else if (value instanceof Number number) {
                tag.putDouble(key, number.doubleValue());
            } else if (value != null) {
                tag.putString(key, value.toString());
            }
        }
        return tag;
    }

    private static Map<String, Object> readObjectMap(CompoundTag tag) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (String key : tag.getAllKeys()) {
            Tag element = tag.get(key);
            Object value = readTagValue(element);
            if (value != null) {
                map.put(key, value);
            }
        }
        return map;
    }

    private static Object readTagValue(Tag tag) {
        if (tag == null) {
            return null;
        }
        int id = tag.getId();
        if (id == Tag.TAG_COMPOUND) {
            return readObjectMap((CompoundTag) tag);
        }
        if (tag instanceof NumericTag numeric) {
            if (id == Tag.TAG_BYTE) {
                byte b = numeric.getAsByte();
                if (b == 0 || b == 1) {
                    return b == 1;
                }
                return (int) b;
            }
            if (id == Tag.TAG_INT) {
                return numeric.getAsInt();
            }
            if (id == Tag.TAG_LONG) {
                return numeric.getAsLong();
            }
            return numeric.getAsDouble();
        }
        if (tag instanceof StringTag stringTag) {
            return stringTag.getAsString();
        }
        if (tag instanceof DoubleTag doubleTag) {
            return doubleTag.getAsDouble();
        }
        return null;
    }

    private static JsonObject writeObjectMapJson(Map<String, Object> map) {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nested) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) nested;
                object.add(key, writeObjectMapJson(nestedMap));
            } else if (value instanceof Boolean bool) {
                object.addProperty(key, bool);
            } else if (value instanceof Number number) {
                object.addProperty(key, number);
            } else if (value != null) {
                object.addProperty(key, value.toString());
            } else {
                object.add(key, JsonNull.INSTANCE);
            }
        }
        return object;
    }

    private static Map<String, Object> readObjectMap(JsonObject object) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            Object value = readJsonValue(entry.getValue());
            if (value != null) {
                map.put(entry.getKey(), value);
            }
        }
        return map;
    }

    private static Object readJsonValue(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (element.isJsonObject()) {
            return readObjectMap(element.getAsJsonObject());
        }
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }
            if (primitive.isNumber()) {
                return primitive.getAsDouble();
            }
            if (primitive.isString()) {
                return primitive.getAsString();
            }
        }
        return null;
    }

    private static boolean asBoolean(JsonElement element, boolean fallback) {
        if (element == null || element.isJsonNull()) {
            return fallback;
        }
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }
            if (primitive.isString()) {
                return Boolean.parseBoolean(primitive.getAsString());
            }
            if (primitive.isNumber()) {
                return primitive.getAsInt() != 0;
            }
        }
        return fallback;
    }

    private static double asDouble(JsonElement element, double fallback) {
        if (element == null || element.isJsonNull()) {
            return fallback;
        }
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isNumber()) {
                return primitive.getAsDouble();
            }
            if (primitive.isString()) {
                try {
                    return Double.parseDouble(primitive.getAsString());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return fallback;
    }
}
