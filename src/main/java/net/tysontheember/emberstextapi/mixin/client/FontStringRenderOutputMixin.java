package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.client.text.RenderFastPath;
import net.tysontheember.emberstextapi.client.text.SpanEffectRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies minimal color-only tinting before vanilla glyph rendering runs.
 */
@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public abstract class FontStringRenderOutputMixin {
    @Unique
    private static final ThreadLocal<GlyphContext> EMBERSTEXTAPI$GLYPH_CONTEXT = ThreadLocal.withInitial(GlyphContext::new);

    @Inject(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At("HEAD"))
    private void emberstextapi$captureArgs(int lineIndex, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        GlyphContext context = EMBERSTEXTAPI$GLYPH_CONTEXT.get();
        context.index = lineIndex;
        context.codePoint = codePoint;
    }

    @Inject(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At("TAIL"))
    private void emberstextapi$clearArgs(int lineIndex, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        GlyphContext context = EMBERSTEXTAPI$GLYPH_CONTEXT.get();
        context.index = 0;
        context.codePoint = 0;
    }

    @ModifyVariable(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At("HEAD"), argsOnly = true)
    private Style emberstextapi$tintStyle(Style style) {
        if (RenderFastPath.shouldBypass(style)) {
            return style;
        }

        GlyphContext context = EMBERSTEXTAPI$GLYPH_CONTEXT.get();
        int tint = SpanEffectRegistry.applyTint(style, context.index, context.codePoint);
        if (tint != -1) {
            TextColor color = TextColor.fromRgb(tint & 0x00FFFFFF);
            if (color != null) {
                style = style.withColor(color);
            }
        }

        return style;
    }

    @Unique
    private static final class GlyphContext {
        private int index;
        private int codePoint;
    }
}
