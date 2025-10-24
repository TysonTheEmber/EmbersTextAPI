package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import net.tysontheember.emberstextapi.client.hook.SpanAwareFontHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Font.class)
public abstract class FontMixin {
    @ModifyVariable(
        method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;IIZ)F",
        at = @At("HEAD"),
        argsOnly = true
    )
    private FormattedCharSequence emberstextapi$swapSpanifiedSequence(FormattedCharSequence original) {
        return SpanAwareFontHook.maybeSpanify(original);
    }
}
