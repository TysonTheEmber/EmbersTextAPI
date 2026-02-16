package net.tysontheember.emberstextapi.immersivemessages.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

/** Utility methods for drawing simple shapes. */
public final class RenderUtil {
    private RenderUtil() {}

    public static void fillGradient(GuiGraphics graphics, int x1, int y1, int x2, int y2, int top, int bottom) {
        graphics.fillGradient(x1, y1, x2, y2, top, bottom);
    }

    /**
     * Draws a tooltip-style background identical to the original
     * ImmersiveMessages implementation. A 1px border is rendered on all
     * sides with a gradient that travels around the border from
     * {@code borderStart} to {@code borderEnd}.
     *
     * @param graphics target GUI graphics
     * @param x top-left x of the text area
     * @param y top-left y of the text area
     * @param width width of the text area
     * @param height height of the text area
     * @param background ARGB colour of the background fill
     * @param borderStart ARGB colour for the starting corner (top-left)
     * @param borderEnd ARGB colour for the opposite corner (bottom-right)
     */
    public static void drawBackground(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int background,
            int borderStart,
            int borderEnd
    ) {
        int xMaxL = x - 3;
        int xMinL = x - 4;
        int xMinR = x + width + 3;
        int xMaxR = x + width + 4;

        int yMaxT = y - 4;
        int yMinT = y - 5;
        int yMaxB = y + height + 4;
        int yMinB = y + height + 3;

        // main background (center, top, bottom, left, right)
        graphics.fillGradient(xMaxL, yMinT, xMinR, yMaxT, background, background);
        graphics.fillGradient(xMaxL, yMinB, xMinR, yMaxB, background, background);
        graphics.fillGradient(xMaxL, yMaxT, xMinR, yMinB, background, background);
        graphics.fillGradient(xMinL, yMaxT, xMaxL, yMinB, background, background);
        graphics.fillGradient(xMinR, yMaxT, xMaxR, yMinB, background, background);

        // top/bottom borders
        int topWidth = xMinR - xMaxL;
        for (int i = 0; i < topWidth; i++) {
            float t = topWidth <= 1 ? 0f : (float) i / (topWidth - 1);
            int col = lerpColour(borderStart, borderEnd, t);
            graphics.fill(xMaxL + i, yMaxT, xMaxL + i + 1, yMaxT + 1, col);
            graphics.fill(xMinR - 1 - i, yMinB - 1, xMinR - i, yMinB, col);
        }

        // side borders
        int sideHeight = yMinB - yMaxT - 2;
        for (int i = 0; i < sideHeight; i++) {
            float t = sideHeight <= 1 ? 0f : (float) i / (sideHeight - 1);
            int rightCol = lerpColour(borderEnd, borderStart, t);
            graphics.fill(xMinR - 1, yMaxT + 1 + i, xMinR, yMaxT + 2 + i, rightCol);
            int leftCol = lerpColour(borderStart, borderEnd, t);
            graphics.fill(xMaxL, yMaxT + 1 + i, xMaxL + 1, yMaxT + 2 + i, leftCol);
        }
    }

    private static int lerpColour(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = a & 0xFF;
        int aa = (a >> 24) & 0xFF;

        int br = (b >> 16) & 0xFF;
        int bg = (b >> 8) & 0xFF;
        int bb = b & 0xFF;
        int ba = (b >> 24) & 0xFF;

        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        int aOut = (int) (aa + (ba - aa) * t);
        return (aOut & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (bl & 0xFF);
    }

    public static void drawBackgroundGradient(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int[] gradient,
            int borderTop,
            int borderBottom
    ) {
        int xMaxL = x - 3;
        int xMinL = x - 4;
        int xMinR = x + width + 3;
        int xMaxR = x + width + 4;

        int yMaxT = y - 4;
        int yMinT = y - 5;
        int yMaxB = y + height + 4;
        int yMinB = y + height + 3;

        int darkerTop = lerpColour(0xFF000000, borderTop, 0.5f);
        int darkerBottom = lerpColour(0xFF000000, borderBottom, 0.5f);

        // main background (center, top, bottom, left, right) — using multi-stop gradient
        fillHorizontalGradient(graphics, xMaxL, yMinT, xMinR, yMaxT, gradient);
        fillHorizontalGradient(graphics, xMaxL, yMinB, xMinR, yMaxB, gradient);
        fillHorizontalGradient(graphics, xMaxL, yMaxT, xMinR, yMinB, gradient);
        fillHorizontalGradient(graphics, xMinL, yMaxT, xMaxL, yMinB, gradient);
        fillHorizontalGradient(graphics, xMinR, yMaxT, xMaxR, yMinB, gradient);

        // inner top/bottom borders
        int topWidth = xMinR - xMaxL;
        for (int i = 0; i < topWidth; i++) {
            float t = topWidth <= 1 ? 0f : (float) i / (topWidth - 1);
            int col = lerpColour(borderTop, borderBottom, t);
            graphics.fill(xMaxL + i, yMaxT, xMaxL + i + 1, yMaxT + 1, col);
            graphics.fill(xMinR - 1 - i, yMinB - 1, xMinR - i, yMinB, col);
        }

        // inner side borders
        int sideHeight = yMinB - yMaxT - 2;
        for (int i = 0; i < sideHeight; i++) {
            float t = sideHeight <= 1 ? 0f : (float) i / (sideHeight - 1);
            int rightCol = lerpColour(borderBottom, borderTop, t);
            graphics.fill(xMinR - 1, yMaxT + 1 + i, xMinR, yMaxT + 2 + i, rightCol);
            int leftCol = lerpColour(borderTop, borderBottom, t);
            graphics.fill(xMaxL, yMaxT + 1 + i, xMaxL + 1, yMaxT + 2 + i, leftCol);
        }

        // outer (halo) border — darker version based on inner gradient
        int outerTopWidth = xMaxR - xMinL;
        for (int i = 0; i < outerTopWidth; i++) {
            float tInner = topWidth <= 1 ? 0f : (float) Mth.clamp(i - 1, 0, topWidth - 1) / (topWidth - 1);
            int innerCol = lerpColour(borderTop, borderBottom, tInner);
            int col = lerpColour(0xFF000000, innerCol, 0.5f);
            graphics.fill(xMinL + i, yMinT, xMinL + i + 1, yMinT + 1, col);
            graphics.fill(xMaxR - 1 - i, yMaxB - 1, xMaxR - i, yMaxB, col);
        }

        int outerSideHeight = yMaxB - yMinT - 2;
        for (int i = 0; i < outerSideHeight; i++) {
            float tInner = sideHeight <= 1 ? 0f : (float) Mth.clamp(i - 1, 0, sideHeight - 1) / (sideHeight - 1);
            int rightInner = lerpColour(borderBottom, borderTop, tInner);
            int rightCol = lerpColour(0xFF000000, rightInner, 0.5f);
            graphics.fill(xMaxR - 1, yMinT + 1 + i, xMaxR, yMinT + 2 + i, rightCol);
            int leftInner = lerpColour(borderTop, borderBottom, tInner);
            int leftCol = lerpColour(0xFF000000, leftInner, 0.5f);
            graphics.fill(xMinL, yMinT + 1 + i, xMinL + 1, yMinT + 2 + i, leftCol);
        }
    }

    private static void fillHorizontalGradient(GuiGraphics graphics, int x1, int y1, int x2, int y2, int[] gradient) {
        int w = x2 - x1;
        for (int i = 0; i < w; i++) {
            float t = w <= 1 ? 0f : i / (float) (w - 1);
            int color = sampleGradient(gradient, t);
            graphics.fill(x1 + i, y1, x1 + i + 1, y2, color);
        }
    }

    private static int sampleGradient(int[] gradient, float t) {
        int segments = gradient.length - 1;
        float scaled = t * segments;
        int idx = Mth.clamp((int) Math.floor(scaled), 0, segments - 1);
        float local = scaled - idx;
        return lerpArgb(gradient[idx], gradient[idx + 1], local);
    }

    private static int lerpArgb(int start, int end, float t) {
        int sa = (start >> 24) & 0xFF;
        int sr = (start >> 16) & 0xFF;
        int sg = (start >> 8) & 0xFF;
        int sb = start & 0xFF;
        int ea = (end >> 24) & 0xFF;
        int er = (end >> 16) & 0xFF;
        int eg = (end >> 8) & 0xFF;
        int eb = end & 0xFF;
        int a = (int) Mth.lerp(t, sa, ea);
        int r = (int) Mth.lerp(t, sr, er);
        int g = (int) Mth.lerp(t, sg, eg);
        int b = (int) Mth.lerp(t, sb, eb);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
