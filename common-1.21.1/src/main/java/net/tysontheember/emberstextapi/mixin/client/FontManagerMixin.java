package net.tysontheember.emberstextapi.mixin.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import net.tysontheember.emberstextapi.sdf.SDFGlyphProviderDefinition;
import net.tysontheember.emberstextapi.sdf.SDFProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.preset.PresetDefinition;
import net.tysontheember.emberstextapi.immersivemessages.effects.preset.PresetLoader;
import net.tysontheember.emberstextapi.immersivemessages.effects.preset.PresetRegistry;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.server.packs.resources.PreparableReloadListener;

/**
 * Mixin on FontManager to intercept font reload and load our custom
 * "emberstextapi:sdf" provider type from font JSON files.
 * <p>
 * Hooks into reload() to scan font JSONs and populate SDFProviderRegistry
 * before vanilla creates FontSets. FontSetMixin then injects these providers.
 */
@Mixin(FontManager.class)
public abstract class FontManagerMixin {

    @Unique
    private static final Logger emberstextapi$LOGGER = LoggerFactory.getLogger("EmbersTextAPI/FontManagerMixin");

    /**
     * Hook into reload() to load SDF providers before vanilla font loading proceeds.
     * The ResourceManager is available here and SDF providers will be registered
     * in SDFProviderRegistry for FontSetMixin to pick up.
     */
    @Inject(method = "reload", at = @At("HEAD"))
    private void emberstextapi$onReload(
            PreparableReloadListener.PreparationBarrier barrier,
            ResourceManager resourceManager,
            ProfilerFiller prepProfiler,
            ProfilerFiller applyProfiler,
            Executor backgroundExecutor,
            Executor gameExecutor,
            CallbackInfoReturnable<CompletableFuture<Void>> cir) {

        SDFProviderRegistry.clear();

        // Ensure effects are registered before loading presets (reload may fire before client setup)
        EffectRegistry.initializeDefaultEffects();

        // Load effect presets from resource packs
        PresetRegistry.clear();
        List<PresetDefinition> presets = PresetLoader.loadAll(resourceManager);
        for (PresetDefinition preset : presets) {
            PresetRegistry.register(preset);
        }

        try {
            if (!ConfigHelper.getInstance().isSdfEnabled()) {
                emberstextapi$LOGGER.info("SDF font rendering disabled via config");
                return;
            }
        } catch (Exception ignored) {
        }

        emberstextapi$loadSdfProviders(resourceManager);
    }

    @Unique
    private void emberstextapi$loadSdfProviders(ResourceManager resourceManager) {
        Map<ResourceLocation, List<net.minecraft.server.packs.resources.Resource>> fontFiles =
                resourceManager.listResourceStacks("font", rl -> rl.getPath().endsWith(".json"));

        for (Map.Entry<ResourceLocation, List<net.minecraft.server.packs.resources.Resource>> entry : fontFiles.entrySet()) {
            ResourceLocation fontJsonLoc = entry.getKey();

            String path = fontJsonLoc.getPath();
            if (!path.startsWith("font/") || !path.endsWith(".json")) continue;
            String fontName = path.substring(5, path.length() - 5);
            ResourceLocation fontId = ResourceLocation.fromNamespaceAndPath(fontJsonLoc.getNamespace(), fontName);

            for (net.minecraft.server.packs.resources.Resource resource : entry.getValue()) {
                try (Reader reader = new InputStreamReader(resource.open())) {
                    JsonObject json = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                    if (!json.has("providers")) continue;

                    JsonArray providers = json.getAsJsonArray("providers");
                    for (JsonElement elem : providers) {
                        if (!elem.isJsonObject()) continue;
                        JsonObject providerJson = elem.getAsJsonObject();

                        if (SDFGlyphProviderDefinition.isSdfProvider(providerJson)) {
                            GlyphProvider provider = SDFGlyphProviderDefinition.load(providerJson, resourceManager);
                            if (provider != null) {
                                SDFProviderRegistry.register(fontId, provider);
                            }
                        }
                    }
                } catch (Throwable t) {
                    emberstextapi$LOGGER.error("Error scanning font JSON {} for SDF providers", fontJsonLoc, t);
                }
            }
        }
    }
}
