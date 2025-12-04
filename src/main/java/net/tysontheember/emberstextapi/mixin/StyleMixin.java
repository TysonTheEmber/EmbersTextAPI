package net.tysontheember.emberstextapi.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.duck.ETAStyle;
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

    @Override
    public ImmutableList<Effect> emberstextapi$getEffects() {
        return emberstextapi$effects;
    }

    @Override
    public void emberstextapi$setEffects(ImmutableList<Effect> effects) {
        this.emberstextapi$effects = effects != null ? effects : ImmutableList.of();
    }

    @Override
    public void emberstextapi$addEffect(Effect effect) {
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
            }
        }
    }
}
