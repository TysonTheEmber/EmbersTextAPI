package net.tysontheember.emberstextapi.mixin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.tysontheember.emberstextapi.accessor.ETABakedGlyph;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Mixin for BakedGlyph to implement custom rendering with EffectSettings.
 * <p>
 * This mixin adds a new render method that accepts {@link EffectSettings}
 * instead of individual color/position parameters, allowing effects to
 * control all aspects of glyph rendering.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Uses position from EffectSettings (x, y)</li>
 *   <li>Uses color from EffectSettings (r, g, b, a)</li>
 *   <li>Supports vertical masking (maskTop, maskBottom) for glitch effects</li>
 *   <li>Handles italic slant correctly</li>
 * </ul>
 *
 * @see ETABakedGlyph
 * @see EffectSettings
 */
@Mixin(BakedGlyph.class)
public class BakedGlyphMixin implements ETABakedGlyph {

    // ===== Shadow Fields from BakedGlyph =====

    @Shadow
    @Final
    private float left;

    @Shadow
    @Final
    private float right;

    @Shadow
    @Final
    private float up;

    @Shadow
    @Final
    private float down;

    @Shadow
    @Final
    private float u0;

    @Shadow
    @Final
    private float v0;

    @Shadow
    @Final
    private float u1;

    @Shadow
    @Final
    private float v1;

    // ===== Custom Render Method =====

    /**
     * Render this glyph using parameters from EffectSettings.
     * <p>
     * This is a re-implementation of BakedGlyph's vanilla render() method,
     * but using values from EffectSettings instead of method parameters.
     * This allows effects to control position, color, and masking.
     * </p>
     *
     * <h3>Coordinate System:</h3>
     * <p>
     * The glyph is rendered as a textured quad with:
     * <ul>
     *   <li>Position offset by settings.x and settings.y</li>
     *   <li>UV coordinates from the glyph's texture atlas position</li>
     *   <li>Color from settings.r, settings.g, settings.b, settings.a</li>
     *   <li>Optional vertical masking via settings.maskTop/maskBottom</li>
     * </ul>
     * </p>
     *
     * <h3>Italic Handling:</h3>
     * <p>
     * When italic is true, the top vertices are offset horizontally to create
     * a slant effect. The offset is calculated as: {@code 1.0 - 0.25 * verticalPosition}
     * </p>
     *
     * @param settings Effect settings containing position, color, and masking
     * @param italic Whether to render with italic slant
     * @param boldOffset Horizontal offset for bold rendering (typically 0 or ~0.5)
     * @param pose Transformation matrix for the glyph
     * @param buffer Vertex consumer to write vertices to
     * @param packedLight Packed light coordinates for lighting
     */
    @Override
    public void emberstextapi$render(
            EffectSettings settings,
            boolean italic,
            float boldOffset,
            Matrix4f pose,
            VertexConsumer buffer,
            int packedLight) {

        // Calculate horizontal position with bold offset
        float x = settings.x + boldOffset;

        // Calculate vertex positions
        // left/right are relative to glyph origin
        float leftX = x + this.left;
        float rightX = x + this.right;

        // MC 1.21.1: up/down are used directly (no - 3.0f baseline adjustment;
        // that offset was removed in 1.21.1's BakedGlyph.render())
        float upY = settings.y + this.up;
        float downY = settings.y + this.down;

        // Calculate italic slant offsets
        // MC 1.21.1: italic uses this.up/this.down directly (not offset values)
        float italicOffsetUp = italic ? 1.0f - 0.25f * this.up : 0.0f;
        float italicOffsetDown = italic ? 1.0f - 0.25f * this.down : 0.0f;

        // UV coordinates (texture coordinates)
        float u0 = this.u0;
        float u1 = this.u1;
        float v0 = this.v0;
        float v1 = this.v1;

        // Apply vertical masking if present
        // This allows glitch effects to render only part of the character
        if (settings.maskTop != 0) {
            // Mask from top - adjust UV and position
            v0 += (this.v1 - this.v0) * settings.maskTop;
            upY += (this.down - this.up) * settings.maskTop;
        }
        if (settings.maskBottom != 0) {
            // Mask from bottom - adjust UV and position
            v1 -= (this.v1 - this.v0) * settings.maskBottom;
            downY -= (this.down - this.up) * settings.maskBottom;
        }

        // Render quad (4 vertices in counter-clockwise order)
        // Each vertex: position (x,y,z), color (r,g,b,a), UV (u,v), light

        // Top-left vertex
        buffer.addVertex(pose, leftX + italicOffsetUp, upY, 0.0f)
                .setColor(settings.r, settings.g, settings.b, settings.a)
                .setUv(u0, v0)
                .setLight(packedLight);

        // Bottom-left vertex
        buffer.addVertex(pose, leftX + italicOffsetDown, downY, 0.0f)
                .setColor(settings.r, settings.g, settings.b, settings.a)
                .setUv(u0, v1)
                .setLight(packedLight);

        // Bottom-right vertex
        buffer.addVertex(pose, rightX + italicOffsetDown, downY, 0.0f)
                .setColor(settings.r, settings.g, settings.b, settings.a)
                .setUv(u1, v1)
                .setLight(packedLight);

        // Top-right vertex
        buffer.addVertex(pose, rightX + italicOffsetUp, upY, 0.0f)
                .setColor(settings.r, settings.g, settings.b, settings.a)
                .setUv(u1, v0)
                .setLight(packedLight);
    }
}
