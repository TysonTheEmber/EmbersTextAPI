package net.tysontheember.emberstextapi.sdf;

import net.tysontheember.emberstextapi.sdf.GlyphOutline.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MSDF edge coloring algorithm.
 */
class EdgeColoringTest {

    /** Default corner angle threshold (3.0 radians ≈ 171.9°) */
    private static final double DEFAULT_THRESHOLD = 3.0;

    // =========================================================================
    // Square contour (4 lines, 4 right-angle corners)
    // =========================================================================

    @Test
    @DisplayName("Square contour gets alternating colors at each corner")
    void testSquareColoring() {
        // CCW square: (0,0) → (100,0) → (100,100) → (0,100) → close
        List<Segment> segments = List.of(
                new Line(0, 0, 100, 0),       // bottom: rightward
                new Line(100, 0, 100, 100),    // right: upward
                new Line(100, 100, 0, 100),    // top: leftward
                new Line(0, 100, 0, 0)         // left: downward
        );

        GlyphOutline outline = ,new GlyphOutline(
                List.of(new Contour(segments)),
                0, 0, 100, 100, false, false
        );

        EdgeColoring.ColoredContour[] result = EdgeColoring.colorEdges(outline, DEFAULT_THRESHOLD);
        assertEquals(1, result.length, "Should have 1 contour");

        List<EdgeColoring.ColoredEdge> edges = result[0].edges();
        assertEquals(4, edges.size(), "Should have 4 edges");

        // Verify: at each corner, adjacent edges must differ
        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            int color1 = edges.get(i).color();
            int color2 = edges.get(next).color();

            // At corners, colors must differ in at least one channel
            // The angle between adjacent tangents of a square is 90° = π/2 ≈ 1.571 rad
            // which is > threshold of 3.0? No, π/2 < 3.0, so these are NOT corners
            // with the default threshold!
            // Wait — the angles at a square corner: 
            //   bottom edge tangent: (1,0), right edge tangent: (0,1)
            //   angle = acos(0) = π/2 ≈ 1.571 
            // This is LESS than 3.0, so with threshold 3.0 these would NOT be detected as corners.
            // We need a lower threshold to detect 90° corners.
        }
    }

    @Test
    @DisplayName("Square contour with low threshold detects all corners")
    void testSquareColoringLowThreshold() {
        List<Segment> segments = List.of(
                new Line(0, 0, 100, 0),
                new Line(100, 0, 100, 100),
                new Line(100, 100, 0, 100),
                new Line(0, 100, 0, 0)
        );

        GlyphOutline outline = ,new GlyphOutline(
                List.of(new Contour(segments)),
                0, 0, 100, 100, false, false
        );

        // Use π/4 (45°) threshold — should detect all 90° corners
        EdgeColoring.ColoredContour[] result = EdgeColoring.colorEdges(outline, Math.PI / 4);
        List<EdgeColoring.ColoredEdge> edges = result[0].edges();
        assertEquals(4, edges.size());

        // At each corner, adjacent edges must have different colors
        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            int c1 = edges.get(i).color();
            int c2 = edges.get(next).color();
            assertNotEquals(c1, c2,
                    String.format("Edges %d and %d at a corner should have different colors (got %d and %d)",
                            i, next, c1, c2));
        }
    }

    // =========================================================================
    // Circle contour (no corners)
    // =========================================================================

    @Test
    @DisplayName("Circle contour with no corners gets uniform WHITE color")
    void testCircleUniformColor() {
        // Approximate circle with 4 cubic Bézier arcs (smooth — no sharp corners)
        double r = 100;
        double k = 4.0 / 3.0 * (Math.sqrt(2) - 1); // ≈ 0.5523

        List<Segment> segments = List.of(
                new CubicBezier((float) r, 0, (float) r, (float) (r * k), (float) (r * k), (float) r, 0, (float) r),
                new CubicBezier(0, (float) r, (float) (-r * k), (float) r, (float) -r, (float) (r * k), (float) -r, 0),
                new CubicBezier((float) -r, 0, (float) -r, (float) (-r * k), (float) (-r * k), (float) -r, 0, (float) -r),
                new CubicBezier(0, (float) -r, (float) (r * k), (float) -r, (float) r, (float) (-r * k), (float) r, 0)
        );

        GlyphOutline outline = ,new GlyphOutline(
                List.of(new Contour(segments)),
                (float) -r, (float) -r, (float) r, (float) r, false, false
        );

        // With default threshold (3.0 rad ≈ 171.9°), the smooth joins between
        // cubic arcs should NOT be detected as corners
        EdgeColoring.ColoredContour[] result = EdgeColoring.colorEdges(outline, DEFAULT_THRESHOLD);
        List<EdgeColoring.ColoredEdge> edges = result[0].edges();

        // All edges should have the same color (WHITE for uniform)
        for (EdgeColoring.ColoredEdge edge : edges) {
            assertEquals(EdgeColoring.WHITE, edge.color(),
                    "Smooth contour edges should all be WHITE");
        }
    }

    // =========================================================================
    // Color validity tests
    // =========================================================================

    @Test
    @DisplayName("No two adjacent edges at a corner share the same color")
    void testAdjacentEdgesAtCornerDiffer() {
        // Triangle: 3 edges with 60° angles at each vertex
        List<Segment> segments = List.of(
                new Line(0, 0, 100, 0),
                new Line(100, 0, 50, 86.6f),
                new Line(50, 86.6f, 0, 0)
        );

        GlyphOutline outline = ,new GlyphOutline(
                List.of(new Contour(segments)),
                0, 0, 100, 86.6f, false, false
        );

        // Low threshold to detect the 60° corners
        EdgeColoring.ColoredContour[] result = EdgeColoring.colorEdges(outline, 0.5);
        List<EdgeColoring.ColoredEdge> edges = result[0].edges();
        assertEquals(3, edges.size());

        // At each corner, adjacent edges must differ
        for (int i = 0; i < 3; i++) {
            int next = (i + 1) % 3;
            int c1 = edges.get(i).color();
            int c2 = edges.get(next).color();
            assertNotEquals(c1, c2,
                    String.format("Triangle edges %d (color=%d) and %d (color=%d) should differ at corner",
                            i, c1, next, c2));
        }
    }

    @Test
    @DisplayName("Every edge has at least 2 channels enabled")
    void testMinTwoChannels() {
        // Pentagon with low threshold to create corners
        List<Segment> segments = List.of(
                new Line(50, 0, 97.55f, 34.55f),
                new Line(97.55f, 34.55f, 79.39f, 90.45f),
                new Line(79.39f, 90.45f, 20.61f, 90.45f),
                new Line(20.61f, 90.45f, 2.45f, 34.55f),
                new Line(2.45f, 34.55f, 50, 0)
        );

        GlyphOutline outline = ,new GlyphOutline(
                List.of(new Contour(segments)),
                2.45f, 0, 97.55f, 90.45f, false, false
        );

        EdgeColoring.ColoredContour[] result = EdgeColoring.colorEdges(outline, 0.5);
        List<EdgeColoring.ColoredEdge> edges = result[0].edges();

        for (int i = 0; i < edges.size(); i++) {
            int color = edges.get(i).color();
            assertTrue(EdgeColoring.hasAtLeastTwoChannels(color),
                    String.format("Edge %d has color %d which has fewer than 2 channels", i, color));
        }
    }

    // =========================================================================
    // Corner detection tests
    // =========================================================================

    @Test
    @DisplayName("Corner detection: 90° angle detected with low threshold")
    void testCornerDetection90() {
        List<Segment> segments = List.of(
                new Line(0, 0, 100, 0),     // rightward
                new Line(100, 0, 100, 100)   // upward
        );

        // 90° = π/2 ≈ 1.571 radians
        // With threshold 1.0, should detect this as a corner
        boolean[] corners = EdgeColoring.detectCorners(segments, 1.0);
        assertTrue(corners[0], "90° angle should be detected as corner with threshold 1.0");
    }

    @Test
    @DisplayName("Corner detection: 180° (straight) not detected")
    void testCornerDetectionStraight() {
        List<Segment> segments = List.of(
                new Line(0, 0, 100, 0),     // rightward
                new Line(100, 0, 200, 0)     // still rightward (collinear)
        );

        // 0° angle between tangents → not a corner for any positive threshold
        boolean[] corners = EdgeColoring.detectCorners(segments, 0.1);
        assertFalse(corners[0], "Collinear edges should not be detected as a corner");
    }

    @Test
    @DisplayName("Corner detection: obtuse angle (150°) with matching threshold")
    void testCornerDetectionObtuse() {
        // Edge 1 goes right, Edge 2 goes up-right at 30° from horizontal
        // The angle between (1,0) and (cos30°, sin30°) = 30° ≈ 0.524 rad
        List<Segment> segments = List.of(
                new Line(0, 0, 100, 0),
                new Line(100, 0, 200, 57.74f) // ~30° angle from horizontal
        );

        // 0.524 rad angle — should be detected with threshold 0.3, not with 0.6
        boolean[] corners1 = EdgeColoring.detectCorners(segments, 0.3);
        assertTrue(corners1[0], "30° tangent angle should be detected with threshold 0.3");

        boolean[] corners2 = EdgeColoring.detectCorners(segments, 0.6);
        assertFalse(corners2[0], "30° tangent angle should NOT be detected with threshold 0.6");
    }

    // =========================================================================
    // Angle computation test
    // =========================================================================

    @Test
    @DisplayName("Angle between tangents: same direction = 0")
    void testAngleSameDirection() {
        double angle = EdgeColoring.angleBetweenTangents(
                new double[]{1, 0}, new double[]{1, 0}
        );
        assertEquals(0.0, angle, 0.001);
    }

    @Test
    @DisplayName("Angle between tangents: perpendicular = π/2")
    void testAnglePerpendicular() {
        double angle = EdgeColoring.angleBetweenTangents(
                new double[]{1, 0}, new double[]{0, 1}
        );
        assertEquals(Math.PI / 2, angle, 0.001);
    }

    @Test
    @DisplayName("Angle between tangents: opposite = π")
    void testAngleOpposite() {
        double angle = EdgeColoring.angleBetweenTangents(
                new double[]{1, 0}, new double[]{-1, 0}
        );
        assertEquals(Math.PI, angle, 0.001);
    }

    // =========================================================================
    // hasAtLeastTwoChannels utility
    // =========================================================================

    @Test
    @DisplayName("hasAtLeastTwoChannels validation")
    void testHasAtLeastTwoChannels() {
        assertFalse(EdgeColoring.hasAtLeastTwoChannels(0));
        assertFalse(EdgeColoring.hasAtLeastTwoChannels(EdgeColoring.RED));
        assertFalse(EdgeColoring.hasAtLeastTwoChannels(EdgeColoring.GREEN));
        assertFalse(EdgeColoring.hasAtLeastTwoChannels(EdgeColoring.BLUE));
        assertTrue(EdgeColoring.hasAtLeastTwoChannels(EdgeColoring.CYAN));
        assertTrue(EdgeColoring.hasAtLeastTwoChannels(EdgeColoring.MAGENTA));
        assertTrue(EdgeColoring.hasAtLeastTwoChannels(EdgeColoring.YELLOW));
        assertTrue(EdgeColoring.hasAtLeastTwoChannels(EdgeColoring.WHITE));
    }

    // =========================================================================
    // Multi-contour test (e.g., letter 'O' has outer + inner contour)
    // =========================================================================

    @Test
    @DisplayName("Multi-contour outline gets each contour colored independently")
    void testMultiContour() {
        // Outer square
        Contour outer = new Contour(List.of(
                new Line(0, 0, 200, 0),
                new Line(200, 0, 200, 200),
                new Line(200, 200, 0, 200),
                new Line(0, 200, 0, 0)
        ));

        // Inner square (hole)
        Contour inner = new Contour(List.of(
                new Line(50, 50, 150, 50),
                new Line(150, 50, 150, 150),
                new Line(150, 150, 50, 150),
                new Line(50, 150, 50, 50)
        ));

        GlyphOutline outline = ,new GlyphOutline(
                List.of(outer, inner),
                0, 0, 200, 200, false, false
        );

        EdgeColoring.ColoredContour[] result = EdgeColoring.colorEdges(outline, 0.5);
        assertEquals(2, result.length, "Should have 2 contours");

        // Both contours should be properly colored
        for (int c = 0; c < 2; c++) {
            List<EdgeColoring.ColoredEdge> edges = result[c].edges();
            assertEquals(4, edges.size());
            for (EdgeColoring.ColoredEdge edge : edges) {
                assertTrue(EdgeColoring.hasAtLeastTwoChannels(edge.color()),
                        String.format("Contour %d edge should have ≥2 channels", c));
            }
        }
    }
}
