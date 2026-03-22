package net.tysontheember.emberstextapi.sdf;

import net.tysontheember.emberstextapi.sdf.GlyphOutline.*;

import java.util.ArrayList;
import java.util.List;

/**
 * MSDF edge coloring algorithm.
 * <p>
 * Assigns RGB color channels to outline edges such that at every corner,
 * the two meeting edges have different colors. This enables the median-of-three
 * operation in the MSDF shader to produce sharp corner transitions.
 * <p>
 * Based on Chlumsky's "simple" coloring heuristic:
 * <ul>
 *   <li>Detect corners where the angle between adjacent edge tangents is below a threshold</li>
 *   <li>At each corner, switch color so the two meeting edges differ in at least one channel</li>
 *   <li>Each edge gets at least 2 of 3 channels (CYAN/MAGENTA/YELLOW/WHITE)</li>
 *   <li>For single-edge contours, split the edge into thirds with alternating colors</li>
 * </ul>
 *
 * @see MSDFGenerator
 */
public final class EdgeColoring {

    private EdgeColoring() {}

    /** Color channel bitmasks */
    public static final int RED = 1;
    public static final int GREEN = 2;
    public static final int BLUE = 4;

    /** Two-channel color combinations — each edge must have at least 2 channels */
    public static final int CYAN = GREEN | BLUE;       // 6
    public static final int MAGENTA = RED | BLUE;       // 5
    public static final int YELLOW = RED | GREEN;       // 3
    public static final int WHITE = RED | GREEN | BLUE; // 7

    /** The three two-channel colors, cycled at corners */
    private static final int[] TWO_CHANNEL_CYCLE = {CYAN, MAGENTA, YELLOW};

    /**
     * An edge segment with an assigned color bitmask.
     */
    public record ColoredEdge(Segment segment, int color) {}

    /**
     * A contour with all edges colored.
     */
    public record ColoredContour(List<ColoredEdge> edges) {}

    /**
     * Apply edge coloring to an entire glyph outline.
     *
     * @param outline        The glyph outline
     * @param angleThreshold Corner angle threshold in radians. Angles sharper than this
     *                       (i.e., where the dot product of adjacent tangents gives an angle
     *                       greater than this threshold measured from 0=same-direction)
     *                       are treated as corners. Default: 3.0 radians ≈ 171.9°.
     *                       The angle is measured as the angle between the ending tangent
     *                       of edge N and the starting tangent of edge N+1. An angle of π
     *                       means the edges are going in opposite directions (sharp U-turn).
     * @return Array of colored contours, one per input contour
     */
    public static ColoredContour[] colorEdges(GlyphOutline outline, double angleThreshold) {
        List<Contour> contours = outline.contours();
        ColoredContour[] result = new ColoredContour[contours.size()];

        for (int i = 0; i < contours.size(); i++) {
            result[i] = colorContour(contours.get(i), angleThreshold);
        }

        return result;
    }

    /**
     * Color a single contour's edges.
     */
    static ColoredContour colorContour(Contour contour, double angleThreshold) {
        List<Segment> segments = contour.segments();
        int n = segments.size();

        if (n == 0) {
            return new ColoredContour(List.of());
        }

        // Special case: single-edge contour (e.g., a circle made of one cubic)
        // Split into 3 parts with alternating colors
        if (n == 1) {
            return colorSingleEdgeContour(segments.get(0));
        }

        // Special case: two-edge contour
        if (n == 2) {
            return colorTwoEdgeContour(segments, angleThreshold);
        }

        // General case: detect corners, assign colors
        // A corner exists between edge[i] and edge[(i+1) % n] when the angle
        // between them is sharper than the threshold.
        boolean[] isCorner = detectCorners(segments, angleThreshold);

        // Count corners
        int cornerCount = 0;
        for (boolean c : isCorner) {
            if (c) cornerCount++;
        }

        // If no corners detected (smooth contour like a circle),
        // use uniform coloring with WHITE
        if (cornerCount == 0) {
            List<ColoredEdge> colored = new ArrayList<>(n);
            for (Segment seg : segments) {
                colored.add(new ColoredEdge(seg, WHITE));
            }
            return new ColoredContour(colored);
        }

        // If exactly one corner, we still need at least 2 different colors
        // to make MSDF work. Force a second corner at the edge farthest from the first.
        if (cornerCount == 1) {
            int firstCorner = -1;
            for (int i = 0; i < n; i++) {
                if (isCorner[i]) { firstCorner = i; break; }
            }
            // Place second corner at the opposite side
            int secondCorner = (firstCorner + n / 2) % n;
            isCorner[secondCorner] = true;
            cornerCount = 2;
        }

        // Assign colors: start from the first corner, cycle colors at each corner
        int[] colors = new int[n];
        int colorIndex = 0;

        // Find the first corner to start coloring from
        int startEdge = -1;
        for (int i = 0; i < n; i++) {
            if (isCorner[i]) {
                startEdge = (i + 1) % n; // Start at the edge AFTER the corner
                break;
            }
        }

        // Walk through all edges, switching color at corners
        int currentColor = TWO_CHANNEL_CYCLE[colorIndex];
        for (int step = 0; step < n; step++) {
            int edgeIdx = (startEdge + step) % n;

            // Check if we're at a corner (between previous edge and this one)
            int prevEdge = (edgeIdx + n - 1) % n;
            if (step > 0 && isCorner[prevEdge]) {
                // Switch to next color
                colorIndex = (colorIndex + 1) % TWO_CHANNEL_CYCLE.length;
                currentColor = TWO_CHANNEL_CYCLE[colorIndex];
            }

            colors[edgeIdx] = currentColor;
        }

        // Verify: at each corner, the two meeting edges must differ.
        // If the cycle wraps badly (corner count divisible by 3), the first and last
        // group might get the same color. Fix by upgrading one group to WHITE.
        for (int i = 0; i < n; i++) {
            if (isCorner[i]) {
                int nextEdge = (i + 1) % n;
                if (colors[i] == colors[nextEdge]) {
                    // The edges at this corner have the same color — fix by making one WHITE
                    colors[nextEdge] = WHITE;
                }
            }
        }

        List<ColoredEdge> colored = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            colored.add(new ColoredEdge(segments.get(i), colors[i]));
        }
        return new ColoredContour(colored);
    }

    /**
     * Detect corners in a closed contour.
     * <p>
     * A corner exists between edge[i] and edge[(i+1) % n] when the angle
     * formed by the ending tangent of edge[i] and the starting tangent of
     * edge[(i+1)] exceeds the threshold.
     *
     * @return boolean array where isCorner[i] = true means there's a corner
     *         between edge[i] and edge[(i+1) % n]
     */
    static boolean[] detectCorners(List<Segment> segments, double angleThreshold) {
        int n = segments.size();
        boolean[] corners = new boolean[n];

        for (int i = 0; i < n; i++) {
            int next = (i + 1) % n;
            double[] endTan = MSDFGenerator.endTangent(segments.get(i));
            double[] startTan = MSDFGenerator.startTangent(segments.get(next));

            double angle = angleBetweenTangents(endTan, startTan);
            corners[i] = angle > angleThreshold;
        }

        return corners;
    }

    /**
     * Compute the angle between two tangent vectors.
     * Returns the angle in radians in [0, π].
     * <p>
     * When the tangents point in the same direction (smooth join), angle ≈ 0.
     * When they point in opposite directions (sharp corner), angle ≈ π.
     * The angleThreshold is compared against this value, so a threshold of 3.0 means
     * "only detect corners where the tangents form an angle > 3.0 radians (> ~171.9°)".
     */
    static double angleBetweenTangents(double[] t1, double[] t2) {
        // The angle between consecutive tangents at a joint.
        // If edge1 ends going "right" and edge2 starts going "right", that's a smooth join (angle=0).
        // If edge1 ends going "right" and edge2 starts going "left", that's a U-turn (angle=π).
        double dot = t1[0] * t2[0] + t1[1] * t2[1];
        dot = Math.max(-1.0, Math.min(1.0, dot)); // clamp for numerical safety
        return Math.acos(dot);
    }

    /**
     * Handle single-edge contour by splitting the edge conceptually.
     * Assigns alternating colors to simulate three edges.
     * <p>
     * Since we can't actually split Bézier segments without modifying the outline,
     * we use a simpler approach: color the single edge with WHITE (all 3 channels).
     * This won't produce MSDF corner sharpness but single-edge contours are typically
     * smooth curves (circles, ellipses) where corners don't exist anyway.
     */
    private static ColoredContour colorSingleEdgeContour(Segment segment) {
        // Single-edge contours are rare and typically smooth.
        // Use WHITE which degrades to monochrome SDF behavior — fine for circles.
        return new ColoredContour(List.of(new ColoredEdge(segment, WHITE)));
    }

    /**
     * Handle two-edge contour.
     */
    private static ColoredContour colorTwoEdgeContour(List<Segment> segments, double angleThreshold) {
        boolean[] corners = detectCorners(segments, angleThreshold);

        if (corners[0] || corners[1]) {
            // At least one corner — assign different colors
            return new ColoredContour(List.of(
                    new ColoredEdge(segments.get(0), CYAN),
                    new ColoredEdge(segments.get(1), MAGENTA)
            ));
        } else {
            // No corners — smooth contour
            return new ColoredContour(List.of(
                    new ColoredEdge(segments.get(0), WHITE),
                    new ColoredEdge(segments.get(1), WHITE)
            ));
        }
    }

    /**
     * Check if a color bitmask has at least 2 channels enabled.
     */
    public static boolean hasAtLeastTwoChannels(int color) {
        return Integer.bitCount(color & 7) >= 2;
    }
}
