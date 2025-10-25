package net.tysontheember.emberstextapi.mixin.client;

import com.google.gson.JsonElement;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Style.Serializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Serializer.class)
public abstract class StyleSerializerMixin {
    @Inject(method = "fromJson", at = @At("RETURN"))
    private static void emberstextapi$afterFromJson(JsonElement json, CallbackInfoReturnable<Style> cir) {
        // TODO: populate EmbersTextAPI extras when serialization is implemented.
    }

    @Inject(method = "toJson", at = @At("RETURN"))
    private static void emberstextapi$afterToJson(Style style, CallbackInfoReturnable<JsonElement> cir) {
        // TODO: write EmbersTextAPI extras when serialization is implemented.
    }
}
