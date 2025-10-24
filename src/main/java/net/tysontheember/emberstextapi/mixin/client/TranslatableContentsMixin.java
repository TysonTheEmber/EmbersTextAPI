package net.tysontheember.emberstextapi.mixin.client;

import java.util.Optional;

import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.tysontheember.emberstextapi.client.text.GlobalTextConfig;
import net.tysontheember.emberstextapi.client.text.MarkupAdapter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TranslatableContents.class)
public abstract class TranslatableContentsMixin {
    @Redirect(method = "visit(Lnet/minecraft/network/chat/ComponentContents$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/ComponentContents$StyledContentConsumer;accept(Lnet/minecraft/network/chat/Style;Ljava/lang/String;)Ljava/util/Optional;"))
    private <T> Optional<T> emberstextapi$redirectLiteral(ComponentContents.StyledContentConsumer<T> consumer, Style style,
            String literal) {
        if (!GlobalTextConfig.isMarkupEnabled() || !MarkupAdapter.hasMarkup(literal)) {
            return consumer.accept(style, literal);
        }
        return MarkupAdapter.visitLiteral(literal, style, consumer);
    }
}
