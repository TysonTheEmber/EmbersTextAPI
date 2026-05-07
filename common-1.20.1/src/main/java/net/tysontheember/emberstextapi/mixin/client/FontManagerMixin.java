package net.tysontheember.emberstextapi.mixin.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import net.tysontheember.emberstextapi.sdf.SDFGlyphProviderDefinition;
import net.tysontheember.emberstextapi.sdf.SDFProviderRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.MessageEffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.MessageAttributeRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.preset.PresetDefinition;
import net.tysontheember.emberstextapi.immersivemessages.effects.preset.PresetLoader;
import net.tysontheember.emberstextapi.immersivemessages.effects.preset.PresetRegistry;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.server.packs.resources.PreparableReloadListener;

@Mixin(FontManager.class)
public abstract class FontManagerMixin {

    @Shadow
    @Final
    private Map<ResourceLocation, FontSet> fontSets;

    @Shadow
    @Final
    private TextureManager textureManager;

    @Unique
    private static final Logger emberstextapi$LOGGER = LoggerFactory.getLogger("EmbersTextAPI/FontManagerMixin");

    @Redirect(method = "loadResourceStack",
            at = @At(value = "INVOKE",
                    target = "Lcom/google/gson/Gson;fromJson(Ljava/io/Reader;Ljava/lang/Class;)Ljava/lang/Object;"))
    private static Object emberstextapi$stripSdfProviders(Gson gson, Reader reader, Class<?> type) {
        Object raw = gson.fromJson(reader, type);
        if (!(raw instanceof JsonElement el) || !el.isJsonObject()) return raw;
        JsonObject root = el.getAsJsonObject();
        if (!root.has("providers") || !root.get("providers").isJsonArray()) return raw;

        JsonArray src = root.getAsJsonArray("providers");
        JsonArray dst = new JsonArray(src.size());
        boolean changed = false;
        for (JsonElement e : src) {
            if (e.isJsonObject() && SDFGlyphProviderDefinition.isSdfProvider(e.getAsJsonObject())) {
                changed = true;
                continue;
            }
            dst.add(e);
        }
        if (changed) root.add("providers", dst);
        return raw;
    }

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

        EffectRegistry.initializeDefaultEffects();
        MessageEffectRegistry.initializeDefaultEffects();
        MessageAttributeRegistry.initializeDefaultAttributes();

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

    @Inject(method = "apply", at = @At("TAIL"))
    private void emberstextapi$afterApply(CallbackInfo ci) {
        if (!SDFProviderRegistry.hasProviders()) {
            return;
        }

        for (ResourceLocation fontId : SDFProviderRegistry.getFontIds()) {
            List<GlyphProvider> sdfProviders = SDFProviderRegistry.getProvidersForFont(fontId);
            if (sdfProviders == null || sdfProviders.isEmpty()) continue;

            FontSet fontSet = this.fontSets.get(fontId);
            if (fontSet == null) {

                fontSet = new FontSet(this.textureManager, fontId);
                this.fontSets.put(fontId, fontSet);
                emberstextapi$LOGGER.info("Created FontSet for SDF font '{}'", fontId);
            } else {
                emberstextapi$LOGGER.info("Updating existing FontSet for SDF font '{}'", fontId);
            }

            fontSet.reload(new ArrayList<>(sdfProviders));
            emberstextapi$LOGGER.info("Loaded {} SDF providers into FontSet '{}'", sdfProviders.size(), fontId);
        }
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
            ResourceLocation fontId = new ResourceLocation(fontJsonLoc.getNamespace(), fontName);

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
