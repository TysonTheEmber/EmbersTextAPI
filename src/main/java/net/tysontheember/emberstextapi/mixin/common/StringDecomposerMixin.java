package net.tysontheember.emberstextapi.mixin.common;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import net.tysontheember.emberstextapi.client.text.GlobalTextConfig;
import net.tysontheember.emberstextapi.client.text.MarkupAdapter;
import net.tysontheember.emberstextapi.client.text.TypewriterGate;
import net.tysontheember.emberstextapi.client.text.options.ETAOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringDecomposer.class)
public abstract class StringDecomposerMixin {
    @Unique
    private static final ThreadLocal<Boolean> emberstextapi$parsingMarkup = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Inject(method = "iterateFormatted(Ljava/lang/String;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void emberstextapi$injectMarkup(String text, Style style, FormattedCharSink sink,
            CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(emberstextapi$parsingMarkup.get())) {
            return;
        }
        boolean markupEnabled = GlobalTextConfig.isMarkupEnabled();
        boolean hasMarkup = markupEnabled && MarkupAdapter.hasMarkup(text);
        boolean gatingEnabled = GlobalTextConfig.isTypewriterGatingEnabled();

        if (!hasMarkup && !gatingEnabled) {
            return;
        }

        ETAOptions.Snapshot options = GlobalTextConfig.getOptions();
        TypewriterGate gateContext = gatingEnabled ? new TypewriterGate(options) : null;
        FormattedCharSink effectiveSink = gateContext == null ? sink
                : (index, styled, codePoint) -> {
                    if (!gateContext.allow(styled)) {
                        return false;
                    }
                    return sink.accept(index, styled, codePoint);
                };

        emberstextapi$parsingMarkup.set(Boolean.TRUE);
        try {
            boolean handled;
            if (hasMarkup) {
                handled = MarkupAdapter.visitFormatted(text, style, effectiveSink);
            } else {
                handled = StringDecomposer.iterateFormatted(text, style, effectiveSink);
            }
            cir.setReturnValue(handled);
        } finally {
            emberstextapi$parsingMarkup.set(Boolean.FALSE);
        }
    }

}
