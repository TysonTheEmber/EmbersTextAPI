package net.tysontheember.emberstextapi.mixin.client;

import java.util.Optional;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.tysontheember.emberstextapi.client.text.ETAStyleOps;
import net.tysontheember.emberstextapi.client.text.MarkupAdapter;
import net.tysontheember.emberstextapi.client.text.SpanGraph;
import net.tysontheember.emberstextapi.client.text.SpanStyleExtras;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LiteralContents.class)
public class LiteralContentsMixin {
    @Shadow
    @Final
    @Mutable
    private String text;

    @Unique
    private SpanGraph eta$graph;

    @Unique
    private String eta$signature;

    @Inject(method = "<init>(Ljava/lang/String;)V", at = @At("RETURN"))
    private void emberstextapi$parseLiteral(String input, CallbackInfo ci) {
        MarkupAdapter.ParseResult result = MarkupAdapter.parse(this.text);
        this.text = result.sanitized;
        this.eta$graph = result.graph;
        this.eta$signature = result.signature;
    }

    @Inject(method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;", at = @At("HEAD"))
    private void emberstextapi$attachGraph(FormattedText.StyledContentConsumer<?> consumer, Style style, CallbackInfoReturnable<Optional<?>> cir) {
        if (style == null) {
            return;
        }

        if (style instanceof SpanStyleExtras extras) {
            extras.eta$setSpanSignature(this.eta$signature);
        }
        ETAStyleOps.withGraph(style, this.eta$graph);
    }
}
