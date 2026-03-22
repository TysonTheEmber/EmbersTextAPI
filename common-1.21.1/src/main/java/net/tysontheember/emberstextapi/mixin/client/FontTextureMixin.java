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

/**
 * Mixin on FontTexture to swap the GlyphRenderTypes on BakedGlyph instances
 * that were created from SDF glyph data. This routes SDF glyphs through our
 * SDF shader without modifying any of the effects rendering code.
 * <p>
 * Also clears the atlas GL texture to zero before the first SDF glyph upload
 * to prevent bilinear filtering from sampling uninitialized memory at glyph
 * bounding box edges, which produces dark outline artifacts on shadows.
 */
@Mixin(FontTexture.class)
public abstract class FontTextureMixin {

    @Shadow
    @Final
    private GlyphRenderTypes renderTypes;

    @Unique
    private boolean emberstextapi$atlasCleared = false;

    /**
     * Before the first SDF glyph is added to this atlas, zero-fill the entire
     * GL texture. FontTexture allocates its texture via glTexImage2D(null),
     * leaving the contents undefined. With GL_LINEAR filtering (required for SDF),
     * the bilinear sampler at glyph bounding box edges can read the adjacent
     * undefined texel, producing non-zero median values that show as a faint
     * dark outline on shadows. Subsequent glyphs aren't affected because their
     * neighbors are already zero-filled padding from earlier uploads.
     */
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
