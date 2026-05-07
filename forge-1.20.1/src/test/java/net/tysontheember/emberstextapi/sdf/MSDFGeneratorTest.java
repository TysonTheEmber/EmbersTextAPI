package net.tysontheember.emberstextapi.sdf;

import net.tysontheember.emberstextapi.sdf.GlyphOutline.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MSDFGeneratorTest {

    private static final double EPSILON = 0.01;

    @Test
    @DisplayName("Distance to horizontal line segment - point above")
    void testLineDistanceAbove() {
        Line line = new Line(0, 0, 10, 0);
        MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToLine(5, 3, line);
        assertEquals(3.0, dr.distance(), EPSILON);

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

        MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToLine(0, 10, line);
        assertEquals(Math.sqrt(50), dr.distance(), EPSILON);
    }

    @Test
    @DisplayName("Distance to quadratic Bézier - point on curve")
    void testQuadBezierOnCurve() {

        QuadBezier q = new QuadBezier(0, 0, 5, 0, 10, 0);
        MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToQuadBezier(5, 0, q);
        assertEquals(0.0, dr.distance(), EPSILON);
    }

    @Test
    @DisplayName("Distance to quadratic Bézier - point above parabola")
    void testQuadBezierAbove() {

        QuadBezier q = new QuadBezier(0, 0, 5, 10, 10, 0);

        MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToQuadBezier(5, 6, q);

        assertEquals(1.0, dr.distance(), EPSILON);
    }

    @Test
    @DisplayName("Distance to quadratic Bézier - point at endpoint")
    void testQuadBezierEndpoint() {
        QuadBezier q = new QuadBezier(0, 0, 5, 10, 10, 0);
        MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToQuadBezier(0, 0, q);
        assertEquals(0.0, dr.distance(), EPSILON);
    }

    @Test
    @DisplayName("Distance to cubic Bézier - point on curve")
    void testCubicBezierOnCurve() {

        CubicBezier c = new CubicBezier(0, 0, 3, 0, 7, 0, 10, 0);
        MSDFGenerator.DistanceResult dr = MSDFGenerator.distanceToCubicBezier(5, 0, c);
        assertEquals(0.0, dr.distance(), EPSILON);
    }

    @Test
    @DisplayName("Distance to cubic Bézier - S-curve")
    void testCubicBezierSCurve() {

        CubicBezier c = new CubicBezier(0, 0, 0, 10, 10, -10, 10, 0);

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

    @Test
    @DisplayName("Distance to circle approximated by 4 quadratic arcs")
    void testCircleApproximation() {

        double r = 100.0;

        double k = 4.0 / 3.0 * (Math.sqrt(2) - 1);

        CubicBezier[] arcs = {

                new CubicBezier((float) r, 0, (float) r, (float) (r * k), (float) (r * k), (float) r, 0, (float) r),

                new CubicBezier(0, (float) r, (float) (-r * k), (float) r, (float) -r, (float) (r * k), (float) -r, 0),

                new CubicBezier((float) -r, 0, (float) -r, (float) (-r * k), (float) (-r * k), (float) -r, 0, (float) -r),

                new CubicBezier(0, (float) -r, (float) (r * k), (float) -r, (float) r, (float) (-r * k), (float) r, 0),
        };

        double[][] testPoints = {
                {150, 0, 50},
                {0, 150, 50},
                {50, 0, 50},
                {0, 0, 100},
                {100, 0, 0},
                {0, -100, 0},
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

    @Test
    @DisplayName("Signed distance flips correctly across glyph boundary - square")
    void testSignFlipSquare() {

        Line bottom = new Line(0, 0, 100, 0);
        Line right = new Line(100, 0, 100, 100);
        Line top = new Line(100, 100, 0, 100);
        Line left = new Line(0, 100, 0, 0);

        Segment[] edges = {bottom, right, top, left};

        double insideCross = findNearestCross(50, 50, edges);
        assertTrue(insideCross > 0, "Point inside square should have positive cross product (inside)");

        double outsideCross = findNearestCross(50, -10, edges);
        assertTrue(outsideCross < 0, "Point outside square should have negative cross product (outside)");

        double outsideRightCross = findNearestCross(110, 50, edges);
        assertTrue(outsideRightCross < 0, "Point outside right of square should have negative cross product");
    }

    @Test
    @DisplayName("Signed distance consistent for triangle outline")
    void testSignTriangle() {

        Line e1 = new Line(0, 0, 100, 0);
        Line e2 = new Line(100, 0, 50, 86.6f);
        Line e3 = new Line(50, 86.6f, 0, 0);

        Segment[] edges = {e1, e2, e3};

        double insideCross = findNearestCross(50, 28.87, edges);
        assertTrue(insideCross > 0, "Centroid of CCW triangle should be inside");

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

    @Test
    @DisplayName("Pseudo-distance equals true distance for interior closest point")
    void testPseudoDistanceInterior() {
        Line line = new Line(0, 0, 100, 0);

        MSDFGenerator.PseudoDistanceResult pdr = MSDFGenerator.pseudoDistanceToLine(50, 5, line);
        assertEquals(5.0, pdr.distance(), EPSILON);
        assertEquals(5.0, pdr.pseudoDistance(), EPSILON);
    }

    @Test
    @DisplayName("Pseudo-distance equals true distance at endpoints")
    void testPseudoDistanceEndpoint() {
        Line line = new Line(0, 0, 100, 0);

        MSDFGenerator.PseudoDistanceResult pdr = MSDFGenerator.pseudoDistanceToLine(110, 5, line);

        double expected = Math.sqrt(100 + 25);
        assertEquals(expected, pdr.distance(), EPSILON);

        assertEquals(expected, pdr.pseudoDistance(), EPSILON);
    }

    @Test
    @DisplayName("Pseudo-distance to quadratic Bézier")
    void testPseudoDistanceQuad() {
        QuadBezier q = new QuadBezier(0, 0, 5, 10, 10, 0);

        MSDFGenerator.PseudoDistanceResult pdr = MSDFGenerator.pseudoDistanceToQuadBezier(5, 6, q);
        assertTrue(pdr.distance() > 0);
        assertTrue(pdr.pseudoDistance() > 0);
        assertTrue(pdr.pseudoDistance() <= pdr.distance() + EPSILON,
                "Pseudo-distance should not exceed true distance (except at endpoints)");
    }

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

        double len = Math.sqrt(25 + 100);
        assertEquals(5 / len, start[0], EPSILON);
        assertEquals(10 / len, start[1], EPSILON);

        double[] end = MSDFGenerator.endTangent(q);

        assertEquals(5 / len, end[0], EPSILON);
        assertEquals(-10 / len, end[1], EPSILON);
    }

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
