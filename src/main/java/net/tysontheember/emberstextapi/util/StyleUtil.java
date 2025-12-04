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

        // Apply effects (if any)
        List<Effect> effects = span.getEffects();
        if (effects != null && !effects.isEmpty()) {
            // Use cloneAndAddEffects, but on the formatted style
            result = cloneAndAddEffects(result, effects);
        }

        return result;
    }
}
