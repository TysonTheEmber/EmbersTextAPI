package net.tysontheember.emberstextapi.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.StringDecomposer;

@Mixin(TranslatableContents.class)
public abstract class TranslatableContentsMixin {
    @Inject(method = "decomposeTemplate", at = @At("HEAD"))
    private void emberstextapi$initSharedStyle(CallbackInfo ci,
            @Share("emberstextapi$style") LocalRef<Style> sharedStyle) {
        sharedStyle.set(Style.EMPTY);
    }

    @WrapOperation(method = "decomposeTemplate", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private void emberstextapi$forwardMarkup(Consumer<FormattedText> consumer, Object value, Operation<Void> original,
            @Share("emberstextapi$style") LocalRef<Style> sharedStyle) {
        Style style = sharedStyle.get();
        if (!(value instanceof FormattedText formatted) || style == null) {
            original.call(consumer, value);
            return;
        }

        final Style[] lastStyle = { style };
        StringDecomposer.iterateFormatted(formatted, style, (index, glyphStyle, codePoint) -> {
            consumer.accept(FormattedText.of(new String(Character.toChars(codePoint)), glyphStyle));
            lastStyle[0] = glyphStyle;
            return true;
        });
        sharedStyle.set(lastStyle[0]);
    }
}
