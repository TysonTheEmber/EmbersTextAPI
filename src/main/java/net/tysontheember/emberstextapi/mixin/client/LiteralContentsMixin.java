package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.util.FormattedCharSink;
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

@Mixin(LiteralContents.class)
public class LiteralContentsMixin {
    @Shadow
    @Final
    @Mutable
    private String text;

    @Unique
    private SpanGraph eta$graph;

    @Inject(method = "<init>(Ljava/lang/String;)V", at = @At("RETURN"))
    private void emberstextapi$parseLiteral(String input, CallbackInfo cir) {
        MarkupAdapter.ParseResult result = MarkupAdapter.parse(this.text);
        this.text = result.sanitized;
        this.eta$graph = result.graph;
    }

    @Inject(method = "visit(Lnet/minecraft/util/FormattedCharSink;Lnet/minecraft/network/chat/Style;)Z", at = @At("HEAD"))
    private void emberstextapi$attachGraph(FormattedCharSink sink, Style style, CallbackInfoReturnable<Boolean> cir) {
        if (this.eta$graph != null && style instanceof SpanStyleExtras) {
            ETAStyleOps.withGraph(style, this.eta$graph);
        }
    }
}
