package net.tysontheember.emberstextapi.immersivemessages.util;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class ColorParserRgbaTest {

    @Test
    void sixDigitHexReturnsFullAlpha() {
        Optional<float[]> r = ColorParser.parseToRgbaFloats("FF4E50");
        assertTrue(r.isPresent());
        assertArrayEquals(new float[]{1.0f, 78f/255f, 80f/255f, 1.0f}, r.get(), 1e-6f);
    }

    @Test
    void eightDigitHexReadsAlpha() {
        Optional<float[]> r = ColorParser.parseToRgbaFloats("FF4E5080");
        assertTrue(r.isPresent());
        assertArrayEquals(new float[]{1.0f, 78f/255f, 80f/255f, 128f/255f}, r.get(), 1e-6f);
    }

    @Test
    void threeDigitHexExpandsAndDefaultsAlpha() {
        Optional<float[]> r = ColorParser.parseToRgbaFloats("F00");
        assertTrue(r.isPresent());
        assertArrayEquals(new float[]{1.0f, 0.0f, 0.0f, 1.0f}, r.get(), 1e-6f);
    }

    @Test
    void hashPrefixStripped() {
        assertTrue(ColorParser.parseToRgbaFloats("#FF4E50").isPresent());
        assertTrue(ColorParser.parseToRgbaFloats("#FF4E5080").isPresent());
    }

    @Test
    void invalidInputReturnsEmpty() {
        assertTrue(ColorParser.parseToRgbaFloats(null).isEmpty());
        assertTrue(ColorParser.parseToRgbaFloats("ZZZZZZ").isEmpty());
        assertTrue(ColorParser.parseToRgbaFloats("FFFF").isEmpty());
        assertTrue(ColorParser.parseToRgbaFloats("").isEmpty());
    }
}
