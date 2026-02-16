package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor mixin for Font class to expose private fields and methods.
 * <p>
 * This allows StringRenderOutputMixin to access Font internals needed for
 * custom character rendering with effects.
 * </p>
 */
@Mixin(Font.class)
public interface FontAccess {

    /**
     * Access the filterFishyGlyphs field to determine if glyph filtering is enabled.
     *
     * @return true if fishy glyphs should be filtered
     */
    @Accessor
    boolean getFilterFishyGlyphs();

    /**
     * Invoke the private getFontSet method to get the FontSet for a given ResourceLocation.
     *
     * @param resourceLocation The font resource location
     * @return The FontSet for the given font
     */
    @Invoker
    FontSet callGetFontSet(ResourceLocation resourceLocation);
}
