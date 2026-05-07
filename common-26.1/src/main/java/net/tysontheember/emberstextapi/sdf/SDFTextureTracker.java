package net.tysontheember.emberstextapi.sdf;

import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SDFTextureTracker {

    private static final Map<GlyphRenderTypes, Identifier> TEXTURE_LOCATIONS = new ConcurrentHashMap<>();

    private SDFTextureTracker() {}

    public static void track(GlyphRenderTypes renderTypes, Identifier textureLocation) {
        TEXTURE_LOCATIONS.put(renderTypes, textureLocation);
    }

    @Nullable
    public static Identifier getTextureLocation(GlyphRenderTypes renderTypes) {
        return TEXTURE_LOCATIONS.get(renderTypes);
    }

    public static void clear() {
        TEXTURE_LOCATIONS.clear();
    }
}
