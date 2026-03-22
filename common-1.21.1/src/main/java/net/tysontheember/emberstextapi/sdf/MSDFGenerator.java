package net.tysontheember.emberstextapi.sdf;

import net.tysontheember.emberstextapi.sdf.GlyphOutline.*;

/**
 * Multi-Channel Signed Distance Field (MSDF) generator.
 * <p>
 * Computes distance fields directly from glyph outlines using analytical Bézier
 * distance computation. Uses three color channels (RGB) to encode distance to
 * differently-colored edges, allowing the shader to reconstruct sharp corners
 * that monochrome SDF fundamentally cannot represent.
 * <p>
 * Based on Chlumsky's MSDF algorithm. Key improvements over the old EDT-based SDF:
 * <ul>
 *   <li>No bitmap intermediary — works directly on vector outlines</li>
 *   <li>Analytical distance computation — no subdivision approximation</li>
 *   <li>Cross-product sign determination — no winding number ray casting</li>
 *   <li>Multi-channel encoding — sharp corner reproduction</li>
 * </ul>
 *
 * @see EdgeColoring
 */
public final class MSDFGenerator {

    private MSDFGenerator() {}

    // =========================================================================
    // Distance result — holds both distance and the parameter on the segment
    // =========================================================================

    /**
     * Result of a distance query to a segment.
     *
     * @param distance   Unsigned distance to the closest point on the segment
     * @param dot        Cross-product sign indicator at the closest point.
     *                   Positive = query point is to the left of the curve tangent (inside).
     * @param nearParam  Parameter t ∈ [0,1] of the closest point on the segment
     */
    public record DistanceResult(double distance, double dot, double nearParam) {
        /** A sentinel result representing infinite distance. */
        public static final DistanceResult INF = new DistanceResult(Double.MAX_VALUE, 0, 0);

        /**
         * The signed distance: positive = inside, negative = outside.
         */
        public double signedDistance() {
            return Math.copySign(distance, dot);
        }
    }

    // =========================================================================
    // Pseudo-distance result — includes orthogonality info for MSDF
    // =========================================================================

    /**
     * Extended distance result including pseudo-distance for MSDF.
     * <p>
     * Pseudo-distance is the perpendicular (orthogonal) distance from the query
     * point to the edge's tangent line at the closest point. Unlike true distance,
     * pseudo-distance does not "wrap around" segment endpoints, which is essential
     * for MSDF corner reconstruction.
     */
    public record PseudoDistanceResult(double distance, double dot, double nearParam, double pseudoDistance) {
        public double signedPseudoDistance() {
            return Math.copySign(pseudoDistance, dot);
        }
    }

    // =========================================================================
    // Analytical distance to Line segment
    // =========================================================================

    /**
     * Compute signed distance from point (px, py) to a line segment.
     * Uses closed-form projection with clamping.
     */
    public static DistanceResult distanceToLine(double px, double py, Line line) {
        double ax = line.x0(), ay = line.y0();
        double bx = line.x1(), by = line.y1();

        double dx = bx - ax;
        double dy = by - ay;
        double lenSq = dx * dx + dy * dy;

        if (lenSq < 1e-20) {
            double ex = px - ax, ey = py - ay;
            return new DistanceResult(Math.sqrt(ex * ex + ey * ey), 0, 0);
        }

        double t = ((px - ax) * dx + (py - ay) * dy) / lenSq;
        t = Math.max(0.0, Math.min(1.0, t));

        double closestX = ax + t * dx;
        double closestY = ay + t * dy;
        double ex = px - closestX;
        double ey = py - closestY;
        double dist = Math.sqrt(ex * ex + ey * ey);

        // Cross product of tangent with vector to point gives sign
        // tangent = (dx, dy), toPoint = (ex, ey)
        // cross = dx * ey - dy * ex
        double cross = dx * ey - dy * ex;

        return new DistanceResult(dist, cross, t);
    }

    /**
     * Compute pseudo-distance from point to a line segment.
     * For lines, pseudo-distance equals the perpendicular distance (no curve effects).
     */
    public static PseudoDistanceResult pseudoDistanceToLine(double px, double py, Line line) {
        double ax = line.x0(), ay = line.y0();
        double bx = line.x1(), by = line.y1();

        double dx = bx - ax;
        double dy = by - ay;
        double lenSq = dx * dx + dy * dy;

        if (lenSq < 1e-20) {
            double ex = px - ax, ey = py - ay;
            double d = Math.sqrt(ex * ex + ey * ey);
            return new PseudoDistanceResult(d, 0, 0, d);
        }

        double t = ((px - ax) * dx + (py - ay) * dy) / lenSq;

        // Perpendicular distance (always use the infinite line for pseudo-distance)
        double len = Math.sqrt(lenSq);
        double perpDist = Math.abs(dx * (py - ay) - dy * (px - ax)) / len;

        double tClamped = Math.max(0.0, Math.min(1.0, t));
        double closestX = ax + tClamped * dx;
        double closestY = ay + tClamped * dy;
        double ex = px - closestX;
        double ey = py - closestY;
        double trueDist = Math.sqrt(ex * ex + ey * ey);

        // Cross product using closest point, consistent with distanceToLine
        double cross = dx * ey - dy * ex;

        // Use pseudo-distance (perpendicular) when the closest point is within the segment,
        // otherwise use true distance (endpoint distance)
        double pd = (t >= 0.0 && t <= 1.0) ? perpDist : trueDist;

        return new PseudoDistanceResult(trueDist, cross, tClamped, pd);
    }

    // =========================================================================
    // Analytical distance to Quadratic Bézier
    // =========================================================================

    /**
     * Evaluate a quadratic Bézier curve at parameter t.
     * B(t) = (1-t)²·P0 + 2(1-t)t·P1 + t²·P2
     */
    private static double[] evalQuad(double t, double p0x, double p0y,
                                      double p1x, double p1y,
                                      double p2x, double p2y) {
        double mt = 1.0 - t;
        double x = mt * mt * p0x + 2 * mt * t * p1x + t * t * p2x;
        double y = mt * mt * p0y + 2 * mt * t * p1y + t * t * p2y;
        return new double[]{x, y};
    }

    /**
     * Evaluate the derivative of a quadratic Bézier at parameter t.
     * B'(t) = 2(1-t)(P1-P0) + 2t(P2-P1)
     */
    private static double[] evalQuadDeriv(double t, double p0x, double p0y,
                                           double p1x, double p1y,
                                           double p2x, double p2y) {
        double mt = 1.0 - t;
        double x = 2 * mt * (p1x - p0x) + 2 * t * (p2x - p1x);
        double y = 2 * mt * (p1y - p0y) + 2 * t * (p2y - p1y);
        return new double[]{x, y};
    }

    /**
     * Compute signed distance from point (px, py) to a quadratic Bézier.
     * <p>
     * Solves for the closest point by finding roots of the cubic equation:
     * d/dt [(P - B(t)) · B'(t)] = 0
     * Uses initial sampling + Newton-Raphson refinement for robustness.
     */
    public static DistanceResult distanceToQuadBezier(double px, double py, QuadBezier q) {
        double p0x = q.x0(), p0y = q.y0();
        double p1x = q.cx(), p1y = q.cy();
        double p2x = q.x1(), p2y = q.y1();

        // Sample at regular intervals + refine with Newton-Raphson
        int samples = 8;
        double bestDist = Double.MAX_VALUE;
        double bestT = 0;
        double bestCross = 0;

        // Check endpoints explicitly
        for (double tInit : new double[]{0.0, 1.0}) {
            double[] pt = evalQuad(tInit, p0x, p0y, p1x, p1y, p2x, p2y);
            double dx = px - pt[0], dy = py - pt[1];
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d < bestDist) {
                double[] tangent = evalQuadDeriv(tInit, p0x, p0y, p1x, p1y, p2x, p2y);
                bestDist = d;
                bestT = tInit;
                bestCross = tangent[0] * dy - tangent[1] * dx;
            }
        }

        // Sample interior points and refine
        for (int i = 0; i <= samples; i++) {
            double tInit = (double) i / samples;
            double t = refineQuadNewton(tInit, px, py, p0x, p0y, p1x, p1y, p2x, p2y);
            t = Math.max(0.0, Math.min(1.0, t));

            double[] pt = evalQuad(t, p0x, p0y, p1x, p1y, p2x, p2y);
            double dx = px - pt[0], dy = py - pt[1];
            double d = Math.sqrt(dx * dx + dy * dy);

            if (d < bestDist) {
                double[] tangent = evalQuadDeriv(t, p0x, p0y, p1x, p1y, p2x, p2y);
                bestDist = d;
                bestT = t;
                bestCross = tangent[0] * dy - tangent[1] * dx;
            }
        }

        return new DistanceResult(bestDist, bestCross, bestT);
    }

    /**
     * Newton-Raphson refinement for closest point on quadratic Bézier.
     * Minimizes f(t) = (P - B(t)) · B'(t) = 0.
     */
    private static double refineQuadNewton(double t, double px, double py,
                                            double p0x, double p0y,
                                            double p1x, double p1y,
                                            double p2x, double p2y) {
        for (int iter = 0; iter < 8; iter++) {
            double[] pt = evalQuad(t, p0x, p0y, p1x, p1y, p2x, p2y);
            double[] d1 = evalQuadDeriv(t, p0x, p0y, p1x, p1y, p2x, p2y);

            // Second derivative: B''(t) = 2(P2 - 2P1 + P0) (constant for quadratic)
            double d2x = 2 * (p2x - 2 * p1x + p0x);
            double d2y = 2 * (p2y - 2 * p1y + p0y);

            double diffx = pt[0] - px;
            double diffy = pt[1] - py;

            // f(t) = diff · B'(t)
            double f = diffx * d1[0] + diffy * d1[1];
            // f'(t) = B'(t)·B'(t) + diff·B''(t)
            double fp = d1[0] * d1[0] + d1[1] * d1[1] + diffx * d2x + diffy * d2y;

            if (Math.abs(fp) < 1e-20) break;

            double dt = f / fp;
            t -= dt;
            t = Math.max(-0.1, Math.min(1.1, t)); // allow slight overshoot for convergence

            if (Math.abs(dt) < 1e-10) break;
        }
        return t;
    }

    /**
     * Compute pseudo-distance from point to a quadratic Bézier.
     */
    public static PseudoDistanceResult pseudoDistanceToQuadBezier(double px, double py, QuadBezier q) {
        DistanceResult dr = distanceToQuadBezier(px, py, q);

        double p0x = q.x0(), p0y = q.y0();
        double p1x = q.cx(), p1y = q.cy();
        double p2x = q.x1(), p2y = q.y1();

        double t = dr.nearParam();
        double[] tangent = evalQuadDeriv(t, p0x, p0y, p1x, p1y, p2x, p2y);
        double tanLen = Math.sqrt(tangent[0] * tangent[0] + tangent[1] * tangent[1]);

        double pseudoDist;
        if (tanLen < 1e-12) {
            pseudoDist = dr.distance();
        } else {
            double[] pt = evalQuad(t, p0x, p0y, p1x, p1y, p2x, p2y);
            // Perpendicular distance = |cross(tangent, diff)| / |tangent|
            double diffx = px - pt[0], diffy = py - pt[1];
            pseudoDist = Math.abs(tangent[0] * diffy - tangent[1] * diffx) / tanLen;
        }

        // If closest point is at an endpoint, use true distance
        if (t <= 0.0 || t >= 1.0) {
            pseudoDist = dr.distance();
        }

        return new PseudoDistanceResult(dr.distance(), dr.dot(), dr.nearParam(), pseudoDist);
    }

    // =========================================================================
    // Analytical distance to Cubic Bézier
    // =========================================================================

    /**
     * Evaluate a cubic Bézier curve at parameter t.
     * B(t) = (1-t)³·P0 + 3(1-t)²t·P1 + 3(1-t)t²·P2 + t³·P3
     */
    private static double[] evalCubic(double t, double p0x, double p0y,
                                       double p1x, double p1y,
                                       double p2x, double p2y,
                                       double p3x, double p3y) {
        double mt = 1.0 - t;
        double mt2 = mt * mt;
        double t2 = t * t;
        double x = mt2 * mt * p0x + 3 * mt2 * t * p1x + 3 * mt * t2 * p2x + t2 * t * p3x;
        double y = mt2 * mt * p0y + 3 * mt2 * t * p1y + 3 * mt * t2 * p2y + t2 * t * p3y;
        return new double[]{x, y};
    }

    /**
     * Evaluate the derivative of a cubic Bézier at parameter t.
     * B'(t) = 3(1-t)²(P1-P0) + 6(1-t)t(P2-P1) + 3t²(P3-P2)
     */
    private static double[] evalCubicDeriv(double t, double p0x, double p0y,
                                            double p1x, double p1y,
                                            double p2x, double p2y,
                                            double p3x, double p3y) {
        double mt = 1.0 - t;
        double x = 3 * mt * mt * (p1x - p0x) + 6 * mt * t * (p2x - p1x) + 3 * t * t * (p3x - p2x);
        double y = 3 * mt * mt * (p1y - p0y) + 6 * mt * t * (p2y - p1y) + 3 * t * t * (p3y - p2y);
        return new double[]{x, y};
    }

    /**
     * Evaluate the second derivative of a cubic Bézier at parameter t.
     * B''(t) = 6(1-t)(P2-2P1+P0) + 6t(P3-2P2+P1)
     */
    private static double[] evalCubicDeriv2(double t, double p0x, double p0y,
                                             double p1x, double p1y,
                                             double p2x, double p2y,
                                             double p3x, double p3y) {
        double mt = 1.0 - t;
        double ax = p2x - 2 * p1x + p0x;
        double ay = p2y - 2 * p1y + p0y;
        double bx = p3x - 2 * p2x + p1x;
        double by = p3y - 2 * p2y + p1y;
        return new double[]{6 * mt * ax + 6 * t * bx, 6 * mt * ay + 6 * t * by};
    }

    /**
     * Compute signed distance from point (px, py) to a cubic Bézier.
     * <p>
     * Uses initial sampling at regular intervals followed by Newton-Raphson
     * refinement. The degree-5 polynomial for the exact closest-t is solved
     * iteratively rather than analytically.
     */
    public static DistanceResult distanceToCubicBezier(double px, double py, CubicBezier c) {
        double p0x = c.x0(), p0y = c.y0();
        double p1x = c.cx1(), p1y = c.cy1();
        double p2x = c.cx2(), p2y = c.cy2();
        double p3x = c.x1(), p3y = c.y1();

        int samples = 12;
        double bestDist = Double.MAX_VALUE;
        double bestT = 0;
        double bestCross = 0;

        // Check endpoints explicitly
        for (double tInit : new double[]{0.0, 1.0}) {
            double[] pt = evalCubic(tInit, p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y);
            double dx = px - pt[0], dy = py - pt[1];
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d < bestDist) {
                double[] tangent = evalCubicDeriv(tInit, p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y);
                // If tangent is zero at endpoint, use second derivative
                if (tangent[0] * tangent[0] + tangent[1] * tangent[1] < 1e-20) {
                    tangent = evalCubicDeriv2(tInit, p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y);
                }
                bestDist = d;
                bestT = tInit;
                bestCross = tangent[0] * dy - tangent[1] * dx;
            }
        }

        // Sample interior and refine
        for (int i = 0; i <= samples; i++) {
            double tInit = (double) i / samples;
            double t = refineCubicNewton(tInit, px, py, p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y);
            t = Math.max(0.0, Math.min(1.0, t));

            double[] pt = evalCubic(t, p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y);
            double dx = px - pt[0], dy = py - pt[1];
            double d = Math.sqrt(dx * dx + dy * dy);

            if (d < bestDist) {
                double[] tangent = evalCubicDeriv(t, p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y);
                if (tangent[0] * tangent[0] + tangent[1] * tangent[1] < 1e-20) {
                    tangent = evalCubicDeriv2(t, p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y);
                }
                bestDist = d;
                bestT = t;
                bestCross = tangent[0] * dy - tangent[1] * dx;
            }
        }

        return new DistanceResult(bestDist, bestCross, bestT);
    }

    /**
     * Newton-Raphson refinement for closest point on cubic Bézier.
     */
    private static double refineCubicNewton(double t, double px, double py,
                                             double p0x, double p0y,
                                             double p1x, double p1y,
                                             double p2x, double p2y,
                                             double p3x, double p3y) {
        for (int iter = 0; iter < 10; iter++) {
            double[] pt = evalCubic(t, p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y);
            double[] d1 = evalCubicDeriv(t, p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y);
            double[] d2 = evalCubicDeriv2(t, p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y);

            double diffx = pt[0] - px;
            double diffy = pt[1] - py;

            double f = diffx * d1[0] + diffy * d1[1];
            double fp = d1[0] * d1[0] + d1[1] * d1[1] + diffx * d2[0] + diffy * d2[1];

            if (Math.abs(fp) < 1e-20) break;

            double dt = f / fp;
            t -= dt;
            t = Math.max(-0.1, Math.min(1.1, t));

            if (Math.abs(dt) < 1e-10) break;
        }
        return t;
    }

    /**
     * Compute pseudo-distance from point to a cubic Bézier.
     */
    public static PseudoDistanceResult pseudoDistanceToCubicBezier(double px, double py, CubicBezier c) {
        DistanceResult dr = distanceToCubicBezier(px, py, c);

        double p0x = c.x0(), p0y = c.y0();
        double p1x = c.cx1(), p1y = c.cy1();
        double p2x = c.cx2(), p2y = c.cy2();
        double p3x = c.x1(), p3y = c.y1();

        double t = dr.nearParam();
        double[] tangent = evalCubicDeriv(t, p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y);
        double tanLen = Math.sqrt(tangent[0] * tangent[0] + tangent[1] * tangent[1]);

        // Handle zero tangent at cusps
        if (tanLen < 1e-12) {
            tangent = evalCubicDeriv2(t, p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y);
            tanLen = Math.sqrt(tangent[0] * tangent[0] + tangent[1] * tangent[1]);
        }

        double pseudoDist;
        if (tanLen < 1e-12) {
            pseudoDist = dr.distance();
        } else {
            double[] pt = evalCubic(t, p0x, p0y, p1x, p1y, p2x, p2y, p3x, p3y);
            double diffx = px - pt[0], diffy = py - pt[1];
            pseudoDist = Math.abs(tangent[0] * diffy - tangent[1] * diffx) / tanLen;
        }

        // If closest point is at an endpoint, use true distance
        if (t <= 0.0 || t >= 1.0) {
            pseudoDist = dr.distance();
        }

        return new PseudoDistanceResult(dr.distance(), dr.dot(), dr.nearParam(), pseudoDist);
    }

    // =========================================================================
    // Generic segment dispatch
    // =========================================================================

    /**
     * Compute signed distance from a point to any segment type.
     */
    public static DistanceResult distanceToSegment(double px, double py, Segment seg) {
        if (seg instanceof Line line) {
            return distanceToLine(px, py, line);
        } else if (seg instanceof QuadBezier quad) {
            return distanceToQuadBezier(px, py, quad);
        } else if (seg instanceof CubicBezier cubic) {
            return distanceToCubicBezier(px, py, cubic);
        }
        return DistanceResult.INF;
    }

    /**
     * Compute pseudo-distance from a point to any segment type.
     */
    public static PseudoDistanceResult pseudoDistanceToSegment(double px, double py, Segment seg) {
        if (seg instanceof Line line) {
            return pseudoDistanceToLine(px, py, line);
        } else if (seg instanceof QuadBezier quad) {
            return pseudoDistanceToQuadBezier(px, py, quad);
        } else if (seg instanceof CubicBezier cubic) {
            return pseudoDistanceToCubicBezier(px, py, cubic);
        }
        return new PseudoDistanceResult(Double.MAX_VALUE, 0, 0, Double.MAX_VALUE);
    }

    // =========================================================================
    // Segment tangent helpers (for edge coloring corner detection)
    // =========================================================================

    /**
     * Get the tangent direction at the start (t=0) of a segment.
     * Returns normalized [dx, dy].
     */
    public static double[] startTangent(Segment seg) {
        double dx, dy;
        if (seg instanceof Line line) {
            dx = line.x1() - line.x0();
            dy = line.y1() - line.y0();
        } else if (seg instanceof QuadBezier q) {
            dx = q.cx() - q.x0();
            dy = q.cy() - q.y0();
            // If control point coincides with start, use endpoint
            if (dx * dx + dy * dy < 1e-12) {
                dx = q.x1() - q.x0();
                dy = q.y1() - q.y0();
            }
        } else if (seg instanceof CubicBezier c) {
            dx = c.cx1() - c.x0();
            dy = c.cy1() - c.y0();
            if (dx * dx + dy * dy < 1e-12) {
                dx = c.cx2() - c.x0();
                dy = c.cy2() - c.y0();
            }
            if (dx * dx + dy * dy < 1e-12) {
                dx = c.x1() - c.x0();
                dy = c.y1() - c.y0();
            }
        } else {
            return new double[]{1, 0};
        }
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-12) return new double[]{1, 0};
        return new double[]{dx / len, dy / len};
    }

    /**
     * Get the tangent direction at the end (t=1) of a segment.
     * Returns normalized [dx, dy].
     */
    public static double[] endTangent(Segment seg) {
        double dx, dy;
        if (seg instanceof Line line) {
            dx = line.x1() - line.x0();
            dy = line.y1() - line.y0();
        } else if (seg instanceof QuadBezier q) {
            dx = q.x1() - q.cx();
            dy = q.y1() - q.cy();
            if (dx * dx + dy * dy < 1e-12) {
                dx = q.x1() - q.x0();
                dy = q.y1() - q.y0();
            }
        } else if (seg instanceof CubicBezier c) {
            dx = c.x1() - c.cx2();
            dy = c.y1() - c.cy2();
            if (dx * dx + dy * dy < 1e-12) {
                dx = c.x1() - c.cx1();
                dy = c.y1() - c.cy1();
            }
            if (dx * dx + dy * dy < 1e-12) {
                dx = c.x1() - c.x0();
                dy = c.y1() - c.y0();
            }
        } else {
            return new double[]{1, 0};
        }
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-12) return new double[]{1, 0};
        return new double[]{dx / len, dy / len};
    }

    // =========================================================================
    // Winding number for robust inside/outside determination
    // =========================================================================

    /**
     * Compute the winding number at point (px, py) by ray casting.
     * <p>
     * Casts a horizontal ray from (px, py) going to +infinity and counts directed
     * crossings with all contour segments. This is robust at segment junctions
     * (unlike cross-product sign, which is ambiguous when two segments are equidistant).
     *
     * @return winding number: nonzero = inside (non-zero fill), odd crossings = inside (even-odd fill)
     */
    static int computeWindingNumber(double px, double py, GlyphOutline outline) {
        int winding = 0;
        for (GlyphOutline.Contour contour : outline.contours()) {
            for (Segment seg : contour.segments()) {
                if (seg instanceof Line line) {
                    winding += lineRayCrossing(px, py, line);
                } else if (seg instanceof QuadBezier quad) {
                    winding += quadRayCrossing(px, py, quad);
                } else if (seg instanceof CubicBezier cubic) {
                    winding += cubicRayCrossing(px, py, cubic);
                }
            }
        }
        return winding;
    }

    /**
     * Determine inside/outside using the winding number and the outline's fill rule.
     */
    static boolean isInside(double px, double py, GlyphOutline outline) {
        int winding = computeWindingNumber(px, py, outline);
        if (outline.evenOddFill()) {
            return (winding & 1) != 0; // even-odd: odd crossings = inside
        }
        return winding != 0; // non-zero winding
    }

    /**
     * Ray crossing contribution for a line segment.
     * Horizontal ray from (px, py) going to +X infinity.
     */
    private static int lineRayCrossing(double px, double py, Line line) {
        double y0 = line.y0(), y1 = line.y1();
        // Upward crossing: y0 <= py < y1
        if (y0 <= py && y1 > py) {
            double t = (py - y0) / (y1 - y0);
            double crossX = line.x0() + t * (line.x1() - line.x0());
            if (crossX > px) return 1;
        }
        // Downward crossing: y1 <= py < y0
        else if (y1 <= py && y0 > py) {
            double t = (py - y0) / (y1 - y0);
            double crossX = line.x0() + t * (line.x1() - line.x0());
            if (crossX > px) return -1;
        }
        return 0;
    }

    /**
     * Ray crossing contribution for a quadratic Bézier.
     * Solves B_y(t) = py analytically (quadratic equation in t).
     */
    private static int quadRayCrossing(double px, double py, QuadBezier q) {
        double y0 = q.y0(), y1 = q.cy(), y2 = q.y1();
        // Quadratic: a*t² + b*t + c = 0 where B_y(t) = py
        double a = y0 - 2 * y1 + y2;
        double b = 2 * (y1 - y0);
        double c = y0 - py;

        int winding = 0;

        if (Math.abs(a) < 1e-12) {
            // Degenerate to linear
            if (Math.abs(b) < 1e-12) return 0;
            double t = -c / b;
            if (t >= 0 && t < 1) {
                double mt = 1 - t;
                double crossX = mt * mt * q.x0() + 2 * mt * t * q.cx() + t * t * q.x1();
                if (crossX > px) {
                    // Direction from y-derivative: B'_y(t) = b + 2*a*t ≈ b
                    winding += b > 0 ? 1 : -1;
                }
            }
            return winding;
        }

        double disc = b * b - 4 * a * c;
        if (disc < 0) return 0;

        double sqrtDisc = Math.sqrt(disc);
        double inv2a = 1.0 / (2 * a);
        double t1 = (-b - sqrtDisc) * inv2a;
        double t2 = (-b + sqrtDisc) * inv2a;

        for (double t : new double[]{t1, t2}) {
            if (t >= 0 && t < 1) {
                double mt = 1 - t;
                double crossX = mt * mt * q.x0() + 2 * mt * t * q.cx() + t * t * q.x1();
                if (crossX > px) {
                    // y-derivative: B'_y(t) = 2(1-t)(y1-y0) + 2t(y2-y1)
                    double dydt = 2 * (1 - t) * (y1 - y0) + 2 * t * (y2 - y1);
                    winding += dydt > 0 ? 1 : -1;
                }
            }
        }
        return winding;
    }

    /**
     * Ray crossing contribution for a cubic Bézier.
     * Solves B_y(t) = py by subdividing and testing line segments.
     */
    private static int cubicRayCrossing(double px, double py, CubicBezier c) {
        double y0 = c.y0(), y1 = c.cy1(), y2 = c.cy2(), y3 = c.y1();
        // Quick reject: if all control points are on the same side of py
        double minY = Math.min(Math.min(y0, y1), Math.min(y2, y3));
        double maxY = Math.max(Math.max(y0, y1), Math.max(y2, y3));
        if (py < minY || py >= maxY) return 0;

        // Subdivide into line segments for ray crossing
        int subdivisions = 16;
        int winding = 0;
        double prevX = c.x0(), prevY = c.y0();
        for (int i = 1; i <= subdivisions; i++) {
            double t = (double) i / subdivisions;
            double[] pt = evalCubic(t, c.x0(), c.y0(), c.cx1(), c.cy1(),
                    c.cx2(), c.cy2(), c.x1(), c.y1());
            double curX = pt[0], curY = pt[1];

            if (prevY <= py && curY > py) {
                double frac = (py - prevY) / (curY - prevY);
                if (prevX + frac * (curX - prevX) > px) winding++;
            } else if (curY <= py && prevY > py) {
                double frac = (py - prevY) / (curY - prevY);
                if (prevX + frac * (curX - prevX) > px) winding--;
            }
            prevX = curX;
            prevY = curY;
        }
        return winding;
    }

    // =========================================================================
    // MSDF Generation
    // =========================================================================

    /**
     * Generate a 3-channel MSDF texture from a glyph outline.
     * <p>
     * Output is a byte array of size {@code width * height * 3} in RGB order.
     * Each channel encodes the signed pseudo-distance to the nearest edge of
     * that channel's color, normalized so that 128 = on edge.
     *
     * @param outline       The glyph outline with contours
     * @param coloredEdges  Edge coloring result from {@link EdgeColoring}
     * @param width         Output texture width in pixels
     * @param height        Output texture height in pixels
     * @param glyphMinX     Glyph bounding box min X (font units)
     * @param glyphMinY     Glyph bounding box min Y (font units)
     * @param glyphMaxX     Glyph bounding box max X (font units)
     * @param glyphMaxY     Glyph bounding box max Y (font units)
     * @param pxRange       Pixel range — how many output pixels the distance field spans.
     *                      Controls the width of the anti-aliasing transition.
     * @return RGB byte array (3 bytes per pixel, row-major, Y=0 is top)
     */
    public static byte[] generate(GlyphOutline outline,
                                   EdgeColoring.ColoredContour[] coloredEdges,
                                   int width, int height,
                                   double glyphMinX, double glyphMinY,
                                   double glyphMaxX, double glyphMaxY,
                                   double pxRange) {
        byte[] msdf = new byte[width * height * 3];

        double glyphW = glyphMaxX - glyphMinX;
        double glyphH = glyphMaxY - glyphMinY;
        double maxDim = Math.max(glyphW, glyphH);
        if (maxDim < 1.0) maxDim = 1.0;

        // Map from texel to glyph coordinate space
        // We want the glyph centered in the texture with padding for the distance field
        double texelToGlyph = maxDim / (Math.max(width, height) - pxRange);

        // unitRange: the distance in glyph units that corresponds to pxRange pixels
        // Must use the same scale factor as texelToGlyph for consistent normalization
        double unitRange = pxRange * texelToGlyph;
        double centerX = (glyphMinX + glyphMaxX) * 0.5;
        double centerY = (glyphMinY + glyphMaxY) * 0.5;
        double originX = centerX - width * 0.5 * texelToGlyph;
        double originY = centerY - height * 0.5 * texelToGlyph;

        // Determine winding sign based on outline fill direction.
        // FT_OUTLINE_REVERSE_FILL (0x4) is set for CCW outer contours (PostScript/CFF).
        // When not set, outer contours are CW (TrueType) and cross-product sign is inverted.
        int windingSign = outline.reverseFill() ? 1 : -1;

        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                // Map pixel center to glyph space
                // Y is flipped: glyph Y increases upward, texture Y increases downward
                double gx = originX + (px + 0.5) * texelToGlyph;
                double gy = originY + ((height - 1 - py) + 0.5) * texelToGlyph;

                // Ground truth inside/outside via winding number ray casting.
                // This is robust at segment junctions where cross-product sign
                // is ambiguous (two meeting segments can give opposite signs).
                boolean truthInside = isInside(gx, gy, outline);
                int truthSign = truthInside ? 1 : -1;

                // Per-channel: find nearest edge and its pseudo-distance.
                // MSDF requires per-channel signs to produce sharp corners —
                // at a corner, two channels see one edge and one channel sees the other.
                double minDistR = Double.MAX_VALUE, signR = 1;
                double minDistG = Double.MAX_VALUE, signG = 1;
                double minDistB = Double.MAX_VALUE, signB = 1;
                double minPseudoR = Double.MAX_VALUE;
                double minPseudoG = Double.MAX_VALUE;
                double minPseudoB = Double.MAX_VALUE;
                double minDistOverall = Double.MAX_VALUE;

                for (EdgeColoring.ColoredContour cc : coloredEdges) {
                    for (EdgeColoring.ColoredEdge edge : cc.edges()) {
                        PseudoDistanceResult pdr = pseudoDistanceToSegment(gx, gy, edge.segment());
                        int edgeSign = pdr.dot() * windingSign >= 0 ? 1 : -1;

                        if (pdr.distance() < minDistOverall) {
                            minDistOverall = pdr.distance();
                        }

                        if ((edge.color() & EdgeColoring.RED) != 0) {
                            if (pdr.distance() < minDistR) {
                                minDistR = pdr.distance();
                                signR = edgeSign;
                                minPseudoR = pdr.pseudoDistance();
                            }
                        }
                        if ((edge.color() & EdgeColoring.GREEN) != 0) {
                            if (pdr.distance() < minDistG) {
                                minDistG = pdr.distance();
                                signG = edgeSign;
                                minPseudoG = pdr.pseudoDistance();
                            }
                        }
                        if ((edge.color() & EdgeColoring.BLUE) != 0) {
                            if (pdr.distance() < minDistB) {
                                minDistB = pdr.distance();
                                signB = edgeSign;
                                minPseudoB = pdr.pseudoDistance();
                            }
                        }
                    }
                }

                // Per-channel signed pseudo-distances (preserves MSDF corner behavior)
                double sdR = signR * minPseudoR;
                double sdG = signG * minPseudoG;
                double sdB = signB * minPseudoB;

                // Normalize: 128 = on edge, 0 = unitRange/2 outside, 255 = unitRange/2 inside
                double norm = 127.0 / (unitRange * 0.5);
                int r = (int) Math.round(128.0 + norm * sdR);
                int g = (int) Math.round(128.0 + norm * sdG);
                int b = (int) Math.round(128.0 + norm * sdB);

                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));

                // Error correction: if median(R,G,B) disagrees with the winding
                // number ground truth, shift all channels uniformly to correct the
                // median while preserving inter-channel differences (MSDF corner info).
                // Since median(r+k, g+k, b+k) = median(r,g,b) + k, a uniform shift
                // exactly corrects the median classification without collapsing to
                // single-channel SDF. This avoids the visible seams that the old
                // "collapse to gtVal" approach created, so no deadzone is needed.
                float med = median(r, g, b);
                boolean medianInside = med >= 128;
                if (medianInside != truthInside) {
                    double groundTruthSD = truthSign * minDistOverall;
                    int gtMed = (int) Math.round(128.0 + norm * groundTruthSD);
                    gtMed = Math.max(0, Math.min(255, gtMed));
                    int shift = (int) (gtMed - med);
                    r = Math.max(0, Math.min(255, r + shift));
                    g = Math.max(0, Math.min(255, g + shift));
                    b = Math.max(0, Math.min(255, b + shift));
                }

                int idx = (py * width + px) * 3;
                msdf[idx] = (byte) r;
                msdf[idx + 1] = (byte) g;
                msdf[idx + 2] = (byte) b;
            }
        }

        return msdf;
    }

    /**
     * MSDF error correction pass.
     * <p>
     * Scans for texels where the median of (R, G, B) disagrees with what the
     * individual channels suggest about inside/outside. This catches false-color
     * artifacts at edge intersection points caused by the multi-channel encoding.
     * <p>
     * When a texel's median flips sign compared to a neighbor's median, and the
     * texel has one channel that's an outlier (far from the other two), that channel
     * is clamped to the median value.
     */
    private static void correctErrors(byte[] msdf, int width, int height) {
        // Work on a copy to avoid cascading corrections
        byte[] copy = msdf.clone();

        for (int py = 0; py < height; py++) {
            for (int px = 0; px < width; px++) {
                int idx = (py * width + px) * 3;
                int r = copy[idx] & 0xFF;
                int g = copy[idx + 1] & 0xFF;
                int b = copy[idx + 2] & 0xFF;

                float median = median(r, g, b);
                boolean inside = median >= 128;

                // Check if this texel's median disagrees with most neighbors
                int agreeCount = 0;
                int disagreeCount = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = px + dx, ny = py + dy;
                        if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;
                        int nIdx = (ny * width + nx) * 3;
                        int nr = copy[nIdx] & 0xFF;
                        int ng = copy[nIdx + 1] & 0xFF;
                        int nb = copy[nIdx + 2] & 0xFF;
                        boolean nInside = median(nr, ng, nb) >= 128;
                        if (nInside == inside) agreeCount++;
                        else disagreeCount++;
                    }
                }

                // If a clear majority of neighbors disagree, this is likely an error artifact
                if (disagreeCount > agreeCount && disagreeCount >= 5) {
                    // Clamp all channels to the median
                    int med = Math.max(0, Math.min(255, Math.round(median)));
                    msdf[idx] = (byte) med;
                    msdf[idx + 1] = (byte) med;
                    msdf[idx + 2] = (byte) med;
                }
            }
        }
    }

    /**
     * Compute the median of three values (the MSDF reconstruction kernel).
     */
    static float median(float r, float g, float b) {
        return Math.max(Math.min(r, g), Math.min(Math.max(r, g), b));
    }

    private static float median(int r, int g, int b) {
        return median((float) r, (float) g, (float) b);
    }
}
