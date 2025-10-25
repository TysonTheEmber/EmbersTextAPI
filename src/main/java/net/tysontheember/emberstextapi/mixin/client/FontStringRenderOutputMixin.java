package net.tysontheember.emberstextapi.mixin.client;

import java.util.Optional;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.client.text.ETAStyleOps;
import net.tysontheember.emberstextapi.client.text.GradientColorer;
import net.tysontheember.emberstextapi.client.text.SpanEffectRegistry;
import net.tysontheember.emberstextapi.client.text.SpanGraph;
import net.tysontheember.emberstextapi.client.text.SpanNode;
import net.tysontheember.emberstextapi.client.text.SpanStyleExtras;
import net.tysontheember.emberstextapi.client.text.TypewriterGate;
import net.tysontheember.emberstextapi.client.text.TypewriterTrack;
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

    @Unique
    private boolean eta$skipGlyph;

    @Shadow
    private float dimFactor;

    @Inject(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At("HEAD"))
    private void emberstextapi$computeGrad(int logicalIndex, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        eta$overrideColor = Integer.MIN_VALUE;
        eta$overrideRed = 0.0F;
        eta$overrideGreen = 0.0F;
        eta$overrideBlue = 0.0F;
        eta$skipGlyph = false;
        SpanGraph graph = ETAStyleOps.getGraph(style);
        if (graph == null) {
            return;
        }

        if (style instanceof SpanStyleExtras extras) {
            Optional<SpanNode> typewriter = SpanEffectRegistry.findTypewriter(graph, logicalIndex);
            if (typewriter.isPresent()) {
                SpanNode node = typewriter.get();
                String signature = extras.eta$getSpanSignature();
                if (signature == null && graph.getSignature() != null) {
                    signature = graph.getSignature();
                }
                if (signature == null) {
                    signature = "";
                }
                boolean wordMode = false;
                String mode = node.getParameter("mode");
                if (mode != null && mode.equalsIgnoreCase("word")) {
                    wordMode = node.getWordBoundaries().length > 0;
                }
                float speed = Math.max(0.01F, node.getFloat("speed", 30.0F));
                int targetLength = Math.max(0, node.getEnd() - node.getStart());
                int[] wordBoundaries = node.getWordBoundaries();
                if (wordMode) {
                    if (wordBoundaries.length == 0) {
                        wordMode = false;
                    } else {
                        targetLength = wordBoundaries.length;
                    }
                }
                TypewriterGate.SurfaceContext context = TypewriterGate.currentContext();
                String signatureKey = signature;
                if (context.surface() != TypewriterGate.Surface.TOOLTIP) {
                    signatureKey = signature + '@' + Integer.toHexString(System.identityHashCode(style));
                }
                TypewriterTrack track = TypewriterGate.getOrCreate(context.surface(), context.keyHint(), signatureKey, node, speed, wordMode, targetLength);
                extras.eta$setTypewriterTrack(track);
                int allowed = track.allowCount();
                extras.eta$setTypewriterIndex(allowed);
                if (!TypewriterGate.isEnabled()) {
                    track.setProgressComplete();
                }
                if (track.isWordMode()) {
                    if (allowed <= 0) {
                        eta$skipGlyph = true;
                    } else {
                        int index = Math.min(allowed - 1, wordBoundaries.length - 1);
                        int allowedEnd = wordBoundaries[index];
                        if (logicalIndex >= allowedEnd) {
                            eta$skipGlyph = true;
                        }
                    }
                } else {
                    int relativeIndex = logicalIndex - node.getStart();
                    if (relativeIndex < 0 || relativeIndex >= allowed) {
                        eta$skipGlyph = true;
                    }
                }
                if (eta$skipGlyph) {
                    cir.setReturnValue(true);
                    cir.cancel();
                    return;
                }
            }
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
        eta$skipGlyph = false;
    }
}
