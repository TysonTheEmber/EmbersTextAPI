package net.tysontheember.emberstextapi.mixin.client;

import java.util.Optional;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.client.text.ETAStyleOps;
import net.tysontheember.emberstextapi.client.text.GradientColorer;
import net.tysontheember.emberstextapi.client.text.SpanEffectRegistry;
import net.tysontheember.emberstextapi.client.text.SpanGraph;
import net.tysontheember.emberstextapi.client.text.SpanNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput")
public abstract class FontStringRenderOutputMixin {
    @Unique
    private int eta$overrideColor = Integer.MIN_VALUE;

    @Unique
    private float eta$overrideRed;

    @Unique
    private float eta$overrideGreen;

    @Unique
    private float eta$overrideBlue;

    @Shadow
    private float dimFactor;

    @Inject(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At("HEAD"))
    private void emberstextapi$computeGrad(int logicalIndex, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        eta$overrideColor = Integer.MIN_VALUE;
        eta$overrideRed = 0.0F;
        eta$overrideGreen = 0.0F;
        eta$overrideBlue = 0.0F;
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
        int color = GradientColorer.sample(node, fromRgb, toRgb, hsv, logicalIndex, t);
        eta$overrideColor = color;
        float factor = this.dimFactor;
        eta$overrideRed = ((color >> 16) & 0xFF) / 255.0F * factor;
        eta$overrideGreen = ((color >> 8) & 0xFF) / 255.0F * factor;
        eta$overrideBlue = (color & 0xFF) / 255.0F * factor;
    }

    @ModifyVariable(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At(value = "STORE"), index = 10)
    private float emberstextapi$overrideRed(float original) {
        if (eta$overrideColor != Integer.MIN_VALUE) {
            return eta$overrideRed;
        }
        return original;
    }

    @ModifyVariable(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At(value = "STORE"), index = 11)
    private float emberstextapi$overrideGreen(float original) {
        if (eta$overrideColor != Integer.MIN_VALUE) {
            return eta$overrideGreen;
        }
        return original;
    }

    @ModifyVariable(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At(value = "STORE"), index = 12)
    private float emberstextapi$overrideBlue(float original) {
        if (eta$overrideColor != Integer.MIN_VALUE) {
            return eta$overrideBlue;
        }
        return original;
    }

    @Inject(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At("TAIL"))
    private void emberstextapi$clearGrad(int logicalIndex, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        eta$overrideColor = Integer.MIN_VALUE;
        eta$overrideRed = 0.0F;
        eta$overrideGreen = 0.0F;
        eta$overrideBlue = 0.0F;
    }
}
