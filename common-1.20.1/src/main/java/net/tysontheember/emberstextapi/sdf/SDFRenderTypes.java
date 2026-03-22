package net.tysontheember.emberstextapi.sdf;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating {@link GlyphRenderTypes} that use the MSDF text shader.
 * <p>
 * Creates three render type variants per atlas texture:
 * <ul>
 *   <li><b>normal</b> — standard rendering with fog and depth test</li>
 *   <li><b>see-through</b> — no depth test, no fog (for text visible through blocks)</li>
 *   <li><b>polygon-offset</b> — with polygon offset layering (for overlapping text)</li>
 * </ul>
 * All variants use the MSDF fragment shader that takes the median of three RGB channels
 * to reconstruct the glyph distance for sharp corner reproduction.
 * <p>
 * Extends {@link RenderType} to access protected {@code CompositeState} and
 * {@code RenderStateShard} constants required for render type construction.
 *
 * @see SDFShaders
 * @see SDFTextureTracker
 */
public abstract class SDFRenderTypes extends RenderType {

    /**
     * Cache of GlyphRenderTypes by atlas texture, so all glyphs on the same atlas
     * share render type instances. This ensures consistent buffer identity in
     * MultiBufferSource and prevents intermediate flushes when switching between
     * characters in the same draw call.
     */
    private static final Map<ResourceLocation, GlyphRenderTypes> CACHE = new ConcurrentHashMap<>();

    // No custom TransparencyStateShard needed — the shader JSON defines the blend
    // function with alpha-preserving factors (srcalpha=0, dstalpha=1) that get applied
    // inside ShaderInstance.apply(), right before the draw call.

    // Dummy constructor — never instantiated, only used for static access to protected members
    private SDFRenderTypes() {
        super("eta_sdf_dummy", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 256, false, false, () -> {}, () -> {});
    }

    public static GlyphRenderTypes createForSDFTexture(ResourceLocation atlasTexture) {
        if (!SDFShaders.isLoaded()) {
            return GlyphRenderTypes.createForIntensityTexture(atlasTexture);
        }

        return CACHE.computeIfAbsent(atlasTexture, tex -> {
            RenderType normal = createSdfRenderType("eta_sdf_text", tex, false, false);
            RenderType seeThrough = createSdfRenderType("eta_sdf_text_see_through", tex, true, false);
            RenderType polygonOffset = createSdfRenderType("eta_sdf_text_polygon_offset", tex, false, true);
            return new GlyphRenderTypes(normal, seeThrough, polygonOffset);
        });
    }

    /**
     * Clears the render type cache. Called when shaders are reloaded to pick up
     * new shader instances.
     */
    public static void clearCache() {
        CACHE.clear();
    }

    private static RenderType createSdfRenderType(
            String name, ResourceLocation texture, boolean seeThrough, boolean polygonOffset) {

        CompositeState.CompositeStateBuilder builder = CompositeState.builder()
                .setShaderState(seeThrough
                        ? new ShaderStateShard(SDFShaders::getSdfTextSeeThroughShader)
                        : new ShaderStateShard(SDFShaders::getSdfTextShader))
                .setTextureState(new TextureStateShard(texture, true, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(LIGHTMAP);

        if (polygonOffset) {
            builder.setLayeringState(POLYGON_OFFSET_LAYERING);
        }

        if (seeThrough) {
            builder.setDepthTestState(NO_DEPTH_TEST);
            builder.setWriteMaskState(COLOR_WRITE);
        }

        return create(
                name,
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS,
                256,
                false,
                true,
                builder.createCompositeState(false)
        );
    }
}
