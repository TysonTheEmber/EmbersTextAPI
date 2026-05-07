package net.tysontheember.emberstextapi.mixin.client;

import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.sdf.SDFRenderTypes;
import net.tysontheember.emberstextapi.sdf.SDFShaders;
import net.tysontheember.emberstextapi.sdf.SDFSheetGlyphInfo;
import net.tysontheember.emberstextapi.sdf.SDFTextureTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FontTexture.class)
public abstract class FontTextureMixin {

    @Shadow
    @Final
    private GlyphRenderTypes renderTypes;

    @Unique
    private boolean emberstextapi$atlasCleared = false;

    @Inject(method = "add", at = @At("HEAD"))
    private void emberstextapi$clearAtlasBeforeFirstSdf(SheetGlyphInfo glyphInfo, CallbackInfoReturnable<BakedGlyph> cir) {
        if (!emberstextapi$atlasCleared && glyphInfo instanceof SDFSheetGlyphInfo) {
            emberstextapi$atlasCleared = true;
            ((FontTexture) (Object) this).bind();
            NativeImage clear = new NativeImage(NativeImage.Format.RGBA, 256, 256, true);
            try {
                clear.upload(0, 0, 0, 0, 0, 256, 256, false, false);
            } finally {
                clear.close();
            }
        }
    }

    @Inject(method = "add", at = @At("RETURN"))
    private void emberstextapi$swapSdfRenderTypes(SheetGlyphInfo glyphInfo, CallbackInfoReturnable<BakedGlyph> cir) {
        if (glyphInfo instanceof SDFSheetGlyphInfo && SDFShaders.isLoaded()) {
            BakedGlyph baked = cir.getReturnValue();
            if (baked != null) {
                ResourceLocation atlasLoc = SDFTextureTracker.getTextureLocation(this.renderTypes);
                if (atlasLoc != null) {
                    GlyphRenderTypes sdfTypes = SDFRenderTypes.createForSDFTexture(atlasLoc);
                    ((BakedGlyphAccessor) baked).emberstextapi$setRenderTypes(sdfTypes);
                }
            }
        }
    }
}
