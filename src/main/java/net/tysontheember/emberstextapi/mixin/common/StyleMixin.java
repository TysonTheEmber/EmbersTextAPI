package net.tysontheember.emberstextapi.mixin.common;

import java.util.List;
import java.util.OptionalInt;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.client.text.ETAStyleOps;
import net.tysontheember.emberstextapi.client.text.SpanEffect;
import net.tysontheember.emberstextapi.client.text.TypewriterTrack;
import net.tysontheember.emberstextapi.duck.ETAStyle;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Style.class)
public abstract class StyleMixin implements ETAStyle {
    @Unique
    private List<SpanEffect> eta$effects = List.of();
    @Unique
    private @Nullable TypewriterTrack eta$typewriterTrack;
    @Unique
    private int eta$typewriterIndex;
    @Unique
    private float eta$neonIntensity;
    @Unique
    private float eta$wobbleAmplitude;
    @Unique
    private float eta$wobbleSpeed;
    @Unique
    private float eta$gradientFlow;

    @Override
    public List<SpanEffect> eta$getEffects() {
        return this.eta$effects;
    }

    @Override
    public void eta$setEffects(List<SpanEffect> effects) {
        this.eta$effects = effects == null || effects.isEmpty() ? List.of() : List.copyOf(effects);
    }

    @Override
    public @Nullable TypewriterTrack eta$getTrack() {
        return this.eta$typewriterTrack;
    }

    @Override
    public void eta$setTrack(@Nullable TypewriterTrack track) {
        this.eta$typewriterTrack = track;
    }

    @Override
    public int eta$getTypewriterIndex() {
        return this.eta$typewriterIndex;
    }

    @Override
    public void eta$setTypewriterIndex(int index) {
        this.eta$typewriterIndex = index;
    }

    @Override
    public float eta$getNeonIntensity() {
        return this.eta$neonIntensity;
    }

    @Override
    public void eta$setNeonIntensity(float intensity) {
        this.eta$neonIntensity = intensity;
    }

    @Override
    public float eta$getWobbleAmplitude() {
        return this.eta$wobbleAmplitude;
    }

    @Override
    public void eta$setWobbleAmplitude(float amplitude) {
        this.eta$wobbleAmplitude = amplitude;
    }

    @Override
    public float eta$getWobbleSpeed() {
        return this.eta$wobbleSpeed;
    }

    @Override
    public void eta$setWobbleSpeed(float speed) {
        this.eta$wobbleSpeed = speed;
    }

    @Override
    public float eta$getGradientFlow() {
        return this.eta$gradientFlow;
    }

    @Override
    public void eta$setGradientFlow(float flow) {
        this.eta$gradientFlow = flow;
    }

    @Inject(method = "withColor", at = @At("RETURN"), cancellable = true)
    private void emberstextapi$carryEtaColor(TextColor color, CallbackInfoReturnable<Style> cir) {
        cir.setReturnValue(ETAStyleOps.copyEtaPayload((Style) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "withBold", at = @At("RETURN"), cancellable = true)
    private void emberstextapi$carryEtaBold(Boolean bold, CallbackInfoReturnable<Style> cir) {
        cir.setReturnValue(ETAStyleOps.copyEtaPayload((Style) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "withItalic", at = @At("RETURN"), cancellable = true)
    private void emberstextapi$carryEtaItalic(Boolean italic, CallbackInfoReturnable<Style> cir) {
        cir.setReturnValue(ETAStyleOps.copyEtaPayload((Style) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "withUnderlined", at = @At("RETURN"), cancellable = true)
    private void emberstextapi$carryEtaUnderlined(Boolean underlined, CallbackInfoReturnable<Style> cir) {
        cir.setReturnValue(ETAStyleOps.copyEtaPayload((Style) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "withStrikethrough", at = @At("RETURN"), cancellable = true)
    private void emberstextapi$carryEtaStrikethrough(Boolean strikethrough, CallbackInfoReturnable<Style> cir) {
        cir.setReturnValue(ETAStyleOps.copyEtaPayload((Style) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "withObfuscated", at = @At("RETURN"), cancellable = true)
    private void emberstextapi$carryEtaObfuscated(Boolean obfuscated, CallbackInfoReturnable<Style> cir) {
        cir.setReturnValue(ETAStyleOps.copyEtaPayload((Style) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "withClickEvent", at = @At("RETURN"), cancellable = true)
    private void emberstextapi$carryEtaClick(@Nullable ClickEvent event, CallbackInfoReturnable<Style> cir) {
        cir.setReturnValue(ETAStyleOps.copyEtaPayload((Style) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "withHoverEvent", at = @At("RETURN"), cancellable = true)
    private void emberstextapi$carryEtaHover(@Nullable HoverEvent event, CallbackInfoReturnable<Style> cir) {
        cir.setReturnValue(ETAStyleOps.copyEtaPayload((Style) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "withInsertion", at = @At("RETURN"), cancellable = true)
    private void emberstextapi$carryEtaInsertion(@Nullable String insertion, CallbackInfoReturnable<Style> cir) {
        cir.setReturnValue(ETAStyleOps.copyEtaPayload((Style) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "withFont", at = @At("RETURN"), cancellable = true)
    private void emberstextapi$carryEtaFont(ResourceLocation font, CallbackInfoReturnable<Style> cir) {
        cir.setReturnValue(ETAStyleOps.copyEtaPayload((Style) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "withShadowColor", at = @At("RETURN"), cancellable = true)
    private void emberstextapi$carryEtaShadow(OptionalInt shadow, CallbackInfoReturnable<Style> cir) {
        cir.setReturnValue(ETAStyleOps.copyEtaPayload((Style) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "withBackgroundColor", at = @At("RETURN"), cancellable = true)
    private void emberstextapi$carryEtaBackground(@Nullable TextColor background, CallbackInfoReturnable<Style> cir) {
        cir.setReturnValue(ETAStyleOps.copyEtaPayload((Style) (Object) this, cir.getReturnValue()));
    }

    @Inject(method = "applyTo", at = @At("RETURN"), cancellable = true)
    private void emberstextapi$mergeEta(Style other, CallbackInfoReturnable<Style> cir) {
        Style result = cir.getReturnValue();
        if (result != null) {
            cir.setReturnValue(ETAStyleOps.merge((Style) (Object) this, other, result));
        }
    }
}
