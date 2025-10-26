package net.tysontheember.emberstextapi.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.util.FormattedCharSink;
import net.tysontheember.emberstextapi.core.markup.GlobalSpanProcessor;
import net.tysontheember.emberstextapi.core.style.EmbersStyle;
import net.tysontheember.emberstextapi.core.style.SpanEffectState;

@Mixin(LiteralContents.class)
public abstract class LiteralContentsMixin {
    @Shadow
    @Final
    private String text;

    @Inject(method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
            at = @At("HEAD"), cancellable = true)
    private <T> void emberstextapi$visit(FormattedText.StyledContentConsumer<T> consumer, Style style,
            CallbackInfoReturnable<Optional<T>> cir) {
        if (!emberstextapi$needsProcessing(style, text)) {
            return;
        }

        final Optional<T>[] resultHolder = new Optional[] { Optional.empty() };
        FormattedCharSink sink = (index, glyphStyle, codePoint) -> {
            Optional<T> value = consumer.accept(glyphStyle, new String(Character.toChars(codePoint)));
            if (value.isPresent()) {
                resultHolder[0] = value;
                return false;
            }
            return true;
        };
        GlobalSpanProcessor.iterateFormatted(text, 0, style, style, sink);
        cir.setReturnValue(resultHolder[0]);
    }

    private static boolean emberstextapi$needsProcessing(Style style, String content) {
        if (content != null && content.indexOf('<') >= 0 && content.indexOf('>') > content.indexOf('<')) {
            return true;
        }
        if (style instanceof EmbersStyle embers) {
            SpanEffectState state = embers.emberstextapi$getSpanEffectState();
            return state != null && !state.isEmpty();
        }
        return false;
    }
}
