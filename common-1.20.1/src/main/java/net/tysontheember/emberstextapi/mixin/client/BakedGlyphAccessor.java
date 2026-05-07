package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BakedGlyph.class)
public interface BakedGlyphAccessor {

    @Accessor("renderTypes")
    GlyphRenderTypes emberstextapi$getRenderTypes();

    @Mutable
    @Accessor("renderTypes")
    void emberstextapi$setRenderTypes(GlyphRenderTypes renderTypes);
}
