package net.tysontheember.emberstextapi.sdf;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SDFRenderTypes extends RenderType {

    private static final Map<ResourceLocation, GlyphRenderTypes> CACHE = new ConcurrentHashMap<>();

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
