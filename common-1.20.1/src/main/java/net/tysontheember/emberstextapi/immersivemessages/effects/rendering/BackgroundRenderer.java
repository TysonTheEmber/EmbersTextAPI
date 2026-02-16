package net.tysontheember.emberstextapi.immersivemessages.effects.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;
import net.tysontheember.emberstextapi.immersivemessages.util.RenderUtil;

/**
 * Handles rendering of message backgrounds including solid colors, gradients, and textures.
 * Extracts background rendering logic from ImmersiveMessage.
 */
public class BackgroundRenderer {

    /**
     * Renders a solid color background with border gradient.
     *
     * @param graphics GUI graphics context
     * @param x X position
     * @param y Y position
     * @param width Background width
     * @param height Background height
     * @param backgroundColor Main background color
     * @param borderStart Border gradient start color
     * @param borderEnd Border gradient end color
     * @param alpha Global alpha multiplier [0, 1]
     */
    public static void renderSolidBackground(GuiGraphics graphics, int x, int y, int width, int height,
                                              ImmersiveColor backgroundColor,
                                              ImmersiveColor borderStart, ImmersiveColor borderEnd,
                                              float alpha) {
        int bg = (Math.min(255, (int)(backgroundColor.getAlpha() * alpha)) << 24) | backgroundColor.getRGB();
        int start = (Math.min(255, (int)(borderStart.getAlpha() * alpha)) << 24) | borderStart.getRGB();
        int end = (Math.min(255, (int)(borderEnd.getAlpha() * alpha)) << 24) | borderEnd.getRGB();
        RenderUtil.drawBackground(graphics, x, y, width, height, bg, start, end);
    }

    /**
     * Renders a multi-stop gradient background with border gradient.
     *
     * @param graphics GUI graphics context
     * @param x X position
     * @param y Y position
     * @param width Background width
     * @param height Background height
     * @param gradientStops Array of gradient color stops
     * @param borderStart Border gradient start color
     * @param borderEnd Border gradient end color
     * @param alpha Global alpha multiplier [0, 1]
     */
    public static void renderGradientBackground(GuiGraphics graphics, int x, int y, int width, int height,
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

    /**
     * Renders a texture background with optional sizing modes.
     *
     * @param graphics GUI graphics context
     * @param x X position
     * @param y Y position
     * @param width Background width
     * @param height Background height
     * @param texture Texture resource location
     * @param textureU U coordinate in texture atlas
     * @param textureV V coordinate in texture atlas
     * @param textureWidth Width of texture region
     * @param textureHeight Height of texture region
     * @param textureAtlasWidth Total atlas width
     * @param textureAtlasHeight Total atlas height
     * @param sizingMode STRETCH or CROP sizing mode
     * @param alpha Global alpha multiplier [0, 1]
     */
    public static void renderTextureBackground(GuiGraphics graphics, int x, int y, int width, int height,
                                                ResourceLocation texture,
                                                int textureU, int textureV,
                                                int textureWidth, int textureHeight,
                                                int textureAtlasWidth, int textureAtlasHeight,
                                                ImmersiveMessage.TextureSizingMode sizingMode,
                                                float alpha) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

        if (sizingMode == ImmersiveMessage.TextureSizingMode.STRETCH) {
            // Stretch texture to fill background
            graphics.blit(texture, x, y, width, height,
                    textureU, textureV, textureWidth, textureHeight,
                    textureAtlasWidth, textureAtlasHeight);
        } else {
            // Tile/center texture
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
            graphics.blit(texture, destX, destY, drawWidth, drawHeight,
                    uOffset, vOffset, drawWidth, drawHeight,
                    textureAtlasWidth, textureAtlasHeight);
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
    }

    /**
     * Calculates the appropriate background width considering shake effects.
     *
     * @param baseWidth Base background width
     * @param hasShake Whether global shake is enabled
     * @param shakeStrength Shake strength multiplier
     * @return Adjusted background width
     */
    public static int calculateShakeAdjustedWidth(int baseWidth, boolean hasShake, float shakeStrength) {
        return hasShake ? baseWidth + (int)(shakeStrength * 4f) : baseWidth;
    }
}
