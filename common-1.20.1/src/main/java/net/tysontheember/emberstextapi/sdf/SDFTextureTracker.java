package net.tysontheember.emberstextapi.sdf;

import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the mapping from {@link GlyphRenderTypes} to the {@link ResourceLocation}
 * of the atlas texture they reference.
 * <p>
 * Populated by {@code GlyphRenderTypesMixin} when new atlas textures are created.
 * Consumed by {@code FontTextureMixin} to determine whether a glyph was uploaded
 * by an SDF provider, so it can swap the render types to the SDF shader variants
 * via {@link SDFRenderTypes#createForSDFTexture}.
 */
public final class SDFTextureTracker {

    private static final Map<GlyphRenderTypes, ResourceLocation> TEXTURE_LOCATIONS = new ConcurrentHashMap<>();

    private SDFTextureTracker() {}

    public static void track(GlyphRenderTypes renderTypes, ResourceLocation textureLocation) {
        TEXTURE_LOCATIONS.put(renderTypes, textureLocation);
    }

    @Nullable
    public static ResourceLocation getTextureLocation(GlyphRenderTypes renderTypes) {
        return TEXTURE_LOCATIONS.get(renderTypes);
    }

    public static void clear() {
        TEXTURE_LOCATIONS.clear();
    }
}
