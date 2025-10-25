package net.tysontheember.emberstextapi.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;

@Mixin(Font.class)
public interface FontAccessor {
    @Invoker("getFontSet")
    FontSet emberstextapi$callGetFontSet(ResourceLocation font);

    @Accessor("filterFishyGlyphs")
    boolean getFilterFishyGlyphs();

    @Accessor("lineHeight")
    int getLineHeight();
}
