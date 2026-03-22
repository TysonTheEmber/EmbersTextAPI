package net.tysontheember.emberstextapi.sdf;

import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Static registry that holds SDF glyph providers loaded during font reload.
 * <p>
 * Populated by {@code FontManagerMixin} during resource reload, consumed by
 * {@code FontSetMixin} (1.21.1) or directly by the {@code FontManagerMixin}
 * apply hook (1.20.1) to inject SDF providers into the appropriate font sets.
 * <p>
 * Each font {@link ResourceLocation} (e.g., {@code emberstextapi:norse}) maps
 * to a list of {@link GlyphProvider} instances. Providers are closed and cleared
 * at the start of each font reload cycle.
 */
public final class SDFProviderRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/SDFProviderRegistry");
    private static final Map<ResourceLocation, List<GlyphProvider>> PROVIDERS = new HashMap<>();

    private SDFProviderRegistry() {}

    public static void clear() {
        // Close any existing providers before clearing
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
