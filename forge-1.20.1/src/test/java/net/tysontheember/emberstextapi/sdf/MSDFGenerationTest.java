package net.tysontheember.emberstextapi.sdf;

import net.tysontheember.emberstextapi.sdf.GlyphOutline.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MSDF texture generation (Phase 3).
 * Verifies the complete pipeline: outline → edge coloring → MSDF texture.
 */
class MSDFGenerationTest {

    @Test
    @DisplayName("Generate MSDF for square outline - correct dimensions")
    void testSquareMSDFDimensions() {
        GlyphOutline outline = makeSquareOutline(0, 0, 100, 100);
        EdgeColoring.ColoredContour[] colored = EdgeColoring.colorEdges(outline, Math.PI / 4);

        int w = 32, h = 32;
        byte[] msdf = MSDFGenerator.generate(outline, colored, w, h,
                0, 0, 100, 100, 4.0);

        assertEquals(w * h * 3, msdf.length, "MSDF should have 3 bytes per pixel");
    }

    @Test
    @DisplayName("Generate MSDF for square - center is inside (> 128)")
    void testSquareCenterInside() {
        GlyphOutline outline = makeSquareOutline(0, 0, 100, 100);
        EdgeColoring.ColoredContour[] colored = EdgeColoring.colorEdges(outline, Math.PI / 4);

        int w = 32, h = 32;
        byte[] msdf = MSDFGenerator.generate(outline, colored, w, h,
                0, 0, 100, 100, 4.0);

        // Center pixel should be "inside" in all channels
        int cx = w / 2, cy = h / 2;
        int idx = (cy * w + cx) * 3;
        int r = msdf[idx] & 0xFF;
        int g = msdf[idx + 1] & 0xFF;
        int b = msdf[idx + 2] & 0xFF;
        float median = MSDFGenerator.median(r, g, b);

        assertTrue(median > 128,
                String.format("Center of square should be inside (median=%.1f, RGB=%d,%d,%d)", median, r, g, b));
    }

    @Test
    @DisplayName("Generate MSDF for square - corner pixel is outside (< 128)")
    void testSquareCornerOutside() {
        GlyphOutline outline = makeSquareOutline(0, 0, 100, 100);
        EdgeColoring.ColoredContour[] colored = EdgeColoring.colorEdges(outline, Math.PI / 4);

        int w = 32, h = 32;
        byte[] msdf = MSDFGenerator.generate(outline, colored, w, h,
                0, 0, 100, 100, 4.0);

        // Corner pixel (0,0) should be "outside"
        int idx = 0;
        int r = msdf[idx] & 0xFF;
        int g = msdf[idx + 1] & 0xFF;
        int b = msdf[idx + 2] & 0xFF;
        float median = MSDFGenerator.median(r, g, b);

        assertTrue(median < 128,
                String.format("Corner of texture should be outside (median=%.1f, RGB=%d,%d,%d)", median, r, g, b));
    }

    @Test
    @DisplayName("Generate MSDF for triangle - median transitions from outside to inside")
    void testTriangleMSDFTransition() {
        // CCW triangle
        List<Segment> segments = List.of(
                new Line(0, 0, 100, 0),
                new Line(100, 0, 50, 86.6f),
                new Line(50, 86.6f, 0, 0)
        );
        GlyphOutline outline = new GlyphOutline(
                List.of(new Contour(segments)),
                0, 0, 100, 86.6f, false
        );
        EdgeColoring.ColoredContour[] colored = EdgeColoring.colorEdges(outline, 0.5);

        int w = 32, h = 32;
        byte[] msdf = MSDFGenerator.generate(outline, colored, w, h,
                0, 0, 100, 86.6, 4.0);

        // Sample along a horizontal line through the middle
        int midY = h / 2;
        boolean foundInside = false;
        boolean foundOutside = false;
        for (int x = 0; x < w; x++) {
            int idx = (midY * w + x) * 3;
            int r = msdf[idx] & 0xFF;
            int g = msdf[idx + 1] & 0xFF;
            int b = msdf[idx + 2] & 0xFF;
            float median = MSDFGenerator.median(r, g, b);
            if (median > 128) foundInside = true;
            if (median < 128) foundOutside = true;
        }

        assertTrue(foundInside, "Should find inside pixels along horizontal midline");
        assertTrue(foundOutside, "Should find outside pixels along horizontal midline");
    }

    @Test
    @DisplayName("MSDF channels differ at corners (not all same value)")
    void testMSDFChannelsDifferAtCorners() {
        GlyphOutline outline = makeSquareOutline(0, 0, 100, 100);
        EdgeColoring.ColoredContour[] colored = EdgeColoring.colorEdges(outline, Math.PI / 4);

        int w = 48, h = 48;
        byte[] msdf = MSDFGenerator.generate(outline, colored, w, h,
                0, 0, 100, 100, 4.0);

        // At corners of the square, the RGB channels should diverge (MSDF's key feature)
        // Sample near where the square corner would be in texture space
        // The square occupies roughly the center 80% of the texture, so corner is around
        // pixel (w*0.15, h*0.85) for bottom-left corner
        boolean foundDivergent = false;
        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                int idx = (py * w + px) * 3;
                int r = msdf[idx] & 0xFF;
                int g = msdf[idx + 1] & 0xFF;
                int b = msdf[idx + 2] & 0xFF;
                // Check if channels diverge significantly (> 10 difference)
                int maxDiff = Math.max(Math.abs(r - g), Math.max(Math.abs(g - b), Math.abs(r - b)));
                if (maxDiff > 10) {
                    foundDivergent = true;
                    break;
                }
            }
            if (foundDivergent) break;
        }

        assertTrue(foundDivergent, "MSDF should have divergent RGB channels near corners");
    }

    @Test
    @DisplayName("MSDF for circle has mostly uniform channels (no sharp corners)")
    void testCircleMSDFUniformChannels() {
        double r = 100;
        double k = 4.0 / 3.0 * (Math.sqrt(2) - 1);
        List<Segment> segments = List.of(
                new CubicBezier((float) r, 0, (float) r, (float) (r * k), (float) (r * k), (float) r, 0, (float) r),
                new CubicBezier(0, (float) r, (float) (-r * k), (float) r, (float) -r, (float) (r * k), (float) -r, 0),
                new CubicBezier((float) -r, 0, (float) -r, (float) (-r * k), (float) (-r * k), (float) -r, 0, (float) -r),
                new CubicBezier(0, (float) -r, (float) (r * k), (float) -r, (float) r, (float) (-r * k), (float) r, 0)
        );
        GlyphOutline outline = new GlyphOutline(
                List.of(new Contour(segments)),
                (float) -r, (float) -r, (float) r, (float) r, false
        );
        EdgeColoring.ColoredContour[] colored = EdgeColoring.colorEdges(outline, 3.0);

        int w = 32, h = 32;
        byte[] msdf = MSDFGenerator.generate(outline, colored, w, h,
                -r, -r, r, r, 4.0);

        // With WHITE coloring (no corners), all channels should be very similar
        int divergentCount = 0;
        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                int idx = (py * w + px) * 3;
                int rv = msdf[idx] & 0xFF;
                int gv = msdf[idx + 1] & 0xFF;
                int bv = msdf[idx + 2] & 0xFF;
                int maxDiff = Math.max(Math.abs(rv - gv), Math.max(Math.abs(gv - bv), Math.abs(rv - bv)));
                if (maxDiff > 5) divergentCount++;
            }
        }

        // Allow very few divergent pixels (error correction artifacts)
        assertTrue(divergentCount < w * h * 0.05,
                "Circle MSDF should have mostly uniform channels, but found " + divergentCount + " divergent pixels");
    }

    @Test
    @DisplayName("MSDF no stray bright pixels in outside region")
    void testNoStrayBrightPixels() {
        GlyphOutline outline = makeSquareOutline(0, 0, 100, 100);
        EdgeColoring.ColoredContour[] colored = EdgeColoring.colorEdges(outline, Math.PI / 4);

        int w = 48, h = 48;
        byte[] msdf = MSDFGenerator.generate(outline, colored, w, h,
                0, 0, 100, 100, 4.0);

        // Check the border rows/columns — these should be solidly "outside"
        // (all channels < 128) since they're well outside the glyph
        for (int x = 0; x < w; x++) {
            // Top row
            {
                int idx = x * 3;
                int r = msdf[idx] & 0xFF;
                int g = msdf[idx + 1] & 0xFF;
                int b = msdf[idx + 2] & 0xFF;
                float median = MSDFGenerator.median(r, g, b);
                assertTrue(median < 128,
                        String.format("Top border pixel (%d,0) should be outside (median=%.1f)", x, median));
            }
            // Bottom row
            {
                int idx = ((h - 1) * w + x) * 3;
                int r = msdf[idx] & 0xFF;
                int g = msdf[idx + 1] & 0xFF;
                int b = msdf[idx + 2] & 0xFF;
                float median = MSDFGenerator.median(r, g, b);
                assertTrue(median < 128,
                        String.format("Bottom border pixel (%d,%d) should be outside (median=%.1f)", x, h - 1, median));
            }
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private static GlyphOutline makeSquareOutline(float x0, float y0, float x1, float y1) {
        List<Segment> segments = List.of(
                new Line(x0, y0, x1, y0),
                new Line(x1, y0, x1, y1),
                new Line(x1, y1, x0, y1),
                new Line(x0, y1, x0, y0)
        );
        return new GlyphOutline(
                List.of(new Contour(segments)),
                x0, y0, x1, y1, false
        );
    }
}
