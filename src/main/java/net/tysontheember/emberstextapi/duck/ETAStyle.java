package net.tysontheember.emberstextapi.duck;

import com.google.common.collect.ImmutableList;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.typewriter.TypewriterTrack;

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

    // ===== Typewriter Effect Support =====

    /**
     * Get the typewriter track for this style.
     * <p>
     * The track manages animation state (timing, revealed character count).
     * Multiple characters sharing the same track will animate together.
     * </p>
     *
     * @return TypewriterTrack instance, or null if no typewriter effect
     */
    TypewriterTrack emberstextapi$getTypewriterTrack();

    /**
     * Set the typewriter track for this style.
     *
     * @param track TypewriterTrack instance
     */
    void emberstextapi$setTypewriterTrack(TypewriterTrack track);

    /**
     * Get the typewriter index (global character position) for this style.
     * <p>
     * This represents the ABSOLUTE position of this character in the full text,
     * used to determine when this character should be revealed relative to
     * the track's current reveal index.
     * </p>
     * <p>
     * A value of -1 indicates uninitialized/not applicable.
     * </p>
     *
     * @return Global character index, or -1 if not set
     */
    int emberstextapi$getTypewriterIndex();

    /**
     * Set the typewriter index (global character position) for this style.
     *
     * @param index Global character position (0-based)
     */
    void emberstextapi$setTypewriterIndex(int index);

    // ===== Obfuscate Effect Support =====
    Object emberstextapi$getObfuscateKey();
    void emberstextapi$setObfuscateKey(Object key);
    Object emberstextapi$getObfuscateStableKey();
    void emberstextapi$setObfuscateStableKey(Object key);

    int emberstextapi$getObfuscateSpanStart();
    void emberstextapi$setObfuscateSpanStart(int start);
    int emberstextapi$getObfuscateSpanLength();
    void emberstextapi$setObfuscateSpanLength(int length);

    // ===== Entity Rendering Support =====

    /**
     * Get the entity ID attached to this style for inline entity rendering.
     *
     * @return Entity resource location string (e.g., "minecraft:creeper"), or null if no entity
     */
    String emberstextapi$getEntityId();

    /**
     * Set the entity ID for inline entity rendering.
     *
     * @param entityId Entity resource location string (e.g., "minecraft:creeper")
     */
    void emberstextapi$setEntityId(String entityId);

    /**
     * Get the entity scale multiplier.
     *
     * @return Scale multiplier (defaults to 1.0 if not set)
     */
    Float emberstextapi$getEntityScale();

    /**
     * Set the entity scale multiplier.
     *
     * @param scale Scale multiplier
     */
    void emberstextapi$setEntityScale(Float scale);

    /**
     * Get the X offset for entity rendering.
     *
     * @return X offset in pixels, or null if not set
     */
    Float emberstextapi$getEntityOffsetX();

    /**
     * Set the X offset for entity rendering.
     *
     * @param offsetX X offset in pixels
     */
    void emberstextapi$setEntityOffsetX(Float offsetX);

    /**
     * Get the Y offset for entity rendering.
     *
     * @return Y offset in pixels, or null if not set
     */
    Float emberstextapi$getEntityOffsetY();

    /**
     * Set the Y offset for entity rendering.
     *
     * @param offsetY Y offset in pixels
     */
    void emberstextapi$setEntityOffsetY(Float offsetY);

    /**
     * Get the entity yaw (Y-axis rotation).
     *
     * @return Yaw in degrees (defaults to 45 if not set)
     */
    Float emberstextapi$getEntityYaw();

    /**
     * Set the entity yaw (Y-axis rotation).
     *
     * @param yaw Yaw in degrees
     */
    void emberstextapi$setEntityYaw(Float yaw);

    /**
     * Get the entity pitch (X-axis rotation).
     *
     * @return Pitch in degrees (defaults to 0 if not set)
     */
    Float emberstextapi$getEntityPitch();

    /**
     * Set the entity pitch (X-axis rotation).
     *
     * @param pitch Pitch in degrees
     */
    void emberstextapi$setEntityPitch(Float pitch);

    /**
     * Get the entity roll (Z-axis rotation).
     *
     * @return Roll in degrees (defaults to 0 if not set)
     */
    Float emberstextapi$getEntityRoll();

    /**
     * Set the entity roll (Z-axis rotation).
     *
     * @param roll Roll in degrees
     */
    void emberstextapi$setEntityRoll(Float roll);

    /**
     * Get the entity lighting level.
     *
     * @return Light level 0-15 (defaults to 15 = full bright if not set)
     */
    Integer emberstextapi$getEntityLighting();

    /**
     * Set the entity lighting level.
     *
     * @param lighting Light level 0-15
     */
    void emberstextapi$setEntityLighting(Integer lighting);

    /**
     * Get the entity spin speed (continuous rotation).
     *
     * @return Spin speed in degrees per tick (positive=clockwise, negative=counter-clockwise)
     */
    Float emberstextapi$getEntitySpin();

    /**
     * Set the entity spin speed (continuous rotation).
     *
     * @param spin Spin speed in degrees per tick
     */
    void emberstextapi$setEntitySpin(Float spin);

    /**
     * Get the entity animation state.
     *
     * @return Animation state string, or null if not set
     */
    String emberstextapi$getEntityAnimation();

    /**
     * Set the entity animation state.
     *
     * @param animation Animation state string
     */
    void emberstextapi$setEntityAnimation(String animation);
}
