package net.tysontheember.emberstextapi.sdf;

import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;

public class SDFSheetGlyphInfo implements SheetGlyphInfo {

    private static final int ATLAS_PAD = 2;

    private final byte[] msdfData;
    private final int width;
    private final int height;
    private final float bearingLeft;
    private final float bearingTop;
    private final float oversample;

    public SDFSheetGlyphInfo(byte[] msdfData, int width, int height,
                              float bearingLeft, float bearingTop, float oversample) {
        this.msdfData = msdfData;
        this.width = width;
        this.height = height;
        this.bearingLeft = bearingLeft;
        this.bearingTop = bearingTop;
        this.oversample = oversample;
    }

    @Override
    public int getPixelWidth() {
        return width + 2 * ATLAS_PAD;
    }

    @Override
    public int getPixelHeight() {
        return height + 2 * ATLAS_PAD;
    }

    @Override
    public float getOversample() {
        return oversample;
    }

    @Override
    public float getBearingX() {
        return bearingLeft - ATLAS_PAD / oversample;
    }

    @Override
    public float getBearingY() {
        return bearingTop - ATLAS_PAD / oversample;
    }

    @Override
    public boolean isColored() {

        return true;
    }

    @Override
    public void upload(int x, int y) {
        int paddedW = width + 2 * ATLAS_PAD;
        int paddedH = height + 2 * ATLAS_PAD;
        NativeImage image = new NativeImage(NativeImage.Format.RGBA, paddedW, paddedH, false);
        try {

            for (int py = 0; py < paddedH; py++) {
                int srcY = Math.max(0, Math.min(py - ATLAS_PAD, height - 1));
                for (int px = 0; px < paddedW; px++) {
                    int srcX = Math.max(0, Math.min(px - ATLAS_PAD, width - 1));
                    int idx = (srcY * width + srcX) * 3;
                    int r = idx < msdfData.length ? (msdfData[idx] & 0xFF) : 0;
                    int g = idx + 1 < msdfData.length ? (msdfData[idx + 1] & 0xFF) : 0;
                    int b = idx + 2 < msdfData.length ? (msdfData[idx + 2] & 0xFF) : 0;

                    int pixel = 0xFF000000 | (b << 16) | (g << 8) | r;
                    image.setPixelRGBA(px, py, pixel);
                }
            }

            image.upload(0, x, y, 0, 0, paddedW, paddedH, true, false);
        } finally {
            image.close();
        }
    }
}
