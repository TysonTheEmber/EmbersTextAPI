package net.tysontheember.emberstextapi.sdf;

import java.util.ArrayList;
import java.util.List;

/**
 * Parsed glyph outline containing contours and their Bézier/line segments.
 * <p>
 * Extracted from FreeType glyph data via {@code FT_Outline_Decompose} in
 * {@link FreeTypeManager#extractOutline}. Coordinates are in font units
 * (unscaled, no hinting) with Y-axis increasing upward.
 * <p>
 * The outline is the foundation of the MSDF pipeline: it provides the vector
 * data from which {@link EdgeColoring} assigns channel colors and
 * {@link MSDFGenerator} computes analytical signed distances.
 *
 * @see FreeTypeManager#extractOutline
 * @see MSDFGenerator
 * @see EdgeColoring
 */
public final class GlyphOutline {

    private final List<Contour> contours;
    private final float minX, minY, maxX, maxY;
    private final boolean evenOddFill;
    private final boolean reverseFill;

    public GlyphOutline(List<Contour> contours, float minX, float minY, float maxX, float maxY,
                         boolean evenOddFill, boolean reverseFill) {
        this.contours = contours;
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.evenOddFill = evenOddFill;
        this.reverseFill = reverseFill;
    }

    public List<Contour> contours() { return contours; }
    public float minX() { return minX; }
    public float minY() { return minY; }
    public float maxX() { return maxX; }
    public float maxY() { return maxY; }
    public float width() { return maxX - minX; }
    public float height() { return maxY - minY; }
    public boolean evenOddFill() { return evenOddFill; }
    public boolean reverseFill() { return reverseFill; }

    /**
     * Get all segments across all contours.
     */
    public List<Segment> allSegments() {
        List<Segment> all = new ArrayList<>();
        for (Contour c : contours) {
            all.addAll(c.segments());
        }
        return all;
    }

    /**
     * A single closed contour of the glyph outline.
     * Outer contours typically wind counter-clockwise; inner contours (holes) wind clockwise.
     *
     * @param segments the ordered list of segments forming this closed contour
     */
    public record Contour(List<Segment> segments) {}

    /**
     * Base type for outline segments. All coordinates are in font units.
     * <p>
     * Each segment type supports analytical distance computation in
     * {@link MSDFGenerator} and tangent extraction for corner detection
     * in {@link EdgeColoring}.
     */
    public sealed interface Segment permits Line, QuadBezier, CubicBezier {}

    /**
     * A straight line segment from (x0, y0) to (x1, y1).
     *
     * @param x0 start X coordinate in font units
     * @param y0 start Y coordinate in font units
     * @param x1 end X coordinate in font units
     * @param y1 end Y coordinate in font units
     */
    public record Line(float x0, float y0, float x1, float y1) implements Segment {}

    /**
     * A quadratic Bézier curve: B(t) = (1−t)²·P0 + 2(1−t)t·C + t²·P1.
     * <p>
     * Produced by TrueType fonts (which use quadratic outlines).
     *
     * @param x0 start X coordinate (P0)
     * @param y0 start Y coordinate (P0)
     * @param cx control point X (C)
     * @param cy control point Y (C)
     * @param x1 end X coordinate (P1)
     * @param y1 end Y coordinate (P1)
     */
    public record QuadBezier(float x0, float y0, float cx, float cy, float x1, float y1) implements Segment {}

    /**
     * A cubic Bézier curve: B(t) = (1−t)³·P0 + 3(1−t)²t·C1 + 3(1−t)t²·C2 + t³·P1.
     * <p>
     * Produced by OpenType/CFF fonts (which use cubic outlines).
     *
     * @param x0  start X coordinate (P0)
     * @param y0  start Y coordinate (P0)
     * @param cx1 first control point X (C1)
     * @param cy1 first control point Y (C1)
     * @param cx2 second control point X (C2)
     * @param cy2 second control point Y (C2)
     * @param x1  end X coordinate (P1)
     * @param y1  end Y coordinate (P1)
     */
    public record CubicBezier(float x0, float y0, float cx1, float cy1,
                               float cx2, float cy2, float x1, float y1) implements Segment {}

    /**
     * Builder for constructing outlines from FT_Outline_Decompose callbacks.
     */
    public static final class Builder {
        // Segments shorter than this (squared) are considered degenerate and filtered out.
        // Bold fonts can produce very short segments at stroke junctions that cause
        // distance computation instabilities in the MSDF generator.
        private static final float DEGENERATE_LEN_SQ = 1.0f;

        private final List<Contour> contours = new ArrayList<>();
        private List<Segment> currentSegments = new ArrayList<>();
        private float curX, curY;
        private float contourStartX, contourStartY;
        private float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        private float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        private boolean evenOddFill;
        private boolean reverseFill;

        public void moveTo(float x, float y) {
            closeContourIfNeeded();
            currentSegments = new ArrayList<>();
            curX = x;
            curY = y;
            contourStartX = x;
            contourStartY = y;
            updateBounds(x, y);
        }

        public void lineTo(float x, float y) {
            currentSegments.add(new Line(curX, curY, x, y));
            curX = x;
            curY = y;
            updateBounds(x, y);
        }

        public void conicTo(float cx, float cy, float x, float y) {
            currentSegments.add(new QuadBezier(curX, curY, cx, cy, x, y));
            curX = x;
            curY = y;
            updateBounds(cx, cy);
            updateBounds(x, y);
        }

        public void cubicTo(float cx1, float cy1, float cx2, float cy2, float x, float y) {
            currentSegments.add(new CubicBezier(curX, curY, cx1, cy1, cx2, cy2, x, y));
            curX = x;
            curY = y;
            updateBounds(cx1, cy1);
            updateBounds(cx2, cy2);
            updateBounds(x, y);
        }

        public void setEvenOddFill(boolean evenOddFill) {
            this.evenOddFill = evenOddFill;
        }

        public void setReverseFill(boolean reverseFill) {
            this.reverseFill = reverseFill;
        }

        public GlyphOutline build() {
            closeContourIfNeeded();
            if (minX > maxX) {
                return new GlyphOutline(List.of(), 0, 0, 0, 0, evenOddFill, reverseFill);
            }
            return new GlyphOutline(List.copyOf(contours), minX, minY, maxX, maxY, evenOddFill, reverseFill);
        }

        private void closeContourIfNeeded() {
            if (!currentSegments.isEmpty()) {
                // Close the contour if not already closed (epsilon-based check)
                float dx = curX - contourStartX;
                float dy = curY - contourStartY;
                if (dx * dx + dy * dy > DEGENERATE_LEN_SQ) {
                    currentSegments.add(new Line(curX, curY, contourStartX, contourStartY));
                }
                // Filter out degenerate (near-zero-length) segments that can cause
                // distance computation errors with bold/complex font outlines
                List<Segment> filtered = new ArrayList<>(currentSegments.size());
                for (Segment seg : currentSegments) {
                    if (!isDegenerate(seg)) {
                        filtered.add(seg);
                    }
                }
                if (!filtered.isEmpty()) {
                    contours.add(new Contour(List.copyOf(filtered)));
                }
            }
        }

        private static boolean isDegenerate(Segment seg) {
            if (seg instanceof Line line) {
                return distSq(line.x0(), line.y0(), line.x1(), line.y1()) < DEGENERATE_LEN_SQ;
            } else if (seg instanceof QuadBezier q) {
                return distSq(q.x0(), q.y0(), q.x1(), q.y1()) < DEGENERATE_LEN_SQ
                        && distSq(q.x0(), q.y0(), q.cx(), q.cy()) < DEGENERATE_LEN_SQ;
            } else if (seg instanceof CubicBezier c) {
                return distSq(c.x0(), c.y0(), c.x1(), c.y1()) < DEGENERATE_LEN_SQ
                        && distSq(c.x0(), c.y0(), c.cx1(), c.cy1()) < DEGENERATE_LEN_SQ
                        && distSq(c.x0(), c.y0(), c.cx2(), c.cy2()) < DEGENERATE_LEN_SQ;
            }
            return false;
        }

        private static float distSq(float x0, float y0, float x1, float y1) {
            float dx = x1 - x0;
            float dy = y1 - y0;
            return dx * dx + dy * dy;
        }

        private void updateBounds(float x, float y) {
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }
    }
}
