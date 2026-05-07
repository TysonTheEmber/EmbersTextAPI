package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Font.class)
public interface FontAccess {

    @Invoker("getGlyphSource")
    GlyphSource callGetGlyphSource(FontDescription fontDescription);

    @Accessor("random")
    RandomSource getRandom();
}
