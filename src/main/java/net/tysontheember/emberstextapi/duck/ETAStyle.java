package net.tysontheember.emberstextapi.duck;

import com.google.common.collect.ImmutableList;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;

/**
 * Duck interface for augmenting Minecraft's Style class with effect storage.
 * <p>
 * This interface is implemented via Mixin on {@link net.minecraft.network.chat.Style}
 * to add effect storage and manipulation capabilities without modifying the vanilla class directly.
 * </p>
 * <p>
 * Effects attached to a Style propagate through style inheritance (withColor, withBold, etc.)
 * and are applied during text rendering via the StringRenderOutputMixin.
 * </p>
 *
 * <h3>Usage Pattern:</h3>
 * <pre>{@code
 * Style style = Component.literal("text").getStyle();
 * ETAStyle etaStyle = (ETAStyle) style;
 * etaStyle.emberstextapi$setEffects(ImmutableList.of(new RainbowEffect()));
 * }</pre>
 *
 * @see net.tysontheember.emberstextapi.mixin.StyleMixin
 */
public interface ETAStyle {

    /**
    * Get the list of effects attached to this style.
    * Returns an immutable list to prevent external modification.
    * Use {@link #emberstextapi$setEffects(ImmutableList)} to modify.
    *
    * @return Immutable list of effects (never null, may be empty)
    */
    ImmutableList<Effect> emberstextapi$getEffects();

    /**
    * Set the complete list of effects for this style.
    * Replaces any existing effects. To add a single effect,
    * use {@link #emberstextapi$addEffect(Effect)} instead.
    *
    * @param effects New effect list (must not be null)
    */
    void emberstextapi$setEffects(ImmutableList<Effect> effects);

    /**
    * Add a single effect to this style's effect list.
    * Creates a new ImmutableList with the added effect.
    * If you need to add multiple effects, use {@link #emberstextapi$setEffects(ImmutableList)} instead.
    *
    * @param effect Effect to add (must not be null)
    */
    void emberstextapi$addEffect(Effect effect);
}
