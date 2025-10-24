package net.tysontheember.emberstextapi.mixin.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

    @Inject(method = "serialize", at = @At("RETURN"), cancellable = true)
    private void emberstextapi$serializeEta(Style style, CallbackInfoReturnable<JsonElement> cir) {
        if (!ETAStyleOps.hasEtaPayload(style)) {
            return;
        }

        JsonElement element = cir.getReturnValue();
        JsonObject object = element instanceof JsonObject jsonObject ? jsonObject : new JsonObject();
        if (element == null || element.isJsonNull()) {
            object = new JsonObject();
        }

        ETAStyle duck = (ETAStyle) style;
        List<SpanEffect> effects = duck.eta$getEffects();
        if (!effects.isEmpty()) {
            object.add(EFFECTS_KEY, serializeEffects(effects));
        }

        TypewriterTrack track = duck.eta$getTrack();
        if (track != null && track.isActive()) {
            object.add(TYPEWRITER_KEY, serializeTrack(track));
        }

        if (duck.eta$getTypewriterIndex() != 0) {
            object.addProperty(TYPEWRITER_INDEX_KEY, duck.eta$getTypewriterIndex());
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

    @Inject(method = "deserialize", at = @At("RETURN"), cancellable = true)
    private void emberstextapi$deserializeEta(@Nullable JsonElement element, CallbackInfoReturnable<Style> cir) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        JsonObject object = GsonHelper.convertToJsonObject(element, "style");
        if (!object.has(EFFECTS_KEY) && !object.has(TYPEWRITER_KEY) && !object.has(TYPEWRITER_INDEX_KEY)
                && !object.has(NEON_INTENSITY_KEY) && !object.has(WOBBLE_AMPLITUDE_KEY)
                && !object.has(WOBBLE_SPEED_KEY) && !object.has(GRADIENT_FLOW_KEY)) {
            return;
        }

        Style style = ETAStyleOps.ensureStandalone(cir.getReturnValue());
        ETAStyle duck = (ETAStyle) style;

        if (object.has(EFFECTS_KEY)) {
            List<SpanEffect> effects = deserializeEffects(GsonHelper.getAsJsonArray(object, EFFECTS_KEY));
            duck.eta$setEffects(effects);
        } else {
            duck.eta$setEffects(List.of());
        }

        if (object.has(TYPEWRITER_KEY)) {
            TypewriterTrack track = deserializeTrack(GsonHelper.getAsJsonObject(object, TYPEWRITER_KEY));
            duck.eta$setTrack(track);
        } else {
            duck.eta$setTrack(null);
        }

        duck.eta$setTypewriterIndex(GsonHelper.getAsInt(object, TYPEWRITER_INDEX_KEY, duck.eta$getTypewriterIndex()));
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
