package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import net.tysontheember.emberstextapi.client.GlobalSwitches;
import net.tysontheember.emberstextapi.client.text.SpanDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringDecomposer.class)
public abstract class StringDecomposerMixin {
    @Inject(method = "iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z", at = @At("HEAD"), cancellable = true)
    private static void emberstextapi$prepareFormatted(String text, int startIndex, Style baseStyle, Style fallbackStyle, FormattedCharSink sink, CallbackInfoReturnable<Boolean> cir) {
        if (!GlobalSwitches.enabled() || !SpanDispatcher.hasSpans(baseStyle)) {
            return;
        }
        SpanDispatcher.styleForCodePoint(baseStyle, 0, 0);
    }

    @Inject(method = "iterateFormatted(Lnet/minecraft/network/chat/FormattedText;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z", at = @At("HEAD"), cancellable = true)
    private static void emberstextapi$prepareFormattedText(FormattedText text, Style baseStyle, FormattedCharSink sink, CallbackInfoReturnable<Boolean> cir) {
        if (!GlobalSwitches.enabled() || !SpanDispatcher.hasSpans(baseStyle)) {
            return;
        }
        SpanDispatcher.styleForCodePoint(baseStyle, 0, 0);
    }

    @Inject(method = "iterateBackwards(Ljava/lang/String;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z", at = @At("HEAD"), cancellable = true)
    private static void emberstextapi$prepareBackwards(String text, Style baseStyle, FormattedCharSink sink, CallbackInfoReturnable<Boolean> cir) {
        if (!GlobalSwitches.enabled() || !SpanDispatcher.hasSpans(baseStyle)) {
            return;
        }
        SpanDispatcher.styleForCodePoint(baseStyle, 0, 0);
    }
}
