package net.tysontheember.emberstextapi.mixin.common;

import net.minecraft.network.chat.Style;
import net.minecraft.util.StringDecomposer;
import net.tysontheember.emberstextapi.client.text.GlobalTextConfig;
import net.tysontheember.emberstextapi.client.text.MarkupAdapter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringDecomposer.class)
public abstract class StringDecomposerMixin {
    @Unique
    private static final ThreadLocal<Boolean> emberstextapi$parsingMarkup = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Inject(method = "iterateFormatted(Ljava/lang/String;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/StringDecomposer$FormattedCharSink;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void emberstextapi$injectMarkup(String text, Style style, StringDecomposer.FormattedCharSink sink,
            CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(emberstextapi$parsingMarkup.get())) {
            return;
        }
        if (!GlobalTextConfig.isMarkupEnabled() || !MarkupAdapter.hasMarkup(text)) {
            return;
        }
        emberstextapi$parsingMarkup.set(Boolean.TRUE);
        try {
            boolean handled = MarkupAdapter.visitFormatted(text, style, sink);
            cir.setReturnValue(handled);
        } finally {
            emberstextapi$parsingMarkup.set(Boolean.FALSE);
        }
    }
}
