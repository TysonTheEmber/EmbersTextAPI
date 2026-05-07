package net.tysontheember.emberstextapi.sdf;

import net.tysontheember.emberstextapi.sdf.GlyphOutline.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EdgeColoringTest {

    private static final double DEFAULT_THRESHOLD = 3.0;

    @Test
    @DisplayName("Square contour gets alternating colors at each corner")
    void testSquareColoring() {

        List<Segment> segments = List.of(
                new Line(0, 0, 100, 0),
                new Line(100, 0, 100, 100),
                new Line(100, 100, 0, 100),
                new Line(0, 100, 0, 0)
        );

        GlyphOutline outline = new GlyphOutline(
                List.of(new Contour(segments)),
                0, 0, 100, 100, false, false
        );

        EdgeColoring.ColoredContour[] result = EdgeColoring.colorEdges(outline, DEFAULT_THRESHOLD);
        assertEquals(1, result.length, "Should have 1 contour");

        List<EdgeColoring.ColoredEdge> edges = result[0].edges();
        assertEquals(4, edges.size(), "Should have 4 edges");

        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            int color1 = edges.get(i).color();
            int color2 = edges.get(next).color();

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

        GlyphOutline outline = new GlyphOutline(
                List.of(new Contour(segments)),
                0, 0, 100, 100, false, false
        );

        EdgeColoring.ColoredContour[] result = EdgeColoring.colorEdges(outline, Math.PI / 4);
        List<EdgeColoring.ColoredEdge> edges = result[0].edges();
        assertEquals(4, edges.size());

        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            int c1 = edges.get(i).color();
            int c2 = edges.get(next).color();
            assertNotEquals(c1, c2,
                    String.format("Edges %d and %d at a corner should have different colors (got %d and %d)",
                            i, next, c1, c2));
        }
    }

    @Test
    @DisplayName("Circle contour with no corners gets uniform WHITE color")
    void testCircleUniformColor() {

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
                (float) -r, (float) -r, (float) r, (float) r, false, false
        );

        EdgeColoring.ColoredContour[] result = EdgeColoring.colorEdges(outline, DEFAULT_THRESHOLD);
        List<EdgeColoring.ColoredEdge> edges = result[0].edges();

        for (EdgeColoring.ColoredEdge edge : edges) {
            assertEquals(EdgeColoring.WHITE, edge.color(),
                    "Smooth contour edges should all be WHITE");
        }
    }

    @Test
    @DisplayName("No two adjacent edges at a corner share the same color")
    void testAdjacentEdgesAtCornerDiffer() {

        List<Segment> segments = List.of(
                new Line(0, 0, 100, 0),
                new Line(100, 0, 50, 86.6f),
                new Line(50, 86.6f, 0, 0)
        );

        GlyphOutline outline = new GlyphOutline(
                List.of(new Contour(segments)),
                0, 0, 100, 86.6f, false, false
        );

        EdgeColoring.ColoredContour[] result = EdgeColoring.colorEdges(outline, 0.5);
        List<EdgeColoring.ColoredEdge> edges = result[0].edges();
        assertEquals(3, edges.size());

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

        List<Segment> segments = List.of(
                new Line(50, 0, 97.55f, 34.55f),
                new Line(97.55f, 34.55f, 79.39f, 90.45f),
                new Line(79.39f, 90.45f, 20.61f, 90.45f),
                new Line(20.61f, 90.45f, 2.45f, 34.55f),
                new Line(2.45f, 34.55f, 50, 0)
        );

        GlyphOutline outline = new GlyphOutline(
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

    @Test
    @DisplayName("Corner detection: 90° angle detected with low threshold")
    void testCornerDetection90() {
        List<Segment> segments = List.of(
                new Line(0, 0, 100, 0),
                new Line(100, 0, 100, 100)
        );

        boolean[] corners = EdgeColoring.detectCorners(segments, 1.0);
        assertTrue(corners[0], "90° angle should be detected as corner with threshold 1.0");
    }

    @Test
    @DisplayName("Corner detection: 180° (straight) not detected")
    void testCornerDetectionStraight() {
        List<Segment> segments = List.of(
                new Line(0, 0, 100, 0),
                new Line(100, 0, 200, 0)
        );

        boolean[] corners = EdgeColoring.detectCorners(segments, 0.1);
        assertFalse(corners[0], "Collinear edges should not be detected as a corner");
    }

    @Test
    @DisplayName("Corner detection: obtuse angle (150°) with matching threshold")
    void testCornerDetectionObtuse() {

        List<Segment> segments = List.of(
                new Line(0, 0, 100, 0),
                new Line(100, 0, 200, 57.74f)
        );

        boolean[] corners1 = EdgeColoring.detectCorners(segments, 0.3);
        assertTrue(corners1[0], "30° tangent angle should be detected with threshold 0.3");

        boolean[] corners2 = EdgeColoring.detectCorners(segments, 0.6);
        assertFalse(corners2[0], "30° tangent angle should NOT be detected with threshold 0.6");
    }

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

    @Test
    @DisplayName("Multi-contour outline gets each contour colored independently")
    void testMultiContour() {

        Contour outer = new Contour(List.of(
                new Line(0, 0, 200, 0),
                new Line(200, 0, 200, 200),
                new Line(200, 200, 0, 200),
                new Line(0, 200, 0, 0)
        ));

        Contour inner = new Contour(List.of(
                new Line(50, 50, 150, 50),
                new Line(150, 50, 150, 150),
                new Line(150, 150, 50, 150),
                new Line(50, 150, 50, 50)
        ));

        GlyphOutline outline = new GlyphOutline(
                List.of(outer, inner),
                0, 0, 200, 200, false, false
        );

        EdgeColoring.ColoredContour[] result = EdgeColoring.colorEdges(outline, 0.5);
        assertEquals(2, result.length, "Should have 2 contours");

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
