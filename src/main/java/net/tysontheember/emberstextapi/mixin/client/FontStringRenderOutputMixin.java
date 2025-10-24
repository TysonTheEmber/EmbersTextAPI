package net.tysontheember.emberstextapi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Placeholder injection for Phase D1. Real glyph rendering logic will be supplied in Phase D2.
 */
@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public abstract class FontStringRenderOutputMixin {
    @Inject(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At("HEAD"))
    private void emberstextapi$prepareGlyph(int index, net.minecraft.network.chat.Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        // Phase D1 placeholder: no-op until effects are implemented.
    }
}
