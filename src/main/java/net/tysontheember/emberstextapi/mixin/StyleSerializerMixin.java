package net.tysontheember.emberstextapi.mixin;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.core.style.EmbersStyle;
import net.tysontheember.emberstextapi.core.style.SpanEffectState;
import net.tysontheember.emberstextapi.core.style.SpanEffectStateCodec;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Extends vanilla style serialisation with Ember's span metadata payloads.
 */
@Mixin(Style.Serializer.class)
public class StyleSerializerMixin {
    @ModifyReturnValue(method = "serialize(Lnet/minecraft/network/chat/Style;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;", at = @At("RETURN"))
    private JsonElement emberstextapi$encodeSpanData(JsonElement original, Style style, Type type, JsonSerializationContext context) {
        if (style == null || original == null || !original.isJsonObject()) {
            return original;
        }
        SpanEffectState state = ((EmbersStyle) (Object) style).emberstextapi$getSpanEffectState();
        if (state == null || state.isEmpty()) {
            return original;
        }
        JsonObject object = original.getAsJsonObject();
        JsonElement payload = SpanEffectStateCodec.toJson(state);
        if (payload == null || payload.isJsonNull()) {
            return original;
        }
        object.add(SpanEffectStateCodec.JSON_KEY, payload);
        return object;
    }

    @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private void emberstextapi$decodeSpanData(JsonElement json, Type type, JsonDeserializationContext context, CallbackInfoReturnable<Style> cir) {
        if (json == null || !json.isJsonObject()) {
            return;
        }
        JsonObject object = json.getAsJsonObject();
        JsonElement payload = object.get(SpanEffectStateCodec.JSON_KEY);
        if (payload == null) {
            return;
        }
        SpanEffectState state = SpanEffectStateCodec.fromJson(payload);
        if (state == null) {
            return;
        }
        Style style = cir.getReturnValue();
        if (style != null) {
            ((EmbersStyle) (Object) style).emberstextapi$setSpanEffectState(state);
        }
    }
}
