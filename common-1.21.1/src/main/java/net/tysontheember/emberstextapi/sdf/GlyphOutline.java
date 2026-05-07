package net.tysontheember.emberstextapi.sdf;

import java.util.ArrayList;
import java.util.List;

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

    public List<Segment> allSegments() {
        List<Segment> all = new ArrayList<>();
        for (Contour c : contours) {
            all.addAll(c.segments());
        }
        return all;
    }

    public record Contour(List<Segment> segments) {}

    public sealed interface Segment permits Line, QuadBezier, CubicBezier {}

    public record Line(float x0, float y0, float x1, float y1) implements Segment {}

    public record QuadBezier(float x0, float y0, float cx, float cy, float x1, float y1) implements Segment {}

    public record CubicBezier(float x0, float y0, float cx1, float cy1,
                               float cx2, float cy2, float x1, float y1) implements Segment {}

    public static final class Builder {

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

                float dx = curX - contourStartX;
                float dy = curY - contourStartY;
                if (dx * dx + dy * dy > DEGENERATE_LEN_SQ) {
                    currentSegments.add(new Line(curX, curY, contourStartX, contourStartY));
                }

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
