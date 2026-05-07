package net.tysontheember.emberstextapi.sdf;

import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SDFRenderTypes {

    private static final Map<Identifier, GlyphRenderTypes> CACHE = new ConcurrentHashMap<>();

    private SDFRenderTypes() {}

    public static GlyphRenderTypes createForSDFTexture(Identifier atlasTexture) {
        if (!SDFShaders.isLoaded()) {

            return GlyphRenderTypes.createForIntensityTexture(atlasTexture);
        }

        return CACHE.computeIfAbsent(atlasTexture, tex -> {

            return GlyphRenderTypes.createForIntensityTexture(tex);
        });
    }

    public static void clearCache() {
        CACHE.clear();
    }
}
