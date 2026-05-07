package net.tysontheember.emberstextapi.util;

import net.tysontheember.emberstextapi.immersivemessages.util.ColorParser;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class Palettes {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/Palettes");

    private static final float[][] FALLBACK_STOPS = {{1f, 1f, 1f, 1f}};
    private static final float[] FALLBACK_POSITIONS = {0f};

    private Palettes() {}

    public static ColorPalette parse(@Nullable String spec, boolean hsv, ColorPalette.SampleMode mode) {
        if (spec == null || spec.trim().isEmpty()) {
            return new ColorPalette(FALLBACK_STOPS, FALLBACK_POSITIONS, hsv, mode);
        }

        String[] entries = spec.split(",");
        List<float[]> rgbaStops = new ArrayList<>(entries.length);
        List<Float> rawPositions = new ArrayList<>(entries.length);

        for (String raw : entries) {
            String entry = raw.trim();
            if (entry.isEmpty()) continue;

            float position = Float.NaN;
            int at = entry.indexOf('@');
            String hex;
            if (at >= 0) {
                hex = entry.substring(0, at).trim();
                String pos = entry.substring(at + 1).trim();
                try {
                    position = Float.parseFloat(pos);
                } catch (NumberFormatException e) {
                    LOGGER.warn("Invalid palette position '{}' in entry '{}'; skipping", pos, entry);
                    continue;
                }
            } else {
                hex = entry;
            }

            Optional<float[]> rgba = ColorParser.parseToRgbaFloats(hex);
            if (rgba.isEmpty()) {
                LOGGER.warn("Invalid palette color '{}'; skipping", hex);
                continue;
            }

            rgbaStops.add(rgba.get());
            rawPositions.add(position);
        }

        if (rgbaStops.isEmpty()) {
            return new ColorPalette(FALLBACK_STOPS, FALLBACK_POSITIONS, hsv, mode);
        }

        float[] positions = fillPositions(rawPositions);
        float[][] stops = rgbaStops.toArray(new float[0][]);
        sortByPosition(stops, positions);

        return new ColorPalette(stops, positions, hsv, mode);
    }

    private static float[] fillPositions(List<Float> raw) {
        int n = raw.size();
        float[] out = new float[n];
        for (int i = 0; i < n; i++) out[i] = raw.get(i);

        int i = 0;
        while (i < n) {
            if (!Float.isNaN(out[i])) { i++; continue; }

            int runStart = i;
            while (i < n && Float.isNaN(out[i])) i++;
            int runEnd = i;

            float lower = runStart == 0 ? 0f : out[runStart - 1];
            float upper = runEnd == n ? 1f : out[runEnd];
            int runLen = runEnd - runStart;

            for (int k = 0; k < runLen; k++) {
                float t = runStart == 0 && runEnd == n
                        ? (runLen == 1 ? 0.5f : (float) k / (runLen - 1))
                        : (runStart == 0 ? (float) k / runLen
                        : (runEnd == n ? (float) (k + 1) / runLen
                        : (float) (k + 1) / (runLen + 1)));
                out[runStart + k] = lower + (upper - lower) * t;
            }
        }

        for (int j = 0; j < n; j++) {
            if (out[j] < 0f) out[j] = 0f;
            if (out[j] > 1f) out[j] = 1f;
        }
        return out;
    }

    private static void sortByPosition(float[][] stops, float[] positions) {
        int n = positions.length;
        for (int i = 1; i < n; i++) {
            for (int j = i; j > 0 && positions[j] < positions[j - 1]; j--) {
                float p = positions[j]; positions[j] = positions[j - 1]; positions[j - 1] = p;
                float[] s = stops[j]; stops[j] = stops[j - 1]; stops[j - 1] = s;
            }
        }
    }
}
