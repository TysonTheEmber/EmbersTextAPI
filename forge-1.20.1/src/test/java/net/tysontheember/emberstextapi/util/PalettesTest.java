package net.tysontheember.emberstextapi.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PalettesTest {

    @Test
    void twoPlainHexStopsGetZeroAndOne() {
        ColorPalette p = Palettes.parse("FF0000,00FF00", false, ColorPalette.SampleMode.CLAMP);
        assertEquals(2, p.size());
        assertArrayEquals(new float[]{1, 0, 0, 1}, p.sample(0f), 1e-5f);
        assertArrayEquals(new float[]{0, 1, 0, 1}, p.sample(1f), 1e-5f);
    }

    @Test
    void threePlainHexStopsEvenlySpaced() {
        ColorPalette p = Palettes.parse("FF0000,00FF00,0000FF", false, ColorPalette.SampleMode.CLAMP);
        assertEquals(3, p.size());
        assertArrayEquals(new float[]{0, 1, 0, 1}, p.sample(0.5f), 1e-5f);
    }

    @Test
    void positionedStopRespected() {
        ColorPalette p = Palettes.parse("FF0000,00FF00@0.2,0000FF", false, ColorPalette.SampleMode.CLAMP);
        assertArrayEquals(new float[]{0, 1, 0, 1}, p.sample(0.2f), 1e-5f);
    }

    @Test
    void alphaHexParsed() {
        ColorPalette p = Palettes.parse("FF000080", false, ColorPalette.SampleMode.CLAMP);
        float[] c = p.sample(0.5f);
        assertEquals(128f/255f, c[3], 1e-5f);
    }

    @Test
    void hashPrefixAcceptedPerEntry() {
        ColorPalette p = Palettes.parse("#FF0000,#00FF00", false, ColorPalette.SampleMode.CLAMP);
        assertEquals(2, p.size());
    }

    @Test
    void emptyInputReturnsFallbackWhite() {
        ColorPalette p = Palettes.parse("", false, ColorPalette.SampleMode.CLAMP);
        assertArrayEquals(new float[]{1, 1, 1, 1}, p.sample(0.5f), 1e-5f);
    }

    @Test
    void allInvalidEntriesFallbackToWhite() {
        ColorPalette p = Palettes.parse("ZZZZZZ,NOTHING", false, ColorPalette.SampleMode.CLAMP);
        assertArrayEquals(new float[]{1, 1, 1, 1}, p.sample(0.5f), 1e-5f);
    }

    @Test
    void invalidEntrySkipped() {
        ColorPalette p = Palettes.parse("FF0000,ZZZZZZ,0000FF", false, ColorPalette.SampleMode.CLAMP);
        assertEquals(2, p.size());
    }

    @Test
    void consecutiveUnpositionedSpreadBetweenPositionedAnchors() {
        ColorPalette p = Palettes.parse("FF0000@0,00FF00,00FF00,0000FF@1", false, ColorPalette.SampleMode.CLAMP);
        assertEquals(4, p.size());
    }
}
