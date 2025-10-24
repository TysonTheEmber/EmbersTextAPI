package net.tysontheember.emberstextapi.mixin.client;

import java.util.Optional;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.tysontheember.emberstextapi.client.text.GlobalTextConfig;
import net.tysontheember.emberstextapi.client.text.MarkupAdapter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiteralContents.class)
public abstract class LiteralContentsMixin {
    @Shadow
    @Final
    private String text;

    @Inject(method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
            at = @At("HEAD"), cancellable = true)
    private <T> void emberstextapi$visitMarkup(FormattedText.StyledContentConsumer<T> consumer, Style style,
            CallbackInfoReturnable<Optional<T>> cir) {
        if (!GlobalTextConfig.isMarkupEnabled() || !MarkupAdapter.hasMarkup(this.text)) {
            return;
        }
        Optional<T> result = MarkupAdapter.visitLiteral(this.text, style, consumer);
        cir.setReturnValue(result);
    }
}
