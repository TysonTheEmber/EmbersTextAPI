package net.tysontheember.emberstextapi.mixin.client;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;

@Mixin(Font.class)
public interface FontAccess {
    @Accessor
    boolean getFilterFishyGlyphs();

    @Invoker
    FontSet callGetFontSet(ResourceLocation font);

    @Invoker
    void callRenderChar(BakedGlyph glyph, boolean italic, boolean bold, float boldOffset, float x, float y, Matrix4f pose,
            VertexConsumer consumer, float red, float green, float blue, float alpha, int light);
}
