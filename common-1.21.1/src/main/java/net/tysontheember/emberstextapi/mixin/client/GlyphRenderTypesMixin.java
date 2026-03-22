package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.sdf.SDFTextureTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tracks which ResourceLocation was used to create each GlyphRenderTypes instance.
 * This allows FontTextureMixin to look up the atlas texture location when creating
 * SDF render types for SDF-backed glyphs.
 */
@Mixin(GlyphRenderTypes.class)
public class GlyphRenderTypesMixin {

    @Inject(method = "createForIntensityTexture", at = @At("RETURN"))
    private static void emberstextapi$trackIntensityTexture(ResourceLocation location, CallbackInfoReturnable<GlyphRenderTypes> cir) {
        SDFTextureTracker.track(cir.getReturnValue(), location);
    }

    @Inject(method = "createForColorTexture", at = @At("RETURN"))
    private static void emberstextapi$trackColorTexture(ResourceLocation location, CallbackInfoReturnable<GlyphRenderTypes> cir) {
        SDFTextureTracker.track(cir.getReturnValue(), location);
    }
}
