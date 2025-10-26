package net.tysontheember.emberstextapi.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.core.markup.GlobalSpanProcessor;
import net.tysontheember.emberstextapi.core.style.EmbersStyle;
import net.tysontheember.emberstextapi.core.style.SpanEffectState;

@Mixin(StringSplitter.class)
public class StringSplitterMixin {
    @WrapOperation(method = "splitLines(Lnet/minecraft/network/chat/FormattedText;ILnet/minecraft/network/chat/Style;Ljava/util/function/BiConsumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/FormattedText;visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;"))
    private Optional<Object> emberstextapi$visitFormattedText(FormattedText formattedText,
            FormattedText.StyledContentConsumer<Object> consumer, Style style, Operation<Optional<Object>> original) {
        return formattedText.visit((glyphStyle, string) -> {
            if (string.isEmpty()) {
                return Optional.empty();
            }
            SpanEffectState state = glyphStyle instanceof EmbersStyle embers ? embers.emberstextapi$getSpanEffectState() : null;
            boolean needsGlyphSplit = state != null && state.typewriter() != null;
            if (!needsGlyphSplit) {
                return consumer.accept(glyphStyle, string);
            }
            GlobalSpanProcessor.iterateFormatted(string, 0, glyphStyle, glyphStyle, (index, splitStyle, codePoint) -> {
                consumer.accept(splitStyle, new String(Character.toChars(codePoint)));
                return true;
            });
            return Optional.empty();
        }, style);
    }
}
