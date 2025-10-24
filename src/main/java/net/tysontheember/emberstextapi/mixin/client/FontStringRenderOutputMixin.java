package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.client.text.RenderFastPath;
import net.tysontheember.emberstextapi.client.text.SpanEffectRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Applies minimal color-only tinting before vanilla glyph rendering runs.
 */
@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public abstract class FontStringRenderOutputMixin {
    @ModifyVariable(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At("HEAD"), argsOnly = true)
    private Style emberstextapi$tintStyle(Style style, int index, int codePoint) {
        if (RenderFastPath.shouldBypass(style)) {
            return style;
        }

        int tint = SpanEffectRegistry.applyTint(style, index, codePoint);
        if (tint != -1) {
            TextColor color = TextColor.fromRgb(tint & 0x00FFFFFF);
            if (color != null) {
                style = style.withColor(color);
            }
        }

        return style;
    }
}
