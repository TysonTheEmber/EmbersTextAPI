package net.tysontheember.emberstextapi.mixin.client;

import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.sdf.SDFProviderRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Mixin(FontSet.class)
public abstract class FontSetMixin {

    @Shadow
    @Final
    private ResourceLocation name;

    @ModifyVariable(method = "reload", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private List<GlyphProvider> emberstextapi$addSdfProviders(List<GlyphProvider> providers) {
        List<GlyphProvider> sdfProviders = SDFProviderRegistry.getProvidersForFont(this.name);
        if (sdfProviders == null || sdfProviders.isEmpty()) {
            return providers;
        }

        List<GlyphProvider> merged = new ArrayList<>(sdfProviders.size() + providers.size());
        merged.addAll(sdfProviders);
        merged.addAll(providers);
        return merged;
    }
}
