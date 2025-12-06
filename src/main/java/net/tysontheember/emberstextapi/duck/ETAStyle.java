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

    /**
     * Get the item ID attached to this style for inline item rendering.
     *
     * @return Item resource location string (e.g., "minecraft:diamond"), or null if no item
     */
    String emberstextapi$getItemId();

    /**
     * Set the item ID for inline item rendering.
     *
     * @param itemId Item resource location string (e.g., "minecraft:diamond")
     */
    void emberstextapi$setItemId(String itemId);

    /**
     * Get the item stack count for item rendering.
     *
     * @return Item count (defaults to 1 if not set)
     */
    Integer emberstextapi$getItemCount();

    /**
     * Set the item stack count for item rendering.
     *
     * @param count Item count (must be >= 1)
     */
    void emberstextapi$setItemCount(Integer count);

    /**
     * Get the X offset for item rendering.
     *
     * @return X offset in pixels, or null if not set
     */
    Float emberstextapi$getItemOffsetX();

    /**
     * Set the X offset for item rendering.
     *
     * @param offsetX X offset in pixels
     */
    void emberstextapi$setItemOffsetX(Float offsetX);

    /**
     * Get the Y offset for item rendering.
     *
     * @return Y offset in pixels, or null if not set
     */
    Float emberstextapi$getItemOffsetY();

    /**
     * Set the Y offset for item rendering.
     *
     * @param offsetY Y offset in pixels
     */
    void emberstextapi$setItemOffsetY(Float offsetY);
}
