package net.tysontheember.emberstextapi.mixin.client;

import java.util.function.Consumer;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.FormattedText;
import net.tysontheember.emberstextapi.client.text.ETAStyleOps;
import net.tysontheember.emberstextapi.client.text.MarkupAdapter;
import net.tysontheember.emberstextapi.client.text.SpanGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TranslatableContents.class)
public class TranslatableContentsMixin {
    @Unique
    private SpanGraph eta$graph;

    @Unique
    private MarkupAdapter.ParseResult eta$pendingParse;

    @Inject(method = "decomposeTemplate(Ljava/lang/String;Ljava/util/function/Consumer;)Z", at = @At("HEAD"))
    private void emberstextapi$prepareTemplate(String template, Consumer<FormattedText> consumer, CallbackInfoReturnable<Boolean> cir) {
        this.eta$pendingParse = MarkupAdapter.parse(template);
        this.eta$graph = this.eta$pendingParse.graph;
    }

    @ModifyVariable(method = "decomposeTemplate(Ljava/lang/String;Ljava/util/function/Consumer;)Z", at = @At("HEAD"), argsOnly = true)
    private String emberstextapi$sanitizeTemplate(String value) {
        if (this.eta$pendingParse != null) {
            return this.eta$pendingParse.sanitized;
        }
        return value;
    }

    @ModifyVariable(method = "decomposeTemplate(Ljava/lang/String;Ljava/util/function/Consumer;)Z", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private Consumer<FormattedText> emberstextapi$wrapConsumer(Consumer<FormattedText> original) {
        if (original == null || this.eta$pendingParse == null || this.eta$pendingParse.graph == null) {
            return original;
        }
        SpanGraph graph = this.eta$pendingParse.graph;
        return formatted -> {
            if (formatted instanceof MutableComponent component) {
                Style style = component.getStyle();
                SpanGraph child = ETAStyleOps.getGraph(style);
                ETAStyleOps.withGraphMerged(style, graph, child);
            }
            original.accept(formatted);
        };
    }

    @Inject(method = "decomposeTemplate(Ljava/lang/String;Ljava/util/function/Consumer;)Z", at = @At("RETURN"))
    private void emberstextapi$clearPending(String template, Consumer<FormattedText> consumer, CallbackInfoReturnable<Boolean> cir) {
        this.eta$pendingParse = null;
    }

    @Inject(method = "visit(Lnet/minecraft/util/FormattedCharSink;Lnet/minecraft/network/chat/Style;)Z", at = @At("HEAD"))
    private void emberstextapi$attachGraph(FormattedCharSink sink, Style style, CallbackInfoReturnable<Boolean> cir) {
        if (style != null && this.eta$graph != null) {
            SpanGraph child = ETAStyleOps.getGraph(style);
            ETAStyleOps.withGraphMerged(style, this.eta$graph, child);
        }
    }
}
