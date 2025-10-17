package net.tysontheember.emberstextapi.text;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Utilities for converting between Forge/Minecraft {@link ResourceLocation}
 * values and the core {@link EmbersKey} identifier type.
 */
public final class EmbersKeyAdapter {
    private EmbersKeyAdapter() {
    }

    @Nullable
    public static EmbersKey fromResource(@Nullable ResourceLocation location) {
        if (location == null) {
            return null;
        }
        return EmbersKey.of(location.getNamespace(), location.getPath());
    }

    @Nullable
    public static ResourceLocation toResource(@Nullable EmbersKey key) {
        if (key == null) {
            return null;
        }
        return new ResourceLocation(key.namespace(), key.path());
    }
}
