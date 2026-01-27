package net.tysontheember.emberstextapi.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.duck.ETAStyle;
import net.tysontheember.emberstextapi.typewriter.TypewriterTrack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

/**
 * Mixin for augmenting Minecraft's Style class with Effect storage capabilities.
 * <p>
 * This mixin adds a field to store effects and implements the ETAStyle duck interface
 * to provide getter/setter methods. Effects are NOT automatically propagated to avoid
 * mutating cached/shared Style instances, which would cause effects to leak globally.
 * </p>
 * <p>
 * Effects should only be set explicitly by StyleUtil when creating new Style instances
 * for text with markup. This ensures effects remain isolated to the specific text they
 * were intended for.
 * </p>
 *
 * @see ETAStyle
 * @see Effect
 */
@Mixin(Style.class)
public class StyleMixin implements ETAStyle {

    /**
     * Storage for effects attached to this style.
     * Uses ImmutableList to ensure thread-safety and prevent external modification.
     * Defaults to empty list rather than null to simplify null-checking.
     */
    @Unique
    private ImmutableList<Effect> emberstextapi$effects = ImmutableList.of();

    /**
     * Item rendering properties
     */
    @Unique
    private String emberstextapi$itemId = null;

    @Unique
    private Integer emberstextapi$itemCount = null;

    @Unique
    private Float emberstextapi$itemOffsetX = null;

    @Unique
    private Float emberstextapi$itemOffsetY = null;

    /**
     * Entity rendering properties
     */
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

    /**
     * Typewriter track for animation state.
     */
    @Unique
    private TypewriterTrack emberstextapi$typewriterTrack = null;

    /**
     * Typewriter index (global character position).
     * -1 means uninitialized/not applicable.
     */
    @Unique
    private int emberstextapi$typewriterIndex = -1;

    /**
     * Obfuscate key used for caching animation state across renders.
     */
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

    // Entity rendering getters/setters

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

    /**
     * Propagate effects and typewriter data when Style methods return a new Style.
     * <p>
     * This ensures that when Style's withX() methods create a new Style instance,
     * the effects and typewriter data are copied to the new instance.
     * </p>
     */
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

        // Use our effects if we have them, otherwise use the other style's effects
        ImmutableList<Effect> effects = emberstextapi$effects.isEmpty()
                ? thatStyle.emberstextapi$getEffects()
                : emberstextapi$effects;
        resultStyle.emberstextapi$setEffects(effects);

        // Use our track if we have it, otherwise use the other style's track
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

    /**
     * Helper method to propagate data to a result Style.
     */
    @Unique
    private void emberstextapi$propagateData(Style result) {
        Style self = (Style) (Object) this;
        if (self == result) {
            return;
        }
        // Only propagate if we have data to propagate
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

    /**
     * Extend the equals() method to include effect comparison.
     * <p>
     * This ensures that two Style objects are only considered equal if they have
     * the same effects attached. Without this, styles with different effects would
     * incorrectly be considered equal if their vanilla properties matched.
     * </p>
     * <p>
     * We inject at HEAD and cancel early if effects don't match. This prevents
     * unnecessary processing of the rest of the equals logic.
     * </p>
     *
     * @param obj The object to compare against
     * @param cir Callback info to set the return value
     */
    @Inject(method = "equals", at = @At("HEAD"), cancellable = true)
    private void emberstextapi$equals(Object obj, CallbackInfoReturnable<Boolean> cir) {
        // Only check if comparing different objects
        if (this != obj && obj instanceof ETAStyle otherStyle) {
            // If effects don't match, styles are not equal
            if (!Objects.equals(this.emberstextapi$effects, otherStyle.emberstextapi$getEffects())) {
                cir.setReturnValue(false);
                return;
            }
            // If item properties don't match, styles are not equal
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
            // If entity properties don't match, styles are not equal
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
            // If typewriter index doesn't match, styles are not equal
            // This is critical to prevent characters with different indices from being merged
            if (this.emberstextapi$typewriterIndex != otherStyle.emberstextapi$getTypewriterIndex()) {
                cir.setReturnValue(false);
            }
        }
    }
}
