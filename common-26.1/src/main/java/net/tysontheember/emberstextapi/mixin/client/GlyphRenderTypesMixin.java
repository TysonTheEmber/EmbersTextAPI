package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.resources.Identifier;
import net.tysontheember.emberstextapi.sdf.SDFTextureTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GlyphRenderTypes.class)
public class GlyphRenderTypesMixin {

    @Inject(method = "createForIntensityTexture", at = @At("RETURN"))
    private static void emberstextapi$trackIntensityTexture(Identifier location, CallbackInfoReturnable<GlyphRenderTypes> cir) {
        SDFTextureTracker.track(cir.getReturnValue(), location);
    }

    @Inject(method = "createForColorTexture", at = @At("RETURN"))
    private static void emberstextapi$trackColorTexture(Identifier location, CallbackInfoReturnable<GlyphRenderTypes> cir) {
        SDFTextureTracker.track(cir.getReturnValue(), location);
    }
}
