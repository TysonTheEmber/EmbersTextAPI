package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor mixin for BakedGlyph to allow swapping the GlyphRenderTypes
 * after construction. Used by FontTextureMixin to replace standard render types
 * with SDF render types for SDF-backed glyphs.
 */
@Mixin(BakedGlyph.class)
public interface BakedGlyphAccessor {

    @Accessor("renderTypes")
    GlyphRenderTypes emberstextapi$getRenderTypes();

    @Mutable
    @Accessor("renderTypes")
    void emberstextapi$setRenderTypes(GlyphRenderTypes renderTypes);
}
