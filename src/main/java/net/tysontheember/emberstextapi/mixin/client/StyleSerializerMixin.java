package net.tysontheember.emberstextapi.mixin.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import java.lang.reflect.Type;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Style.Serializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Serializer.class)
public abstract class StyleSerializerMixin {
    @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private void emberstextapi$afterDeserialize(JsonElement json, Type type, JsonDeserializationContext context, CallbackInfoReturnable<Style> cir) {
        // TODO: populate EmbersTextAPI extras when serialization is implemented.
    }

    @Inject(method = "serialize(Lnet/minecraft/network/chat/Style;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;", at = @At("RETURN"))
    private void emberstextapi$afterSerialize(Style style, Type type, JsonSerializationContext context, CallbackInfoReturnable<JsonElement> cir) {
        // TODO: write EmbersTextAPI extras when serialization is implemented.
    }
}
