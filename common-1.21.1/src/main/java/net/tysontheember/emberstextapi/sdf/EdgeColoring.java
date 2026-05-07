package net.tysontheember.emberstextapi.sdf;

import net.tysontheember.emberstextapi.sdf.GlyphOutline.*;

import java.util.ArrayList;
import java.util.List;

public final class EdgeColoring {

    private EdgeColoring() {}

    public static final int RED = 1;
    public static final int GREEN = 2;
    public static final int BLUE = 4;

    public static final int CYAN = GREEN | BLUE;
    public static final int MAGENTA = RED | BLUE;
    public static final int YELLOW = RED | GREEN;
    public static final int WHITE = RED | GREEN | BLUE;

    private static final int[] TWO_CHANNEL_CYCLE = {CYAN, MAGENTA, YELLOW};

    public record ColoredEdge(Segment segment, int color) {}

    public record ColoredContour(List<ColoredEdge> edges) {}

    public static ColoredContour[] colorEdges(GlyphOutline outline, double angleThreshold) {
        List<Contour> contours = outline.contours();
        ColoredContour[] result = new ColoredContour[contours.size()];

        for (int i = 0; i < contours.size(); i++) {
            result[i] = colorContour(contours.get(i), angleThreshold);
        }

        return result;
    }

    static ColoredContour colorContour(Contour contour, double angleThreshold) {
        List<Segment> segments = contour.segments();
        int n = segments.size();

        if (n == 0) {
            return new ColoredContour(List.of());
        }

        if (n == 1) {
            return colorSingleEdgeContour(segments.get(0));
        }

        if (n == 2) {
            return colorTwoEdgeContour(segments, angleThreshold);
        }

        boolean[] isCorner = detectCorners(segments, angleThreshold);

        int cornerCount = 0;
        for (boolean c : isCorner) {
            if (c) cornerCount++;
        }

        if (cornerCount == 0) {
            List<ColoredEdge> colored = new ArrayList<>(n);
            for (Segment seg : segments) {
                colored.add(new ColoredEdge(seg, WHITE));
            }
            return new ColoredContour(colored);
        }

        if (cornerCount == 1) {
            int firstCorner = -1;
            for (int i = 0; i < n; i++) {
                if (isCorner[i]) { firstCorner = i; break; }
            }

            int secondCorner = (firstCorner + n / 2) % n;
            isCorner[secondCorner] = true;
            cornerCount = 2;
        }

        int[] colors = new int[n];
        int colorIndex = 0;

        int startEdge = -1;
        for (int i = 0; i < n; i++) {
            if (isCorner[i]) {
                startEdge = (i + 1) % n;
                break;
            }
        }

        int currentColor = TWO_CHANNEL_CYCLE[colorIndex];
        for (int step = 0; step < n; step++) {
            int edgeIdx = (startEdge + step) % n;

            int prevEdge = (edgeIdx + n - 1) % n;
            if (step > 0 && isCorner[prevEdge]) {

                colorIndex = (colorIndex + 1) % TWO_CHANNEL_CYCLE.length;
                currentColor = TWO_CHANNEL_CYCLE[colorIndex];
            }

            colors[edgeIdx] = currentColor;
        }

        for (int i = 0; i < n; i++) {
            if (isCorner[i]) {
                int nextEdge = (i + 1) % n;
                if (colors[i] == colors[nextEdge]) {

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

    static double angleBetweenTangents(double[] t1, double[] t2) {

        double dot = t1[0] * t2[0] + t1[1] * t2[1];
        dot = Math.max(-1.0, Math.min(1.0, dot));
        return Math.acos(dot);
    }

    private static ColoredContour colorSingleEdgeContour(Segment segment) {

        return new ColoredContour(List.of(new ColoredEdge(segment, WHITE)));
    }

    private static ColoredContour colorTwoEdgeContour(List<Segment> segments, double angleThreshold) {
        boolean[] corners = detectCorners(segments, angleThreshold);

        if (corners[0] || corners[1]) {

            return new ColoredContour(List.of(
                    new ColoredEdge(segments.get(0), CYAN),
                    new ColoredEdge(segments.get(1), MAGENTA)
            ));
        } else {

            return new ColoredContour(List.of(
                    new ColoredEdge(segments.get(0), WHITE),
                    new ColoredEdge(segments.get(1), WHITE)
            ));
        }
    }

    public static boolean hasAtLeastTwoChannels(int color) {
        return Integer.bitCount(color & 7) >= 2;
    }
}
