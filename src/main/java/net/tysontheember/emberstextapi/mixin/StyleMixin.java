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

        resultStyle.emberstextapi$setTypewriterTrack(track);
        resultStyle.emberstextapi$setTypewriterIndex(index);
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
        if (emberstextapi$effects.isEmpty() && emberstextapi$typewriterTrack == null) {
            return;
        }
        ETAStyle resultStyle = (ETAStyle) result;
        resultStyle.emberstextapi$setEffects(emberstextapi$effects);
        if (emberstextapi$typewriterTrack != null) {
            resultStyle.emberstextapi$setTypewriterTrack(emberstextapi$typewriterTrack);
            resultStyle.emberstextapi$setTypewriterIndex(emberstextapi$typewriterIndex);
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
            // If typewriter index doesn't match, styles are not equal
            // This is critical to prevent characters with different indices from being merged
            if (this.emberstextapi$typewriterIndex != otherStyle.emberstextapi$getTypewriterIndex()) {
                cir.setReturnValue(false);
            }
        }
    }
}
