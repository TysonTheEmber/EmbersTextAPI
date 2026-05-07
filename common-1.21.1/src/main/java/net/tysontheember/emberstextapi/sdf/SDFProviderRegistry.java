package net.tysontheember.emberstextapi.sdf;

import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class SDFProviderRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/SDFProviderRegistry");
    private static final Map<ResourceLocation, List<GlyphProvider>> PROVIDERS = new ConcurrentHashMap<>();

    private SDFProviderRegistry() {}

    public static void clear() {

        for (List<GlyphProvider> list : PROVIDERS.values()) {
            for (GlyphProvider provider : list) {
                provider.close();
            }
        }
        PROVIDERS.clear();
    }

    public static void register(ResourceLocation fontName, GlyphProvider provider) {
        PROVIDERS.computeIfAbsent(fontName, k -> new ArrayList<>()).add(provider);
        LOGGER.info("Registered SDF provider for font '{}'", fontName);
    }

    @Nullable
    public static List<GlyphProvider> getProvidersForFont(ResourceLocation fontName) {
        return PROVIDERS.get(fontName);
    }

    public static boolean hasProviders() {
        return !PROVIDERS.isEmpty();
    }
}
