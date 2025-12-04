package net.tysontheember.emberstextapi.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
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
 * to provide getter/setter methods. Effects are propagated through all style modification
 * methods (withColor, withBold, etc.) to ensure they persist through style inheritance.
 * </p>
 * <p>
 * The applyTo method receives special handling to merge effects from both styles,
 * allowing effects to be combined when styles are merged.
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
     * Propagate effects through the withColor method.
     */
    @Inject(method = "withColor(Lnet/minecraft/network/chat/TextColor;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private void emberstextapi$withColor(TextColor color, CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateEffects(cir);
    }

    /**
     * Propagate effects through the withBold method.
     */
    @Inject(method = "withBold(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private void emberstextapi$withBold(Boolean bold, CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateEffects(cir);
    }

    /**
     * Propagate effects through the withItalic method.
     */
    @Inject(method = "withItalic(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private void emberstextapi$withItalic(Boolean italic, CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateEffects(cir);
    }

    /**
     * Propagate effects through the withUnderlined method.
     */
    @Inject(method = "withUnderlined(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private void emberstextapi$withUnderlined(Boolean underlined, CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateEffects(cir);
    }

    /**
     * Propagate effects through the withStrikethrough method.
     */
    @Inject(method = "withStrikethrough(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private void emberstextapi$withStrikethrough(Boolean strikethrough, CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateEffects(cir);
    }

    /**
     * Propagate effects through the withObfuscated method.
     */
    @Inject(method = "withObfuscated(Ljava/lang/Boolean;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private void emberstextapi$withObfuscated(Boolean obfuscated, CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateEffects(cir);
    }

    /**
     * Propagate effects through the withClickEvent method.
     */
    @Inject(method = "withClickEvent(Lnet/minecraft/network/chat/ClickEvent;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private void emberstextapi$withClickEvent(ClickEvent clickEvent, CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateEffects(cir);
    }

    /**
     * Propagate effects through the withHoverEvent method.
     */
    @Inject(method = "withHoverEvent(Lnet/minecraft/network/chat/HoverEvent;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private void emberstextapi$withHoverEvent(HoverEvent hoverEvent, CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateEffects(cir);
    }

    /**
     * Propagate effects through the withInsertion method.
     */
    @Inject(method = "withInsertion(Ljava/lang/String;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private void emberstextapi$withInsertion(String insertion, CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateEffects(cir);
    }

    /**
     * Propagate effects through the withFont method.
     */
    @Inject(method = "withFont(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private void emberstextapi$withFont(ResourceLocation font, CallbackInfoReturnable<Style> cir) {
        emberstextapi$propagateEffects(cir);
    }

    /**
     * Common helper method to propagate effects to the returned style.
     * <p>
     * This method is called by all withX() injection points. It checks if a new
     * Style instance was created, and if so, copies our effects to it.
     * </p>
     *
     * @param cir Callback info containing the return value (new Style)
     */
    @Unique
    private void emberstextapi$propagateEffects(CallbackInfoReturnable<Style> cir) {
        Style self = (Style) (Object) this;
        Style returned = cir.getReturnValue();

        // If the method returned 'this' (no new instance created), no propagation needed
        if (self == returned || returned == null) {
            return;
        }

        // If we have no effects, nothing to propagate
        if (emberstextapi$effects.isEmpty()) {
            return;
        }

        // Copy our effects to the new Style instance
        ((ETAStyle) returned).emberstextapi$setEffects(emberstextapi$effects);
    }

    /**
     * Handle the applyTo method specially to merge effects from both styles.
     * <p>
     * The applyTo method merges two styles together. The semantics are:
     * - 'this' is the base style
     * - 'that' is the style being applied on top
     * - Properties from 'that' override properties from 'this'
     * </p>
     * <p>
     * For effects, we follow the same pattern: effects from 'that' (the parameter)
     * take precedence. If 'that' has no effects, we use effects from 'this'.
     * This matches vanilla style merging behavior.
     * </p>
     *
     * @param that The style being applied to 'this'
     * @param cir Callback info containing the merged Style
     */
    @Inject(method = "applyTo(Lnet/minecraft/network/chat/Style;)Lnet/minecraft/network/chat/Style;", at = @At("RETURN"))
    private void emberstextapi$applyTo(Style that, CallbackInfoReturnable<Style> cir) {
        Style self = (Style) (Object) this;
        Style returned = cir.getReturnValue();

        // If no new style was created, return as-is
        if (self == returned || that == returned || returned == null) {
            return;
        }

        // Get effects from both styles
        ImmutableList<Effect> thisEffects = this.emberstextapi$effects;
        ImmutableList<Effect> thatEffects = ((ETAStyle) that).emberstextapi$getEffects();

        // Apply 'that' style's effects if present, otherwise fall back to 'this' style's effects
        // This matches vanilla behavior where 'that' properties override 'this' properties
        ETAStyle returnedStyle = (ETAStyle) returned;
        returnedStyle.emberstextapi$setEffects(thatEffects.isEmpty() ? thisEffects : thatEffects);
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
