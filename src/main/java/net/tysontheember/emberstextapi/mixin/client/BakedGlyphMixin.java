package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.tysontheember.emberstextapi.duck.ETABakedGlyph;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Placeholder duck implementation. Real rendering hooks arrive in Phase D2.
 */
@Mixin(BakedGlyph.class)
public abstract class BakedGlyphMixin implements ETABakedGlyph {
    @Override
    public void eta$renderWith(float x, float y, int rgba, boolean shadow) {
        // Phase D1 stub: intentionally left blank.
    }
}
