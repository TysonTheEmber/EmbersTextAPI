package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.client.render.SplitterHook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(StringSplitter.class)
public abstract class StringSplitterMixin {
    @ModifyVariable(
        method = "splitLines(Lnet/minecraft/network/chat/FormattedText;ILnet/minecraft/network/chat/Style;Ljava/util/function/BiConsumer;)V",
        at = @At("HEAD"),
        argsOnly = true
    )
    private FormattedText emberstextapi$normaliseMarkup(FormattedText value, int width, Style baseStyle, java.util.function.BiConsumer<FormattedText, Boolean> output) {
        return SplitterHook.preprocess(value, baseStyle, width);
    }
}
