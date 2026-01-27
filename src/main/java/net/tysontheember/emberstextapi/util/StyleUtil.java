package net.tysontheember.emberstextapi.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.duck.ETAStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for working with Style objects and effects in the global text styling system.
 * <p>
 * Provides helper methods for cloning styles and adding effects, used primarily by
 * the markup detection mixins (LiteralContentsMixin, TranslatableContentsMixin) to
 * attach parsed effects to style objects during text component visiting.
 * </p>
 *
 * @see net.tysontheember.emberstextapi.duck.ETAStyle
 * @see net.tysontheember.emberstextapi.mixin.StyleMixin
 */
public class StyleUtil {

    /**
     * Clone a style and add effects to it.
     * <p>
     * This method creates a copy of the original style and attaches the provided effects.
     * If the effects list is null or empty, the original style is returned unchanged.
     * </p>
     * <p>
     * This is the primary method used by markup detection mixins to attach parsed effects
     * from TextSpan objects to the Style object that will be used during rendering.
     * </p>
     * <p>
     * CRITICAL SAFETY: We MUST create a truly new Style instance, not a cached one.
     * Minecraft's Style is immutable and caches instances - if we mutate a cached instance,
     * effects will leak to all text using that style combination. We force new instance
     * creation by chaining withX() calls, which always create new objects.
     * </p>
     *
     * @param original The original style to clone
     * @param newEffects The effects to add to the cloned style
     * @return A new style with the effects attached, or the original if no effects
     */
    public static Style cloneAndAddEffects(Style original, List<Effect> newEffects) {
        // If no effects to add, return original unchanged
        if (newEffects == null || newEffects.isEmpty()) {
            return original;
        }

        // CRITICAL: Force creation of a new Style instance that is guaranteed to be isolated.
        // We cannot use original.applyTo(Style.EMPTY) because:
        // 1. Style is immutable and may return cached instances for common property combinations
        // 2. Mutating a cached instance would cause effects to leak globally
        //
        // Instead, we rebuild the style from scratch using withX() chaining, which always
        // creates new instances. We add a dummy operation at the end (withBold) to ensure
        // even styles with all default properties get a new instance.
        Style cloned = Style.EMPTY
                .withColor(original.getColor())
                .withBold(original.isBold())
                .withItalic(original.isItalic())
                .withUnderlined(original.isUnderlined())
                .withStrikethrough(original.isStrikethrough())
                .withObfuscated(original.isObfuscated())
                .withClickEvent(original.getClickEvent())
                .withHoverEvent(original.getHoverEvent())
                .withInsertion(original.getInsertion())
                .withFont(original.getFont());

        // Cast to our duck interface to access effect methods
        ETAStyle etaStyle = (ETAStyle) cloned;

        // Get existing effects (if any) from the original style
        ETAStyle originalEta = (ETAStyle) original;
        ImmutableList<Effect> existingEffects = originalEta.emberstextapi$getEffects();

        // Merge existing effects with new effects
        List<Effect> mergedEffects = new ArrayList<>();
        if (!existingEffects.isEmpty()) {
            mergedEffects.addAll(existingEffects);
        }
        mergedEffects.addAll(newEffects);

        // Set the merged effects on the cloned style
        etaStyle.emberstextapi$setEffects(ImmutableList.copyOf(mergedEffects));

        return cloned;
    }

    /**
     * Add effects to an existing style without cloning.
     * <p>
     * <b>WARNING:</b> This method modifies the style IN-PLACE by mutating the effects field.
     * Only use this when you're absolutely certain the style instance is:
     * <ul>
     *   <li>Newly created and not shared/cached</li>
     *   <li>Not Style.EMPTY or any other singleton</li>
     *   <li>Not used anywhere else in the codebase</li>
     * </ul>
     * </p>
     * <p>
     * In most cases, you should use {@link #cloneAndAddEffects(Style, List)} instead,
     * which guarantees isolation by creating a new Style instance.
     * </p>
     *
     * @param style The style to modify (will be mutated!)
     * @param effects The effects to add
     * @return The same style instance with effects added
     */
    public static Style addEffects(Style style, List<Effect> effects) {
        if (effects == null || effects.isEmpty()) {
            return style;
        }

        ETAStyle etaStyle = (ETAStyle) style;

        // Get existing effects and merge with new ones
        ImmutableList<Effect> existingEffects = etaStyle.emberstextapi$getEffects();
        List<Effect> mergedEffects = new ArrayList<>();
        if (!existingEffects.isEmpty()) {
            mergedEffects.addAll(existingEffects);
        }
        mergedEffects.addAll(effects);

        etaStyle.emberstextapi$setEffects(ImmutableList.copyOf(mergedEffects));
        return style;
    }

    /**
     * Create a new empty style with the given effects.
     * <p>
     * Convenience method for creating a style from scratch with effects attached.
     * </p>
     * <p>
     * CRITICAL SAFETY: Style.EMPTY is a singleton, so we MUST create a new instance
     * to avoid mutating the global empty style object. We cannot use applyTo() as it
     * may return the same singleton. Instead, we force new instance creation by using
     * withX() chaining.
     * </p>
     *
     * @param effects The effects to attach
     * @return A new empty style with the effects
     */
    public static Style withEffects(List<Effect> effects) {
        if (effects == null || effects.isEmpty()) {
            return Style.EMPTY;
        }

        // CRITICAL: Don't modify Style.EMPTY directly! Create a new instance.
        // We CANNOT use Style.EMPTY.applyTo(Style.EMPTY) because that returns Style.EMPTY itself!
        // Instead, we force creation of a new instance by calling a withX() method.
        // Using withColor((TextColor)null) creates a new Style instance with empty properties.
        Style style = Style.EMPTY.withColor((net.minecraft.network.chat.TextColor)null); // Forces new instance creation
        ETAStyle etaStyle = (ETAStyle) style;
        etaStyle.emberstextapi$setEffects(ImmutableList.copyOf(effects));
        return style;
    }

    /**
     * Apply formatting and effects from a TextSpan to a Style.
     * <p>
     * This method creates a new Style with:
     * <ul>
     *   <li>All properties from the original style</li>
     *   <li>Formatting properties from the TextSpan (bold, italic, underline, strikethrough, obfuscated)</li>
     *   <li>Effects from the TextSpan</li>
     * </ul>
     * </p>
     * <p>
     * This is the primary method used by markup detection mixins to apply both vanilla
     * formatting and custom effects from parsed markup tags to Style objects during rendering.
     * </p>
     *
     * @param original The original style to clone
     * @param span The TextSpan containing formatting and effects
     * @return A new style with formatting and effects applied
     */
    public static Style applyTextSpanFormatting(Style original, net.tysontheember.emberstextapi.immersivemessages.api.TextSpan span) {
        if (span == null) {
            return original;
        }

        // Start with the original style
        Style result = original;

        // Apply formatting properties from TextSpan
        // Only apply if the TextSpan explicitly sets the property (non-null and true)
        if (span.getBold() != null && span.getBold()) {
            result = result.withBold(true);
        }
        if (span.getItalic() != null && span.getItalic()) {
            result = result.withItalic(true);
        }
        if (span.getUnderline() != null && span.getUnderline()) {
            result = result.withUnderlined(true);
        }
        if (span.getStrikethrough() != null && span.getStrikethrough()) {
            result = result.withStrikethrough(true);
        }
        if (span.getObfuscated() != null && span.getObfuscated()) {
            result = result.withObfuscated(true);
        }

        // Apply font (if any)
        if (span.getFont() != null) {
            result = result.withFont(span.getFont());
        }

        // Apply effects (if any)
        List<Effect> effects = span.getEffects();
        if (effects != null && !effects.isEmpty()) {
            // Use cloneAndAddEffects, but on the formatted style
            result = cloneAndAddEffects(result, effects);
        }

        // Apply item data (if any)
        if (span.getItemId() != null) {
            result = cloneAndAddItem(
                result,
                span.getItemId(),
                span.getItemCount() != null ? span.getItemCount() : 1,
                span.getItemOffsetX() != null ? span.getItemOffsetX() : -4.0f,
                span.getItemOffsetY() != null ? span.getItemOffsetY() : -4.0f
            );
        }

        // Apply entity data (if any)
        if (span.getEntityId() != null) {
            result = cloneAndAddEntity(
                result,
                span.getEntityId(),
                span.getEntityScale() != null ? span.getEntityScale() : 1.0f,
                span.getEntityOffsetX() != null ? span.getEntityOffsetX() : 0f,
                span.getEntityOffsetY() != null ? span.getEntityOffsetY() : 0f,
                span.getEntityYaw() != null ? span.getEntityYaw() : 45f,
                span.getEntityPitch() != null ? span.getEntityPitch() : 0f,
                span.getEntityRoll() != null ? span.getEntityRoll() : 0f,
                span.getEntityLighting() != null ? span.getEntityLighting() : 15,
                span.getEntitySpin(),
                span.getEntityAnimation()
            );
        }

        return result;
    }

    /**
     * Create a style with an item attached for inline item rendering.
     * <p>
     * This creates a new style with item rendering data. The style can be applied to
     * any text component (typically a space character or empty string) to render an
     * item icon inline with text.
     * </p>
     * <p>
     * Example usage:
     * <pre>{@code
     * Style itemStyle = StyleUtil.withItem("minecraft:diamond", 1, 0f, 0f);
     * Component text = Component.literal(" ").withStyle(itemStyle);
     * }</pre>
     * </p>
     *
     * @param itemId Item resource location (e.g., "minecraft:diamond")
     * @param count Item stack count (must be >= 1)
     * @param offsetX X offset in pixels for item positioning
     * @param offsetY Y offset in pixels for item positioning
     * @return A new style with item data attached
     */
    public static Style withItem(String itemId, int count, float offsetX, float offsetY) {
        if (itemId == null || itemId.isEmpty()) {
            return Style.EMPTY;
        }

        // Create a new style instance (avoid mutating Style.EMPTY)
        Style style = Style.EMPTY.withColor((net.minecraft.network.chat.TextColor)null);
        ETAStyle etaStyle = (ETAStyle) style;

        // Set item properties
        etaStyle.emberstextapi$setItemId(itemId);
        etaStyle.emberstextapi$setItemCount(Math.max(1, count));
        etaStyle.emberstextapi$setItemOffsetX(offsetX);
        etaStyle.emberstextapi$setItemOffsetY(offsetY);

        return style;
    }

    /**
     * Create a style with an item attached (default offset -4, -4).
     *
     * @param itemId Item resource location (e.g., "minecraft:diamond")
     * @param count Item stack count
     * @return A new style with item data attached
     */
    public static Style withItem(String itemId, int count) {
        return withItem(itemId, count, -4.0f, -4.0f);
    }

    /**
     * Create a style with an item attached (count = 1, default offset -4, -4).
     *
     * @param itemId Item resource location (e.g., "minecraft:diamond")
     * @return A new style with item data attached
     */
    public static Style withItem(String itemId) {
        return withItem(itemId, 1, -4.0f, -4.0f);
    }

    /**
     * Add item data to an existing style.
     * <p>
     * This creates a clone of the original style and adds item rendering data.
     * </p>
     *
     * @param original The original style to clone
     * @param itemId Item resource location
     * @param count Item stack count
     * @param offsetX X offset in pixels
     * @param offsetY Y offset in pixels
     * @return A new style with item data added
     */
    public static Style cloneAndAddItem(Style original, String itemId, int count, float offsetX, float offsetY) {
        if (itemId == null || itemId.isEmpty()) {
            return original;
        }

        // Clone the style with all properties
        Style cloned = Style.EMPTY
                .withColor(original.getColor())
                .withBold(original.isBold())
                .withItalic(original.isItalic())
                .withUnderlined(original.isUnderlined())
                .withStrikethrough(original.isStrikethrough())
                .withObfuscated(original.isObfuscated())
                .withClickEvent(original.getClickEvent())
                .withHoverEvent(original.getHoverEvent())
                .withInsertion(original.getInsertion())
                .withFont(original.getFont());

        ETAStyle etaStyle = (ETAStyle) cloned;
        ETAStyle originalEta = (ETAStyle) original;

        // Copy effects from original if any
        ImmutableList<Effect> existingEffects = originalEta.emberstextapi$getEffects();
        if (!existingEffects.isEmpty()) {
            etaStyle.emberstextapi$setEffects(existingEffects);
        }

        // Set item properties
        etaStyle.emberstextapi$setItemId(itemId);
        etaStyle.emberstextapi$setItemCount(Math.max(1, count));
        etaStyle.emberstextapi$setItemOffsetX(offsetX);
        etaStyle.emberstextapi$setItemOffsetY(offsetY);

        return cloned;
    }

    /**
     * Clone a style and add entity rendering data.
     * <p>
     * Creates a copy of the original style with all properties preserved,
     * then adds entity rendering properties for inline entity display.
     * </p>
     *
     * @param original The style to clone
     * @param entityId Entity resource location (e.g., "minecraft:creeper")
     * @param scale Scale multiplier for entity size
     * @param offsetX X offset in pixels
     * @param offsetY Y offset in pixels
     * @param yaw Y-axis rotation in degrees
     * @param pitch X-axis rotation in degrees
     * @param roll Z-axis rotation in degrees
     * @param lighting Light level 0-15
     * @param spin Spin speed in degrees per tick (null for no spin)
     * @param animation Animation state (optional)
     * @return A new style with entity data attached
     */
    public static Style cloneAndAddEntity(
            Style original,
            String entityId,
            float scale,
            float offsetX,
            float offsetY,
            float yaw,
            float pitch,
            float roll,
            int lighting,
            Float spin,
            String animation
    ) {
        if (entityId == null || entityId.isEmpty()) {
            return original;
        }

        // Clone the style with all properties
        Style cloned = Style.EMPTY
                .withColor(original.getColor())
                .withBold(original.isBold())
                .withItalic(original.isItalic())
                .withUnderlined(original.isUnderlined())
                .withStrikethrough(original.isStrikethrough())
                .withObfuscated(original.isObfuscated())
                .withClickEvent(original.getClickEvent())
                .withHoverEvent(original.getHoverEvent())
                .withInsertion(original.getInsertion())
                .withFont(original.getFont());

        ETAStyle etaStyle = (ETAStyle) cloned;
        ETAStyle originalEta = (ETAStyle) original;

        // Copy effects from original if any
        ImmutableList<Effect> existingEffects = originalEta.emberstextapi$getEffects();
        if (!existingEffects.isEmpty()) {
            etaStyle.emberstextapi$setEffects(existingEffects);
        }

        // Set entity properties
        etaStyle.emberstextapi$setEntityId(entityId);
        etaStyle.emberstextapi$setEntityScale(scale);
        etaStyle.emberstextapi$setEntityOffsetX(offsetX);
        etaStyle.emberstextapi$setEntityOffsetY(offsetY);
        etaStyle.emberstextapi$setEntityYaw(yaw);
        etaStyle.emberstextapi$setEntityPitch(pitch);
        etaStyle.emberstextapi$setEntityRoll(roll);
        etaStyle.emberstextapi$setEntityLighting(Math.max(0, Math.min(15, lighting)));
        if (spin != null) {
            etaStyle.emberstextapi$setEntitySpin(spin);
        }
        if (animation != null && !animation.isEmpty()) {
            etaStyle.emberstextapi$setEntityAnimation(animation);
        }

        return cloned;
    }
}
