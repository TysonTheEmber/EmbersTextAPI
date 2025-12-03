package net.tysontheember.emberstextapi.mixin.duck;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import org.joml.Matrix4f;

/**
 * Duck interface for augmenting Minecraft's BakedGlyph class with custom rendering.
 * <p>
 * This interface is implemented via Mixin on {@link net.minecraft.client.gui.font.glyphs.BakedGlyph}
 * to add a custom rendering method that accepts EffectSettings instead of raw coordinates.
 * </p>
 * <p>
 * This allows effects to modify character rendering parameters (position, rotation, color)
 * before the glyph is rendered to the screen.
 * </p>
 *
 * <h3>Usage Pattern:</h3>
 * <pre>{@code
 * BakedGlyph glyph = fontSet.getGlyph(codepoint);
 * ETABakedGlyph etaGlyph = (ETABakedGlyph) glyph;
 *
 * EffectSettings settings = new EffectSettings(x, y, r, g, b, a, index, codepoint, false);
 * // Apply effects to settings...
 *
 * etaGlyph.emberstextapi$render(settings, italic, boldOffset, pose, buffer, light);
 * }</pre>
 *
 * @see net.tysontheember.emberstextapi.mixin.client.BakedGlyphMixin
 */
public interface ETABakedGlyph {

    /**
     * Render this glyph with effect-modified parameters.
     * <p>
     * This is an alternative to the vanilla render() method that accepts
     * an EffectSettings object instead of raw coordinate/color parameters.
     * </p>
     * <p>
     * The method extracts position (x, y), color (r, g, b, a), rotation, and other
     * parameters from the EffectSettings and applies them during rendering.
     * </p>
     *
     * @param settings Effect settings containing modified rendering parameters
     * @param italic Whether the text is italicized (affects glyph skewing)
     * @param boldOffset Horizontal offset for bold rendering (applied twice for thickness)
     * @param pose Transformation matrix for positioning in 3D space
     * @param buffer Vertex consumer for writing glyph geometry
     * @param packedLight Packed light value for lighting calculations
     */
    void emberstextapi$render(
        EffectSettings settings,
        boolean italic,
        float boldOffset,
        Matrix4f pose,
        VertexConsumer buffer,
        int packedLight
    );
}
