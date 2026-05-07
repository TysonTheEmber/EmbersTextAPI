package net.tysontheember.emberstextapi.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.accessor.ETAStyle;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.TypewriterTrack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(Style.class)
public class StyleMixin implements ETAStyle {

    @Unique
    private ImmutableList<Effect> emberstextapi$effects = ImmutableList.of();

    @Unique
    private String emberstextapi$itemId = null;

    @Unique
    private Integer emberstextapi$itemCount = null;

    @Unique
    private Float emberstextapi$itemOffsetX = null;

    @Unique
    private Float emberstextapi$itemOffsetY = null;

    @Unique
    private String emberstextapi$itemNbt = null;

    @Unique
    private String emberstextapi$entityId = null;

    @Unique
    private Float emberstextapi$entityScale = null;

    @Unique
    private Float emberstextapi$entityOffsetX = null;

    @Unique
    private Float emberstextapi$entityOffsetY = null;

    @Unique
    private Float emberstextapi$entityYaw = null;

    @Unique
    private Float emberstextapi$entityPitch = null;

    @Unique
    private Float emberstextapi$entityRoll = null;

    @Unique
    private Integer emberstextapi$entityLighting = null;

    @Unique
    private Float emberstextapi$entitySpin = null;

    @Unique
    private String emberstextapi$entityAnimation = null;

    @Unique
    private String emberstextapi$entityNbt = null;

    @Unique
    private TypewriterTrack emberstextapi$typewriterTrack = null;

    @Unique
    private int emberstextapi$typewriterIndex = -1;

    @Unique
    private Object emberstextapi$obfuscateKey = null;

    @Unique
    private Object emberstextapi$obfuscateStableKey = null;

    @Unique
    private int emberstextapi$obfuscateSpanStart = -1;

    @Unique
    private int emberstextapi$obfuscateSpanLength = -1;

    @Override
    public ImmutableList<Effect> emberstextapi$getEffects() {
        return emberstextapi$effects;
    }

    @Override
    public void emberstextapi$setEffects(ImmutableList<Effect> effects) {
        this.emberstextapi$effects = effects != null ? effects : ImmutableList.of();
    }

    @Override
    public synchronized void emberstextapi$addEffect(Effect effect) {
        if (effect == null) {
            return;
        }
        if (emberstextapi$effects.isEmpty()) {
            emberstextapi$effects = ImmutableList.of(effect);
        } else {
            emberstextapi$effects = ImmutableList.<Effect>builder()
                    .addAll(emberstextapi$effects)
                    .add(effect)
                    .build();
        }
    }

    @Override
    public String emberstextapi$getItemId() {
        return emberstextapi$itemId;
    }

    @Override
    public void emberstextapi$setItemId(String itemId) {
        this.emberstextapi$itemId = itemId;
    }

    @Override
    public Integer emberstextapi$getItemCount() {
        return emberstextapi$itemCount;
    }

    @Override
    public void emberstextapi$setItemCount(Integer count) {
        this.emberstextapi$itemCount = count;
    }

    @Override
    public Float emberstextapi$getItemOffsetX() {
        return emberstextapi$itemOffsetX;
    }

    @Override
    public void emberstextapi$setItemOffsetX(Float offsetX) {
        this.emberstextapi$itemOffsetX = offsetX;
    }

    @Override
    public Float emberstextapi$getItemOffsetY() {
        return emberstextapi$itemOffsetY;
    }

    @Override
    public void emberstextapi$setItemOffsetY(Float offsetY) {
        this.emberstextapi$itemOffsetY = offsetY;
    }

    @Override
    public String emberstextapi$getItemNbt() {
        return emberstextapi$itemNbt;
    }

    @Override
    public void emberstextapi$setItemNbt(String nbt) {
        this.emberstextapi$itemNbt = nbt;
    }

    @Override
    public String emberstextapi$getEntityId() {
        return emberstextapi$entityId;
    }

    @Override
    public void emberstextapi$setEntityId(String entityId) {
        this.emberstextapi$entityId = entityId;
    }

    @Override
    public Float emberstextapi$getEntityScale() {
        return emberstextapi$entityScale;
    }

    @Override
    public void emberstextapi$setEntityScale(Float scale) {
        this.emberstextapi$entityScale = scale;
    }

    @Override
    public Float emberstextapi$getEntityOffsetX() {
        return emberstextapi$entityOffsetX;
    }

    @Override
    public void emberstextapi$setEntityOffsetX(Float offsetX) {
        this.emberstextapi$entityOffsetX = offsetX;
    }

    @Override
    public Float emberstextapi$getEntityOffsetY() {
        return emberstextapi$entityOffsetY;
    }

    @Override
    public void emberstextapi$setEntityOffsetY(Float offsetY) {
        this.emberstextapi$entityOffsetY = offsetY;
    }

    @Override
    public Float emberstextapi$getEntityYaw() {
        return emberstextapi$entityYaw;
    }

    @Override
    public void emberstextapi$setEntityYaw(Float yaw) {
        this.emberstextapi$entityYaw = yaw;
    }

    @Override
    public Float emberstextapi$getEntityPitch() {
        return emberstextapi$entityPitch;
    }

    @Override
    public void emberstextapi$setEntityPitch(Float pitch) {
        this.emberstextapi$entityPitch = pitch;
    }

    @Override
    public Float emberstextapi$getEntityRoll() {
        return emberstextapi$entityRoll;
    }

    @Override
    public void emberstextapi$setEntityRoll(Float roll) {
        this.emberstextapi$entityRoll = roll;
    }

    @Override
    public Integer emberstextapi$getEntityLighting() {
        return emberstextapi$entityLighting;
    }

    @Override
    public void emberstextapi$setEntityLighting(Integer lighting) {
        this.emberstextapi$entityLighting = lighting;
    }

    @Override
    public Float emberstextapi$getEntitySpin() {
        return emberstextapi$entitySpin;
    }

    @Override
    public void emberstextapi$setEntitySpin(Float spin) {
        this.emberstextapi$entitySpin = spin;
    }

    @Override
    public String emberstextapi$getEntityAnimation() {
        return emberstextapi$entityAnimation;
    }

    @Override
    public void emberstextapi$setEntityAnimation(String animation) {
        this.emberstextapi$entityAnimation = animation;
    }

    @Override
    public String emberstextapi$getEntityNbt() {
        return emberstextapi$entityNbt;
    }

    @Override
    public void emberstextapi$setEntityNbt(String nbt) {
        this.emberstextapi$entityNbt = nbt;
    }

    @Override
    public TypewriterTrack emberstextapi$getTypewriterTrack() {
        return emberstextapi$typewriterTrack;
    }

    @Override
    public void emberstextapi$setTypewriterTrack(TypewriterTrack track) {
        this.emberstextapi$typewriterTrack = track;
    }

    @Override
    public int emberstextapi$getTypewriterIndex() {
        return emberstextapi$typewriterIndex;
    }

    @Override
    public void emberstextapi$setTypewriterIndex(int index) {
        this.emberstextapi$typewriterIndex = index;
    }

    @Override
    public Object emberstextapi$getObfuscateKey() {
        return emberstextapi$obfuscateKey;
    }

    @Override
    public void emberstextapi$setObfuscateKey(Object key) {
        this.emberstextapi$obfuscateKey = key;
    }

    @Override
    public Object emberstextapi$getObfuscateStableKey() {
        return emberstextapi$obfuscateStableKey;
    }

    @Override
    public void emberstextapi$setObfuscateStableKey(Object key) {
        this.emberstextapi$obfuscateStableKey = key;
    }

    @Override
    public int emberstextapi$getObfuscateSpanStart() {
        return emberstextapi$obfuscateSpanStart;
    }

    @Override
    public void emberstextapi$setObfuscateSpanStart(int start) {
        this.emberstextapi$obfuscateSpanStart = start;
    }

    @Override
    public int emberstextapi$getObfuscateSpanLength() {
        return emberstextapi$obfuscateSpanLength;
    }

    @Override
    public void emberstextapi$setObfuscateSpanLength(int length) {
        this.emberstextapi$obfuscateSpanLength = length;
    }

    @Inject(method = "withColor(Lnet/minecraft/network/chat/TextColor;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private void emberstextapi$propagateOnWithColor(CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateData(cir.getReturnValue());
    }

    @Inject(method = "withBold", at = @At("RETURN"))
    private void emberstextapi$propagateOnWithBold(CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateData(cir.getReturnValue());
    }

    @Inject(method = "withItalic", at = @At("RETURN"))
    private void emberstextapi$propagateOnWithItalic(CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateData(cir.getReturnValue());
    }

    @Inject(method = "withUnderlined", at = @At("RETURN"))
    private void emberstextapi$propagateOnWithUnderlined(CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateData(cir.getReturnValue());
    }

    @Inject(method = "withStrikethrough", at = @At("RETURN"))
    private void emberstextapi$propagateOnWithStrikethrough(CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateData(cir.getReturnValue());
    }

    @Inject(method = "withObfuscated", at = @At("RETURN"))
    private void emberstextapi$propagateOnWithObfuscated(CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateData(cir.getReturnValue());
    }

    @Inject(method = "withClickEvent", at = @At("RETURN"))
    private void emberstextapi$propagateOnWithClickEvent(CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateData(cir.getReturnValue());
    }

    @Inject(method = "withHoverEvent", at = @At("RETURN"))
    private void emberstextapi$propagateOnWithHoverEvent(CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateData(cir.getReturnValue());
    }

    @Inject(method = "withInsertion", at = @At("RETURN"))
    private void emberstextapi$propagateOnWithInsertion(CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateData(cir.getReturnValue());
    }

    @Inject(method = "withFont", at = @At("RETURN"))
    private void emberstextapi$propagateOnWithFont(CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateData(cir.getReturnValue());
    }

    @Inject(method = "applyFormat", at = @At("RETURN"))
    private void emberstextapi$propagateOnApplyFormat(CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateData(cir.getReturnValue());
    }

    @Inject(method = "applyLegacyFormat", at = @At("RETURN"))
    private void emberstextapi$propagateOnApplyLegacyFormat(CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateData(cir.getReturnValue());
    }

    @Inject(method = "applyFormats", at = @At("RETURN"))
    private void emberstextapi$propagateOnApplyFormats(CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateData(cir.getReturnValue());
    }

    @Inject(method = "applyTo", at = @At("RETURN"))
    private void emberstextapi$propagateOnApplyTo(Style that, CallbackInfoReturnable<Style> cir) {
        Style result = cir.getReturnValue();
        Style self = (Style) (Object) this;
        if (self == result || that == result) {
            return;
        }
        ETAStyle thatStyle = (ETAStyle) that;
        ETAStyle resultStyle = (ETAStyle) result;

        ImmutableList<Effect> effects = emberstextapi$effects.isEmpty()
                ? thatStyle.emberstextapi$getEffects()
                : emberstextapi$effects;
        resultStyle.emberstextapi$setEffects(effects);

        TypewriterTrack track = emberstextapi$typewriterTrack != null
                ? emberstextapi$typewriterTrack
                : thatStyle.emberstextapi$getTypewriterTrack();
        int index = emberstextapi$typewriterIndex >= 0
                ? emberstextapi$typewriterIndex
                : thatStyle.emberstextapi$getTypewriterIndex();
        Object obfKey = emberstextapi$obfuscateKey != null
                ? emberstextapi$obfuscateKey
                : thatStyle.emberstextapi$getObfuscateKey();
        int obfStart = emberstextapi$obfuscateSpanStart >= 0
                ? emberstextapi$obfuscateSpanStart
                : thatStyle.emberstextapi$getObfuscateSpanStart();
        int obfLen = emberstextapi$obfuscateSpanLength >= 0
                ? emberstextapi$obfuscateSpanLength
                : thatStyle.emberstextapi$getObfuscateSpanLength();
        Object obfStable = emberstextapi$obfuscateStableKey != null
                ? emberstextapi$obfuscateStableKey
                : thatStyle.emberstextapi$getObfuscateStableKey();

        resultStyle.emberstextapi$setTypewriterTrack(track);
        resultStyle.emberstextapi$setTypewriterIndex(index);
        resultStyle.emberstextapi$setObfuscateKey(obfKey);
        resultStyle.emberstextapi$setObfuscateStableKey(obfStable);
        resultStyle.emberstextapi$setObfuscateSpanStart(obfStart);
        resultStyle.emberstextapi$setObfuscateSpanLength(obfLen);
    }

    @Unique
    private void emberstextapi$propagateData(Style result) {
        Style self = (Style) (Object) this;
        if (self == result) {
            return;
        }

        if (emberstextapi$effects.isEmpty()
                && emberstextapi$typewriterTrack == null
                && emberstextapi$obfuscateKey == null
                && emberstextapi$obfuscateStableKey == null
                && emberstextapi$obfuscateSpanStart < 0
                && emberstextapi$obfuscateSpanLength < 0) {
            return;
        }
        ETAStyle resultStyle = (ETAStyle) result;
        resultStyle.emberstextapi$setEffects(emberstextapi$effects);
        if (emberstextapi$typewriterTrack != null) {
            resultStyle.emberstextapi$setTypewriterTrack(emberstextapi$typewriterTrack);
            resultStyle.emberstextapi$setTypewriterIndex(emberstextapi$typewriterIndex);
        }
        if (emberstextapi$obfuscateKey != null) {
            resultStyle.emberstextapi$setObfuscateKey(emberstextapi$obfuscateKey);
        }
        if (emberstextapi$obfuscateStableKey != null) {
            resultStyle.emberstextapi$setObfuscateStableKey(emberstextapi$obfuscateStableKey);
        }
        if (emberstextapi$obfuscateSpanStart >= 0) {
            resultStyle.emberstextapi$setObfuscateSpanStart(emberstextapi$obfuscateSpanStart);
        }
        if (emberstextapi$obfuscateSpanLength >= 0) {
            resultStyle.emberstextapi$setObfuscateSpanLength(emberstextapi$obfuscateSpanLength);
        }
    }

    @Inject(method = "hashCode", at = @At("RETURN"), cancellable = true)
    private void emberstextapi$hashCode(CallbackInfoReturnable<Integer> cir) {
        int h = cir.getReturnValue();
        h = 31 * h + emberstextapi$effects.hashCode();
        h = 31 * h + Objects.hashCode(emberstextapi$itemId);
        h = 31 * h + Objects.hashCode(emberstextapi$itemCount);
        h = 31 * h + Objects.hashCode(emberstextapi$itemOffsetX);
        h = 31 * h + Objects.hashCode(emberstextapi$itemOffsetY);
        h = 31 * h + Objects.hashCode(emberstextapi$entityId);
        h = 31 * h + Objects.hashCode(emberstextapi$entityScale);
        h = 31 * h + Objects.hashCode(emberstextapi$entityOffsetX);
        h = 31 * h + Objects.hashCode(emberstextapi$entityOffsetY);
        h = 31 * h + Objects.hashCode(emberstextapi$entityYaw);
        h = 31 * h + Objects.hashCode(emberstextapi$entityPitch);
        h = 31 * h + Objects.hashCode(emberstextapi$entityRoll);
        h = 31 * h + Objects.hashCode(emberstextapi$entityLighting);
        h = 31 * h + Objects.hashCode(emberstextapi$entitySpin);
        h = 31 * h + emberstextapi$typewriterIndex;
        cir.setReturnValue(h);
    }

    @Inject(method = "equals", at = @At("HEAD"), cancellable = true)
    private void emberstextapi$equals(Object obj, CallbackInfoReturnable<Boolean> cir) {

        if (this != obj && obj instanceof ETAStyle otherStyle) {

            if (!Objects.equals(this.emberstextapi$effects, otherStyle.emberstextapi$getEffects())) {
                cir.setReturnValue(false);
                return;
            }

            if (!Objects.equals(this.emberstextapi$itemId, otherStyle.emberstextapi$getItemId())) {
                cir.setReturnValue(false);
                return;
            }
            if (!Objects.equals(this.emberstextapi$itemCount, otherStyle.emberstextapi$getItemCount())) {
                cir.setReturnValue(false);
                return;
            }
            if (!Objects.equals(this.emberstextapi$itemOffsetX, otherStyle.emberstextapi$getItemOffsetX())) {
                cir.setReturnValue(false);
                return;
            }
            if (!Objects.equals(this.emberstextapi$itemOffsetY, otherStyle.emberstextapi$getItemOffsetY())) {
                cir.setReturnValue(false);
                return;
            }

            if (!Objects.equals(this.emberstextapi$entityId, otherStyle.emberstextapi$getEntityId())) {
                cir.setReturnValue(false);
                return;
            }
            if (!Objects.equals(this.emberstextapi$entityScale, otherStyle.emberstextapi$getEntityScale())) {
                cir.setReturnValue(false);
                return;
            }
            if (!Objects.equals(this.emberstextapi$entityOffsetX, otherStyle.emberstextapi$getEntityOffsetX())) {
                cir.setReturnValue(false);
                return;
            }
            if (!Objects.equals(this.emberstextapi$entityOffsetY, otherStyle.emberstextapi$getEntityOffsetY())) {
                cir.setReturnValue(false);
                return;
            }
            if (!Objects.equals(this.emberstextapi$entityYaw, otherStyle.emberstextapi$getEntityYaw())) {
                cir.setReturnValue(false);
                return;
            }
            if (!Objects.equals(this.emberstextapi$entityPitch, otherStyle.emberstextapi$getEntityPitch())) {
                cir.setReturnValue(false);
                return;
            }
            if (!Objects.equals(this.emberstextapi$entityRoll, otherStyle.emberstextapi$getEntityRoll())) {
                cir.setReturnValue(false);
                return;
            }
            if (!Objects.equals(this.emberstextapi$entityLighting, otherStyle.emberstextapi$getEntityLighting())) {
                cir.setReturnValue(false);
                return;
            }
            if (!Objects.equals(this.emberstextapi$entitySpin, otherStyle.emberstextapi$getEntitySpin())) {
                cir.setReturnValue(false);
                return;
            }

            if (this.emberstextapi$typewriterIndex != otherStyle.emberstextapi$getTypewriterIndex()) {
                cir.setReturnValue(false);
            }
        }
    }
}
