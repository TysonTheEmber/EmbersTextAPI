package net.tysontheember.emberstextapi.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ColorMath} color conversion and interpolation utilities.
 */
class ColorMathTest {

    private static final float EPSILON = 0.001f; // Tolerance for floating-point comparisons

    @Test
    void testRgbToHsvPureRed() {
        float[] hsv = ColorMath.rgbToHsv(1.0f, 0.0f, 0.0f);
        assertEquals(0.0f, hsv[0], EPSILON, "Hue should be 0 for pure red");
        assertEquals(1.0f, hsv[1], EPSILON, "Saturation should be 1");
        assertEquals(1.0f, hsv[2], EPSILON, "Value should be 1");
    }

    @Test
    void testRgbToHsvPureGreen() {
        float[] hsv = ColorMath.rgbToHsv(0.0f, 1.0f, 0.0f);
        assertEquals(1.0f / 3.0f, hsv[0], EPSILON, "Hue should be ~0.333 for pure green");
        assertEquals(1.0f, hsv[1], EPSILON, "Saturation should be 1");
        assertEquals(1.0f, hsv[2], EPSILON, "Value should be 1");
    }

    @Test
    void testRgbToHsvPureBlue() {
        float[] hsv = ColorMath.rgbToHsv(0.0f, 0.0f, 1.0f);
        assertEquals(2.0f / 3.0f, hsv[0], EPSILON, "Hue should be ~0.667 for pure blue");
        assertEquals(1.0f, hsv[1], EPSILON, "Saturation should be 1");
        assertEquals(1.0f, hsv[2], EPSILON, "Value should be 1");
    }

    @Test
    void testRgbToHsvGray() {
        float[] hsv = ColorMath.rgbToHsv(0.5f, 0.5f, 0.5f);
        assertEquals(0.0f, hsv[0], EPSILON, "Hue should be 0 for achromatic color");
        assertEquals(0.0f, hsv[1], EPSILON, "Saturation should be 0 for gray");
        assertEquals(0.5f, hsv[2], EPSILON, "Value should be 0.5");
    }

    @Test
    void testHsvToRgbPureRed() {
        float[] rgb = ColorMath.hsvToRgb(0.0f, 1.0f, 1.0f);
        assertEquals(1.0f, rgb[0], EPSILON, "Red component should be 1");
        assertEquals(0.0f, rgb[1], EPSILON, "Green component should be 0");
        assertEquals(0.0f, rgb[2], EPSILON, "Blue component should be 0");
    }

    @Test
    void testHsvToRgbPureGreen() {
        float[] rgb = ColorMath.hsvToRgb(1.0f / 3.0f, 1.0f, 1.0f);
        assertEquals(0.0f, rgb[0], EPSILON, "Red component should be 0");
        assertEquals(1.0f, rgb[1], EPSILON, "Green component should be 1");
        assertEquals(0.0f, rgb[2], EPSILON, "Blue component should be 0");
    }

    @Test
    void testHsvToRgbPureBlue() {
        float[] rgb = ColorMath.hsvToRgb(2.0f / 3.0f, 1.0f, 1.0f);
        assertEquals(0.0f, rgb[0], EPSILON, "Red component should be 0");
        assertEquals(0.0f, rgb[1], EPSILON, "Green component should be 0");
        assertEquals(1.0f, rgb[2], EPSILON, "Blue component should be 1");
    }

    @Test
    void testHsvToRgbRoundTrip() {
        // Test that RGB -> HSV -> RGB preserves the original color
        float[] originalRgb = {0.7f, 0.3f, 0.9f};
        float[] hsv = ColorMath.rgbToHsv(originalRgb);
        float[] roundTripRgb = ColorMath.hsvToRgb(hsv[0], hsv[1], hsv[2]);

        assertEquals(originalRgb[0], roundTripRgb[0], EPSILON, "Red should be preserved");
        assertEquals(originalRgb[1], roundTripRgb[1], EPSILON, "Green should be preserved");
        assertEquals(originalRgb[2], roundTripRgb[2], EPSILON, "Blue should be preserved");
    }

    @Test
    void testHsvToRgbPacked() {
        // Pure red: HSV(0, 1, 1) -> RGB(255, 0, 0) -> 0xFF0000
        int packed = ColorMath.hsvToRgbPacked(0.0f, 1.0f, 1.0f);
        assertEquals(0xFF0000, packed, "Packed red should be 0xFF0000");

        // Pure green: HSV(1/3, 1, 1) -> RGB(0, 255, 0) -> 0x00FF00
        packed = ColorMath.hsvToRgbPacked(1.0f / 3.0f, 1.0f, 1.0f);
        assertEquals(0x00FF00, packed, "Packed green should be 0x00FF00");

        // Pure blue: HSV(2/3, 1, 1) -> RGB(0, 0, 255) -> 0x0000FF
        packed = ColorMath.hsvToRgbPacked(2.0f / 3.0f, 1.0f, 1.0f);
        assertEquals(0x0000FF, packed, "Packed blue should be 0x0000FF");
    }

    @Test
    void testLerp() {
        assertEquals(0.0f, ColorMath.lerp(0.0f, 10.0f, 0.0f), EPSILON, "t=0 should return start value");
        assertEquals(5.0f, ColorMath.lerp(0.0f, 10.0f, 0.5f), EPSILON, "t=0.5 should return midpoint");
        assertEquals(10.0f, ColorMath.lerp(0.0f, 10.0f, 1.0f), EPSILON, "t=1 should return end value");
        assertEquals(7.5f, ColorMath.lerp(5.0f, 10.0f, 0.5f), EPSILON, "Lerp should work with non-zero start");
    }

    @Test
    void testLerpHueShortestPath() {
        // Test hue interpolation takes shortest path around the color wheel
        // From red (0.0) to yellow (0.17) should go forward
        float result = ColorMath.lerpHue(0.0f, 0.17f, 0.5f);
        assertEquals(0.085f, result, EPSILON, "Hue midpoint should be halfway between 0 and 0.17");

        // From red (0.95) to red (0.05) - should wrap around through 0
        result = ColorMath.lerpHue(0.95f, 0.05f, 0.5f);
        // Shortest path: 0.95 -> 1.0 -> 0.0 -> 0.05 (distance 0.1)
        // Midpoint should be at 0.0
        assertTrue(result < 0.05f || result > 0.95f, "Hue should wrap around 0");

        // Simple forward interpolation
        result = ColorMath.lerpHue(0.2f, 0.4f, 0.5f);
        assertEquals(0.3f, result, EPSILON, "Simple forward interpolation");
    }

    @Test
    void testLerpRgbViaHsv() {
        float[] red = {1.0f, 0.0f, 0.0f};
        float[] blue = {0.0f, 0.0f, 1.0f};

        // Midpoint between red and blue in HSV space
        float[] result = ColorMath.lerpRgbViaHsv(red, blue, 0.5f);

        // Should produce a purple-ish color (exact value depends on HSV interpolation)
        assertTrue(result[0] > 0.0f, "Should have some red component");
        assertTrue(result[2] > 0.0f, "Should have some blue component");

        // t=0 should return start color
        result = ColorMath.lerpRgbViaHsv(red, blue, 0.0f);
        assertArrayEquals(red, result, EPSILON, "t=0 should return start color");

        // t=1 should return end color
        result = ColorMath.lerpRgbViaHsv(red, blue, 1.0f);
        assertArrayEquals(blue, result, EPSILON, "t=1 should return end color");
    }

    @Test
    void testClampFloat() {
        assertEquals(0.0f, ColorMath.clamp(-5.0f, 0.0f, 10.0f), EPSILON, "Should clamp to min");
        assertEquals(10.0f, ColorMath.clamp(15.0f, 0.0f, 10.0f), EPSILON, "Should clamp to max");
        assertEquals(5.0f, ColorMath.clamp(5.0f, 0.0f, 10.0f), EPSILON, "Should not clamp value in range");
    }

    @Test
    void testClampInt() {
        assertEquals(0, ColorMath.clamp(-5, 0, 10), "Should clamp to min");
        assertEquals(10, ColorMath.clamp(15, 0, 10), "Should clamp to max");
        assertEquals(5, ColorMath.clamp(5, 0, 10), "Should not clamp value in range");
    }

    @Test
    void testNormalize() {
        // Map value from [0, 100] to [0, 1]
        assertEquals(0.0f, ColorMath.normalize(0.0f, 0.0f, 100.0f, 0.0f, 1.0f), EPSILON);
        assertEquals(0.5f, ColorMath.normalize(50.0f, 0.0f, 100.0f, 0.0f, 1.0f), EPSILON);
        assertEquals(1.0f, ColorMath.normalize(100.0f, 0.0f, 100.0f, 0.0f, 1.0f), EPSILON);

        // Map value from [0, 10] to [100, 200]
        assertEquals(100.0f, ColorMath.normalize(0.0f, 0.0f, 10.0f, 100.0f, 200.0f), EPSILON);
        assertEquals(150.0f, ColorMath.normalize(5.0f, 0.0f, 10.0f, 100.0f, 200.0f), EPSILON);
        assertEquals(200.0f, ColorMath.normalize(10.0f, 0.0f, 10.0f, 100.0f, 200.0f), EPSILON);
    }

    private static void assertArrayEquals(float[] expected, float[] actual, float delta, String message) {
        assertEquals(expected.length, actual.length, message + " (length mismatch)");
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i], delta, message + " (index " + i + ")");
        }
    }
}
