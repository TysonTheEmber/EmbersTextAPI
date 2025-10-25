package net.tysontheember.emberstextapi.mixin.client;

import java.util.Optional;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.client.text.ETAStyleOps;
import net.tysontheember.emberstextapi.client.text.GradientColorer;
import net.tysontheember.emberstextapi.client.text.SpanEffectRegistry;
import net.tysontheember.emberstextapi.client.text.SpanGraph;
import net.tysontheember.emberstextapi.client.text.SpanNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public abstract class FontStringRenderOutputMixin {
    @Unique
    private int eta$overrideColor = Integer.MIN_VALUE;

    @Inject(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At("HEAD"))
    private void emberstextapi$computeGrad(int logicalIndex, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        eta$overrideColor = Integer.MIN_VALUE;
        SpanGraph graph = ETAStyleOps.getGraph(style);
        if (graph == null) {
            return;
        }
        Optional<SpanNode> gradient = SpanEffectRegistry.findGradient(graph, logicalIndex);
        if (gradient.isEmpty()) {
            return;
        }
        SpanNode node = gradient.get();
        if (!node.hasParameter("from") || !node.hasParameter("to")) {
            return;
        }
        int fromRgb = node.getColor("from", -1);
        int toRgb = node.getColor("to", -1);
        if (fromRgb < 0 || toRgb < 0) {
            return;
        }
        float t = GradientColorer.tFor(node, logicalIndex);
        boolean hsv = node.getBoolean("hsv", false);
        eta$overrideColor = GradientColorer.sample(node, fromRgb, toRgb, hsv, logicalIndex, t);
    }

    @ModifyVariable(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At(value = "STORE"), ordinal = 0)
    private int emberstextapi$overrideBaseColor(int color) {
        if (eta$overrideColor != Integer.MIN_VALUE) {
            int alpha = color & 0xFF000000;
            if (alpha == 0) {
                alpha = 0xFF000000;
            }
            return alpha | (eta$overrideColor & 0xFFFFFF);
        }
        return color;
    }

    @Redirect(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/TextColor;getValue()I"))
    private int emberstextapi$overrideTextColor(TextColor color) {
        if (eta$overrideColor != Integer.MIN_VALUE) {
            return eta$overrideColor;
        }
        return color.getValue();
    }

    @Inject(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At("TAIL"))
    private void emberstextapi$clearGrad(int logicalIndex, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        eta$overrideColor = Integer.MIN_VALUE;
    }
}
