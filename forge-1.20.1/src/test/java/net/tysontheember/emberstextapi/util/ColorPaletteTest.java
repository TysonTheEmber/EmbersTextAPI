package net.tysontheember.emberstextapi.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ColorPaletteTest {

    private static float[] rgba(float r, float g, float b, float a) {
        return new float[]{r, g, b, a};
    }

    @Test
    void singleStopReturnsThatColor() {
        ColorPalette p = new ColorPalette(
                new float[][]{rgba(1, 0, 0, 1)},
                new float[]{0f},
                false, ColorPalette.SampleMode.CLAMP);
        assertArrayEquals(new float[]{1, 0, 0, 1}, p.sample(0.0f), 1e-5f);
        assertArrayEquals(new float[]{1, 0, 0, 1}, p.sample(0.7f), 1e-5f);
    }

    @Test
    void twoStopLinearInterp() {
        ColorPalette p = new ColorPalette(
                new float[][]{rgba(1, 0, 0, 1), rgba(0, 0, 1, 1)},
                new float[]{0f, 1f},
                false, ColorPalette.SampleMode.CLAMP);
        assertArrayEquals(new float[]{0.5f, 0, 0.5f, 1}, p.sample(0.5f), 1e-5f);
    }

    @Test
    void clampModeLocksAtEnds() {
        ColorPalette p = new ColorPalette(
                new float[][]{rgba(1, 0, 0, 1), rgba(0, 1, 0, 1)},
                new float[]{0f, 1f},
                false, ColorPalette.SampleMode.CLAMP);
        assertArrayEquals(new float[]{1, 0, 0, 1}, p.sample(-0.3f), 1e-5f);
        assertArrayEquals(new float[]{0, 1, 0, 1}, p.sample(1.8f), 1e-5f);
    }

    @Test
    void wrapModeModulo1() {
        ColorPalette p = new ColorPalette(
                new float[][]{rgba(1, 0, 0, 1), rgba(0, 1, 0, 1)},
                new float[]{0f, 1f},
                false, ColorPalette.SampleMode.WRAP);
        assertArrayEquals(p.sample(0.25f), p.sample(1.25f), 1e-5f);
        assertArrayEquals(p.sample(0.75f), p.sample(-0.25f), 1e-5f);
    }

    @Test
    void pingPongModeFoldsBack() {
        ColorPalette p = new ColorPalette(
                new float[][]{rgba(1, 0, 0, 1), rgba(0, 1, 0, 1)},
                new float[]{0f, 1f},
                false, ColorPalette.SampleMode.PINGPONG);
        assertArrayEquals(p.sample(0.3f), p.sample(1.7f), 1e-5f);
        assertArrayEquals(p.sample(0.8f), p.sample(1.2f), 1e-5f);
    }

    @Test
    void positionedStopsInterpBetweenNeighbors() {
        ColorPalette p = new ColorPalette(
                new float[][]{rgba(1, 0, 0, 1), rgba(0, 1, 0, 1), rgba(0, 0, 1, 1)},
                new float[]{0f, 0.2f, 1f},
                false, ColorPalette.SampleMode.CLAMP);
        float[] mid = p.sample(0.1f);
        assertEquals(0.5f, mid[0], 1e-5f);
        assertEquals(0.5f, mid[1], 1e-5f);
    }
}
