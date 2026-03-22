package net.tysontheember.emberstextapi.sdf;

import net.tysontheember.emberstextapi.sdf.GlyphOutline.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MSDF analytical distance computation engine.
 */
class MSDFGeneratorTest {

    private static final double EPSILON = 0.01;

    // =========================================================================
    // Line segment distance tests
    // =========================================================================

    @Test
    @DisplayName("Distance to horizontal line segment - point above")
    void testLineDistanceAbove() {
        Line line = new Line(0, 0, 10, 0);
        MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToLine(5, 3, line);
        assertEquals(3.0, dr.distance(), EPSILON);
        // Point is above the line (left of rightward tangent) → positive cross
        assertTrue(dr.dot() > 0, "Point above horizontal line should have positive cross product");
    }

    @Test
    @DisplayName("Distance to horizontal line segment - point below")
    void testLineDistanceBelow() {
        Line line = new Line(0, 0, 10, 0);
        MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToLine(5, -3, line);
        assertEquals(3.0, dr.distance(), EPSILON);
        assertTrue(dr.dot() < 0, "Point below horizontal line should have negative cross product");
    }

    @Test
    @DisplayName("Distance to line segment - point at endpoint")
    void testLineDistanceEndpoint() {
        Line line = new Line(0, 0, 10, 0);
        MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToLine(12, 0, line);
        assertEquals(2.0, dr.distance(), EPSILON);
    }

    @Test
    @DisplayName("Distance to line segment - point on segment")
    void testLineDistanceOnSegment() {
        Line line = new Line(0, 0, 10, 0);
        MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToLine(5, 0, line);
        assertEquals(0.0, dr.distance(), EPSILON);
    }

    @Test
    @DisplayName("Distance to diagonal line segment")
    void testLineDiagonal() {
        Line line = new Line(0, 0, 10, 10);
        // Point at (0, 10) - perpendicular distance to the 45° line y=x
        // Distance = |0-10| / sqrt(2) = 10/sqrt(2) ≈ 7.071... but clamped to endpoint (0,0) or (10,10)
        // Actually closest point on segment: t = ((0-0)*10 + (10-0)*10) / (100+100) = 100/200 = 0.5
        // closest = (5, 5), dist = sqrt(25 + 25) = sqrt(50) ≈ 7.071
        MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToLine(0, 10, line);
        assertEquals(Math.sqrt(50), dr.distance(), EPSILON);
    }

    // =========================================================================
    // Quadratic Bézier distance tests
    // =========================================================================

    @Test
    @DisplayName("Distance to quadratic Bézier - point on curve")
    void testQuadBezierOnCurve() {
        // Straight quadratic Bézier (control point on the line)
        QuadBezier q = new QuadBezier(0, 0, 5, 0, 10, 0);
        MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToQuadBezier(5, 0, q);
        assertEquals(0.0, dr.distance(), EPSILON);
    }

    @Test
    @DisplayName("Distance to quadratic Bézier - point above parabola")
    void testQuadBezierAbove() {
        // Parabolic arc: P0=(0,0), P1=(5,10), P2=(10,0)
        // The midpoint of the curve at t=0.5 is at (5, 5)
        QuadBezier q = new QuadBezier(0, 0, 5, 10, 10, 0);

        // Point directly above the apex
        MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToQuadBezier(5, 6, q);
        // At t=0.5: B(0.5) = (5, 5), so distance should be ~1.0
        assertEquals(1.0, dr.distance(), EPSILON);
    }

    @Test
    @DisplayName("Distance to quadratic Bézier - point at endpoint")
    void testQuadBezierEndpoint() {
        QuadBezier q = new QuadBezier(0, 0, 5, 10, 10, 0);
        MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToQuadBezier(0, 0, q);
        assertEquals(0.0, dr.distance(), EPSILON);
    }

    // =========================================================================
    // Cubic Bézier distance tests
    // =========================================================================

    @Test
    @DisplayName("Distance to cubic Bézier - point on curve")
    void testCubicBezierOnCurve() {
        // Straight cubic
        CubicBezier c = new CubicBezier(0, 0, 3, 0, 7, 0, 10, 0);
        MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToCubicBezier(5, 0, c);
        assertEquals(0.0, dr.distance(), EPSILON);
    }

    @Test
    @DisplayName("Distance to cubic Bézier - S-curve")
    void testCubicBezierSCurve() {
        // S-curve: P0=(0,0), P1=(0,10), P2=(10,-10), P3=(10,0)
        CubicBezier c = new CubicBezier(0, 0, 0, 10, 10, -10, 10, 0);
        // At t=0.5: B(0.5) = (5, 0) (by symmetry of this S-curve)
        // midX = 0.125*0 + 0.375*0 + 0.375*10 + 0.125*10 = 5
        // midY = 0.125*0 + 0.375*10 + 0.375*(-10) + 0.125*0 = 0
        MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToCubicBezier(5, 0, c);
        assertEquals(0.0, dr.distance(), EPSILON);
    }

    @Test
    @DisplayName("Distance to cubic Bézier - point near curve")
    void testCubicBezierNear() {
        CubicBezier c = new CubicBezier(0, 0, 3, 0, 7, 0, 10, 0);
        MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToCubicBezier(5, 2, c);
        assertEquals(2.0, dr.distance(), EPSILON);
    }

    // =========================================================================
    // Circle approximation test (4 quadratic arcs)
    // =========================================================================

    @Test
    @DisplayName("Distance to circle approximated by 4 quadratic arcs")
    void testCircleApproximation() {
        // Approximate a circle of radius 100 centered at origin using 4 quadratic Bézier arcs.
        // Each arc spans 90°. The control point for a quarter-circle quadratic Bézier is at
        // distance r * (4/3) * tan(π/8) ≈ r * 0.5523 from the endpoints.
        // Using the standard Bézier circle approximation with quadratic curves:
        // k = 4/3 * (sqrt(2) - 1) ≈ 0.5523 for cubic, but for quadratic arcs,
        // we use a simpler approach: control point at (r, r) for a quarter-circle.
        // Actually, let's use the exact approach: a quadratic can only approximate a circular arc.
        // For a 90° arc from (r,0) to (0,r), the control point is at (r, r) giving
        // a reasonable approximation.

        double r = 100.0;
        // Using cubic Bézier for better circle approximation
        // k = 4/3 * tan(π/8) ≈ 0.5523
        double k = 4.0 / 3.0 * (Math.sqrt(2) - 1);

        // Four cubic arcs forming a circle
        CubicBezier[] arcs = {
                // Arc 1: (r,0) → (0,r)
                new CubicBezier((float) r, 0, (float) r, (float) (r * k), (float) (r * k), (float) r, 0, (float) r),
                // Arc 2: (0,r) → (-r,0)
                new CubicBezier(0, (float) r, (float) (-r * k), (float) r, (float) -r, (float) (r * k), (float) -r, 0),
                // Arc 3: (-r,0) → (0,-r)
                new CubicBezier((float) -r, 0, (float) -r, (float) (-r * k), (float) (-r * k), (float) -r, 0, (float) -r),
                // Arc 4: (0,-r) → (r,0)
                new CubicBezier(0, (float) -r, (float) (r * k), (float) -r, (float) r, (float) (-r * k), (float) r, 0),
        };

        // Test points at known distances from the circle
        double[][] testPoints = {
                {150, 0, 50},     // outside, on positive X axis
                {0, 150, 50},     // outside, on positive Y axis
                {50, 0, 50},      // inside, on positive X axis
                {0, 0, 100},      // center
                {100, 0, 0},      // on the circle
                {0, -100, 0},     // on the circle
        };

        for (double[] tp : testPoints) {
            double px = tp[0], py = tp[1], expectedDist = tp[2];
            double minDist = Double.MAX_VALUE;
            for (CubicBezier arc : arcs) {
                MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToCubicBezier(px, py, arc);
                if (dr.distance() < minDist) {
                    minDist = dr.distance();
                }
            }
            assertEquals(expectedDist, minDist, 1.0,
                    String.format("Distance from (%.0f,%.0f) to circle r=%.0f", px, py, r));
        }
    }

    // =========================================================================
    // Sign correctness tests
    // =========================================================================

    @Test
    @DisplayName("Signed distance flips correctly across glyph boundary - square")
    void testSignFlipSquare() {
        // A simple square outline: (0,0) → (100,0) → (100,100) → (0,100) → close
        // Inside the square: positive cross product from nearest edge
        // Outside: negative

        Line bottom = new Line(0, 0, 100, 0);
        Line right = new Line(100, 0, 100, 100);
        Line top = new Line(100, 100, 0, 100);
        Line left = new Line(0, 100, 0, 0);

        Segment[] edges = {bottom, right, top, left};

        // Point inside the square (50, 50)
        double insideCross = findNearestCross(50, 50, edges);
        assertTrue(insideCross > 0, "Point inside square should have positive cross product (inside)");

        // Point outside the square (50, -10)
        double outsideCross = findNearestCross(50, -10, edges);
        assertTrue(outsideCross < 0, "Point outside square should have negative cross product (outside)");

        // Point outside to the right (110, 50)
        double outsideRightCross = findNearestCross(110, 50, edges);
        assertTrue(outsideRightCross < 0, "Point outside right of square should have negative cross product");
    }

    @Test
    @DisplayName("Signed distance consistent for triangle outline")
    void testSignTriangle() {
        // CCW triangle: (0,0) → (100,0) → (50,86.6) → close
        Line e1 = new Line(0, 0, 100, 0);
        Line e2 = new Line(100, 0, 50, 86.6f);
        Line e3 = new Line(50, 86.6f, 0, 0);

        Segment[] edges = {e1, e2, e3};

        // Centroid (50, 28.87) should be inside
        double insideCross = findNearestCross(50, 28.87, edges);
        assertTrue(insideCross > 0, "Centroid of CCW triangle should be inside");

        // Point well outside (-10, -10)
        double outsideCross = findNearestCross(-10, -10, edges);
        assertTrue(outsideCross < 0, "Point outside triangle should be outside");
    }

    private double findNearestCross(double px, double py, Segment[] edges) {
        double minDist = Double.MAX_VALUE;
        double crossAtNearest = 0;
        for (Segment seg : edges) {
            MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToSegment(px, py, seg);
            if (dr.distance() < minDist) {
                minDist = dr.distance();
                crossAtNearest = dr.dot();
            }
        }
        return crossAtNearest;
    }

    // =========================================================================
    // Pseudo-distance tests
    // =========================================================================

    @Test
    @DisplayName("Pseudo-distance equals true distance for interior closest point")
    void testPseudoDistanceInterior() {
        Line line = new Line(0, 0, 100, 0);
        // Point directly above the middle of the segment — closest point is interior
        MSDFGenerator.PseudoDistanceResult pdr = MSDFGenerator.pseudoDistanceToLine(50, 5, line);
        assertEquals(5.0, pdr.distance(), EPSILON);
        assertEquals(5.0, pdr.pseudoDistance(), EPSILON);
    }

    @Test
    @DisplayName("Pseudo-distance equals true distance at endpoints")
    void testPseudoDistanceEndpoint() {
        Line line = new Line(0, 0, 100, 0);
        // Point past the end of the segment
        MSDFGenerator.PseudoDistanceResult pdr = MSDFGenerator.pseudoDistanceToLine(110, 5, line);
        // True distance: sqrt(100 + 25) ≈ 11.18
        double expected = Math.sqrt(100 + 25);
        assertEquals(expected, pdr.distance(), EPSILON);
        // At endpoint, pseudo-distance should equal true distance
        assertEquals(expected, pdr.pseudoDistance(), EPSILON);
    }

    @Test
    @DisplayName("Pseudo-distance to quadratic Bézier")
    void testPseudoDistanceQuad() {
        QuadBezier q = new QuadBezier(0, 0, 5, 10, 10, 0);
        // Point near the apex — pseudo-distance should be defined
        MSDFGenerator.PseudoDistanceResult pdr = MSDFGenerator.pseudoDistanceToQuadBezier(5, 6, q);
        assertTrue(pdr.distance() > 0);
        assertTrue(pdr.pseudoDistance() > 0);
        assertTrue(pdr.pseudoDistance() <= pdr.distance() + EPSILON,
                "Pseudo-distance should not exceed true distance (except at endpoints)");
    }

    // =========================================================================
    // Tangent direction tests
    // =========================================================================

    @Test
    @DisplayName("Start tangent of line segment")
    void testLineTangent() {
        Line line = new Line(0, 0, 10, 0);
        double[] start = MSDFGenerator.startTangent(line);
        assertEquals(1.0, start[0], EPSILON);
        assertEquals(0.0, start[1], EPSILON);

        double[] end = MSDFGenerator.endTangent(line);
        assertEquals(1.0, end[0], EPSILON);
        assertEquals(0.0, end[1], EPSILON);
    }

    @Test
    @DisplayName("Tangents of quadratic Bézier")
    void testQuadTangent() {
        QuadBezier q = new QuadBezier(0, 0, 5, 10, 10, 0);
        double[] start = MSDFGenerator.startTangent(q);
        // Start tangent points from P0 toward P1: (5, 10) normalized
        double len = Math.sqrt(25 + 100);
        assertEquals(5 / len, start[0], EPSILON);
        assertEquals(10 / len, start[1], EPSILON);

        double[] end = MSDFGenerator.endTangent(q);
        // End tangent points from P1 toward P2: (5, -10) normalized
        assertEquals(5 / len, end[0], EPSILON);
        assertEquals(-10 / len, end[1], EPSILON);
    }

    // =========================================================================
    // Median function test
    // =========================================================================

    @Test
    @DisplayName("Median of three values")
    void testMedian() {
        assertEquals(5.0f, MSDFGenerator.median(3, 5, 7), 0.001f);
        assertEquals(5.0f, MSDFGenerator.median(5, 3, 7), 0.001f);
        assertEquals(5.0f, MSDFGenerator.median(7, 5, 3), 0.001f);
        assertEquals(5.0f, MSDFGenerator.median(5, 5, 5), 0.001f);
        assertEquals(5.0f, MSDFGenerator.median(5, 7, 3), 0.001f);
    }
}
