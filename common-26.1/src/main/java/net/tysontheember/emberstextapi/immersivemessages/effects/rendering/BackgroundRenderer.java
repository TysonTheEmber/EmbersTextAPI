package net.tysontheember.emberstextapi.immersivemessages.effects.rendering;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;
import net.tysontheember.emberstextapi.immersivemessages.util.RenderUtil;

public class BackgroundRenderer {

    public static void renderSolidBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                                              ImmersiveColor backgroundColor,
                                              ImmersiveColor borderStart, ImmersiveColor borderEnd,
                                              float alpha) {
        int bg = (Math.min(255, (int)(backgroundColor.getAlpha() * alpha)) << 24) | backgroundColor.getRGB();
        int start = (Math.min(255, (int)(borderStart.getAlpha() * alpha)) << 24) | borderStart.getRGB();
        int end = (Math.min(255, (int)(borderEnd.getAlpha() * alpha)) << 24) | borderEnd.getRGB();
        RenderUtil.drawBackground(graphics, x, y, width, height, bg, start, end);
    }

    public static void renderGradientBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                                                 ImmersiveColor[] gradientStops,
                                                 ImmersiveColor borderStart, ImmersiveColor borderEnd,
                                                 float alpha) {
        int[] cols = new int[gradientStops.length];
        for (int i = 0; i < gradientStops.length; i++) {
            ImmersiveColor c = gradientStops[i];
            int a = Math.min(255, (int)(c.getAlpha() * alpha));
            cols[i] = (a << 24) | c.getRGB();
        }
        int start = (Math.min(255, (int)(borderStart.getAlpha() * alpha)) << 24) | borderStart.getRGB();
        int end = (Math.min(255, (int)(borderEnd.getAlpha() * alpha)) << 24) | borderEnd.getRGB();
        RenderUtil.drawBackgroundGradient(graphics, x, y, width, height, cols, start, end);
    }

    public static void renderTextureBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                                                Identifier texture,
                                                int textureU, int textureV,
                                                int textureWidth, int textureHeight,
                                                int textureAtlasWidth, int textureAtlasHeight,
                                                ImmersiveMessage.TextureSizingMode sizingMode,
                                                float alpha) {

        com.mojang.blaze3d.pipeline.RenderPipeline pipeline = net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

        if (sizingMode == ImmersiveMessage.TextureSizingMode.STRETCH) {

            graphics.blit(pipeline, texture, x, y, (float) textureU, (float) textureV,
                    width, height, textureAtlasWidth, textureAtlasHeight);
        } else {

            int drawWidth = Math.min(width, textureWidth);
            int drawHeight = Math.min(height, textureHeight);
            int destX = Math.max(0, (width - drawWidth) / 2) + x;
            int destY = Math.max(0, (height - drawHeight) / 2) + y;
            int uOffset = textureU;
            int vOffset = textureV;
            if (drawWidth < textureWidth) {
                uOffset += (textureWidth - drawWidth) / 2;
            }
            if (drawHeight < textureHeight) {
                vOffset += (textureHeight - drawHeight) / 2;
            }
            graphics.blit(pipeline, texture, destX, destY, (float) uOffset, (float) vOffset,
                    drawWidth, drawHeight, textureAtlasWidth, textureAtlasHeight);
        }

    }

    public static int calculateShakeAdjustedWidth(int baseWidth, boolean hasShake, float shakeStrength) {
        return hasShake ? baseWidth + (int)(shakeStrength * 4f) : baseWidth;
    }
}
