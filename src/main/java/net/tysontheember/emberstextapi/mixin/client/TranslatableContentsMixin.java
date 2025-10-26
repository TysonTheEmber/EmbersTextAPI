package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.tysontheember.emberstextapi.client.text.MarkupAdapter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TranslatableContents.class)
public abstract class TranslatableContentsMixin {
    @Redirect(method = "decomposeTemplate(Ljava/lang/String;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/FormattedText;of(Ljava/lang/String;)Lnet/minecraft/network/chat/FormattedText;"))
    private FormattedText emberstextapi$wrapLiteral(String literal) {
        return MarkupAdapter.toFormattedText(literal, this);
    }
}
