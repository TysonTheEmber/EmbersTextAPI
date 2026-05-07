package net.tysontheember.emberstextapi.mixin.client;

import com.mojang.blaze3d.font.GlyphBitmap;
import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import net.minecraft.resources.Identifier;
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

    @Inject(method = "add", at = @At("RETURN"))
    private void emberstextapi$swapSdfRenderTypes(GlyphInfo glyphInfo, GlyphBitmap glyphBitmap, CallbackInfoReturnable<BakedSheetGlyph> cir) {
        if (glyphBitmap instanceof SDFSheetGlyphInfo && SDFShaders.isLoaded()) {
            BakedSheetGlyph baked = cir.getReturnValue();
            if (baked != null) {
                Identifier atlasLoc = SDFTextureTracker.getTextureLocation(this.renderTypes);
                if (atlasLoc != null) {
                    GlyphRenderTypes sdfTypes = SDFRenderTypes.createForSDFTexture(atlasLoc);
                    ((BakedGlyphAccessor) baked).emberstextapi$setRenderTypes(sdfTypes);
                }
            }
        }
    }
}
