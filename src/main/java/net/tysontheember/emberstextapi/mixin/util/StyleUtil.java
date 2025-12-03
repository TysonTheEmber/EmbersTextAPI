package net.tysontheember.emberstextapi.mixin.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.mixin.duck.ETAStyle;

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
 * @see net.tysontheember.emberstextapi.mixin.duck.ETAStyle
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

        // Clone the style by creating a copy with the same properties
        // We use applyTo to merge the style with EMPTY, which creates a new instance
        Style cloned = Style.EMPTY.applyTo(original);

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
     * This method modifies the style in-place by adding the provided effects.
     * Use this when you're working with a style that doesn't need to be preserved.
     * </p>
     *
     * @param style The style to modify
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
     *
     * @param effects The effects to attach
     * @return A new empty style with the effects
     */
    public static Style withEffects(List<Effect> effects) {
        if (effects == null || effects.isEmpty()) {
            return Style.EMPTY;
        }

        Style style = Style.EMPTY;
        ETAStyle etaStyle = (ETAStyle) style;
        etaStyle.emberstextapi$setEffects(ImmutableList.copyOf(effects));
        return style;
    }
}
