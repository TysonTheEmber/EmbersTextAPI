package net.tysontheember.emberstextapi.mixin.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.network.chat.Style;
import net.minecraft.util.GsonHelper;
import net.tysontheember.emberstextapi.client.text.ETAStyleOps;
import net.tysontheember.emberstextapi.client.text.SpanEffect;
import net.tysontheember.emberstextapi.client.text.TypewriterTrack;
import net.tysontheember.emberstextapi.duck.ETAStyle;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.Serializer.class)
public abstract class StyleSerializerMixin {
    private static final String EFFECTS_KEY = "eta$effects";
    private static final String TYPEWRITER_KEY = "eta$typewriter";
    private static final String TYPEWRITER_INDEX_KEY = "eta$typewriterIndex";
    private static final String NEON_INTENSITY_KEY = "eta$neonIntensity";
    private static final String WOBBLE_AMPLITUDE_KEY = "eta$wobbleAmplitude";
    private static final String WOBBLE_SPEED_KEY = "eta$wobbleSpeed";
    private static final String GRADIENT_FLOW_KEY = "eta$gradientFlow";

    @Inject(
            method =
                    "serialize(Lnet/minecraft/network/chat/Style;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;",
            at = @At("RETURN"),
            cancellable = true)
    private void emberstextapi$serializeEta(Style style, Type type, JsonSerializationContext context,
            CallbackInfoReturnable<JsonElement> cir) {
        if (!ETAStyleOps.hasEtaPayload(style)) {
            return;
        }

        JsonElement element = cir.getReturnValue();
        JsonObject object;
        if (element == null || element.isJsonNull()) {
            object = new JsonObject();
            cir.setReturnValue(object);
        } else if (element instanceof JsonObject jsonObject) {
            object = jsonObject;
        } else {
            return;
        }

        ETAStyle duck = (ETAStyle) style;
        List<SpanEffect> effects = duck.eta$getEffects();
        if (!effects.isEmpty()) {
            object.add(EFFECTS_KEY, serializeEffects(effects));
        }

        TypewriterTrack track = duck.eta$getTrack();
        int typewriterIndex = duck.eta$getTypewriterIndex();
        if (track != null && track.isActive()) {
            JsonObject trackJson = serializeTrack(track);
            if (typewriterIndex != 0) {
                trackJson.addProperty("index", typewriterIndex);
            }
            object.add(TYPEWRITER_KEY, trackJson);
        } else if (typewriterIndex != 0) {
            object.addProperty(TYPEWRITER_INDEX_KEY, typewriterIndex);
        }
        if (duck.eta$getNeonIntensity() != 0.0f) {
            object.addProperty(NEON_INTENSITY_KEY, duck.eta$getNeonIntensity());
        }
        if (duck.eta$getWobbleAmplitude() != 0.0f) {
            object.addProperty(WOBBLE_AMPLITUDE_KEY, duck.eta$getWobbleAmplitude());
        }
        if (duck.eta$getWobbleSpeed() != 0.0f) {
            object.addProperty(WOBBLE_SPEED_KEY, duck.eta$getWobbleSpeed());
        }
        if (duck.eta$getGradientFlow() != 0.0f) {
            object.addProperty(GRADIENT_FLOW_KEY, duck.eta$getGradientFlow());
        }

        cir.setReturnValue(object);
    }

    @Inject(
            method =
                    "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/network/chat/Style;",
            at = @At("RETURN"),
            cancellable = true)
    private void emberstextapi$deserializeEta(@Nullable JsonElement element, Type type,
            JsonDeserializationContext context, CallbackInfoReturnable<Style> cir) {
        if (element == null || element.isJsonNull() || !element.isJsonObject()) {
            return;
        }
        JsonObject object = element.getAsJsonObject();
        if (!object.has(EFFECTS_KEY) && !object.has(TYPEWRITER_KEY) && !object.has(TYPEWRITER_INDEX_KEY)
                && !object.has(NEON_INTENSITY_KEY) && !object.has(WOBBLE_AMPLITUDE_KEY)
                && !object.has(WOBBLE_SPEED_KEY) && !object.has(GRADIENT_FLOW_KEY)) {
            return;
        }

        Style style = ETAStyleOps.ensureStandalone(cir.getReturnValue());
        ETAStyle duck = (ETAStyle) style;
        duck.eta$setEffects(List.of());
        duck.eta$setTrack(null);
        duck.eta$setTypewriterIndex(0);
        duck.eta$setNeonIntensity(0.0f);
        duck.eta$setWobbleAmplitude(0.0f);
        duck.eta$setWobbleSpeed(0.0f);
        duck.eta$setGradientFlow(0.0f);

        if (object.has(EFFECTS_KEY) && object.get(EFFECTS_KEY).isJsonArray()) {
            List<SpanEffect> effects = deserializeEffects(object.getAsJsonArray(EFFECTS_KEY));
            duck.eta$setEffects(effects);
        }

        boolean indexHandled = false;
        if (object.has(TYPEWRITER_KEY)) {
            JsonElement trackElement = object.get(TYPEWRITER_KEY);
            if (trackElement.isJsonObject()) {
                JsonObject trackObject = trackElement.getAsJsonObject();
                TypewriterTrack track = deserializeTrack(trackObject);
                if (track != null && track.isActive()) {
                    duck.eta$setTrack(track);
                }
                if (trackObject.has("index")) {
                    duck.eta$setTypewriterIndex(GsonHelper.getAsInt(trackObject, "index", duck.eta$getTypewriterIndex()));
                    indexHandled = true;
                }
            }
        }

        if (!indexHandled) {
            duck.eta$setTypewriterIndex(
                    GsonHelper.getAsInt(object, TYPEWRITER_INDEX_KEY, duck.eta$getTypewriterIndex()));
        }
        duck.eta$setNeonIntensity(GsonHelper.getAsFloat(object, NEON_INTENSITY_KEY, duck.eta$getNeonIntensity()));
        duck.eta$setWobbleAmplitude(GsonHelper.getAsFloat(object, WOBBLE_AMPLITUDE_KEY, duck.eta$getWobbleAmplitude()));
        duck.eta$setWobbleSpeed(GsonHelper.getAsFloat(object, WOBBLE_SPEED_KEY, duck.eta$getWobbleSpeed()));
        duck.eta$setGradientFlow(GsonHelper.getAsFloat(object, GRADIENT_FLOW_KEY, duck.eta$getGradientFlow()));

        cir.setReturnValue(style);
    }

    @Unique
    private static JsonArray serializeEffects(List<SpanEffect> effects) {
        JsonArray array = new JsonArray();
        for (SpanEffect effect : effects) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", effect.id());
            if (effect.hasParameters()) {
                JsonObject params = new JsonObject();
                effect.parameters().forEach(params::addProperty);
                entry.add("params", params);
            }
            array.add(entry);
        }
        return array;
    }

    @Unique
    private static JsonObject serializeTrack(TypewriterTrack track) {
        JsonObject object = new JsonObject();
        object.addProperty("mode", track.mode().name().toLowerCase(Locale.ROOT));
        if (track.speedMultiplier() != 1.0f) {
            object.addProperty("speed", track.speedMultiplier());
        }
        if (track.trackId() != null && !track.trackId().isEmpty()) {
            object.addProperty("id", track.trackId());
        }
        return object;
    }

    @Unique
    private static List<SpanEffect> deserializeEffects(JsonArray array) {
        List<SpanEffect> effects = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject entry = element.getAsJsonObject();
            String id = GsonHelper.getAsString(entry, "id", null);
            if (id == null || id.isEmpty()) {
                continue;
            }
            Map<String, String> params = new HashMap<>();
            if (entry.has("params") && entry.get("params").isJsonObject()) {
                JsonObject paramObj = entry.getAsJsonObject("params");
                for (Map.Entry<String, JsonElement> parameter : paramObj.entrySet()) {
                    if (parameter.getValue().isJsonPrimitive()) {
                        params.put(parameter.getKey(), parameter.getValue().getAsString());
                    }
                }
            }
            effects.add(new SpanEffect(id, params));
        }
        return List.copyOf(effects);
    }

    @Unique
    private static TypewriterTrack deserializeTrack(JsonObject object) {
        String modeName = GsonHelper.getAsString(object, "mode", TypewriterTrack.Mode.OFF.name());
        TypewriterTrack.Mode mode;
        try {
            mode = TypewriterTrack.Mode.valueOf(modeName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            mode = TypewriterTrack.Mode.OFF;
        }
        float speed = GsonHelper.getAsFloat(object, "speed", 1.0f);
        String id = GsonHelper.getAsString(object, "id", null);
        TypewriterTrack track = new TypewriterTrack(mode, speed, id);
        return track.isActive() ? track : TypewriterTrack.OFF;
    }
}
