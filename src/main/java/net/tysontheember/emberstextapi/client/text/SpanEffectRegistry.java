package net.tysontheember.emberstextapi.client.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.duck.ETAStyle;

/**
 * Placeholder registry that will run span effects in a later phase.
 */
public final class SpanEffectRegistry {
    private static final String[] GRADIENT_IDS = {
            "emberstextapi:gradient",
            "emberstextapi:grad",
            "gradient",
            "grad"
    };

    private SpanEffectRegistry() {
    }

    public static void applyEffects(EffectContext context, EffectSettings settings, List<SpanEffect> effects,
            ETAStyle etaStyle) {
        // Phase D1 stub: intentionally left blank. Later phases will mutate settings or render siblings.
    }

    public static int applyTint(Style style, int index, int codePoint) {
        if (!(style instanceof ETAStyle etaStyle)) {
            return -1;
        }
        List<SpanEffect> effects = etaStyle.eta$getEffects();
        if (effects.isEmpty()) {
            return -1;
        }

        for (SpanEffect effect : effects) {
            if (!isGradient(effect.id())) {
                continue;
            }

            int[] colors = extractGradientColors(effect.parameters());
            if (colors.length < 2) {
                continue;
            }

            int span = resolveSpan(effect.parameters());
            float progress = computeProgress(index, span);
            return sampleGradient(colors, progress);
        }

        return -1;
    }

    private static boolean isGradient(String id) {
        for (String candidate : GRADIENT_IDS) {
            if (candidate.equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    private static int[] extractGradientColors(Map<String, String> params) {
        List<Integer> parsed = new ArrayList<>();
        if (params != null) {
            String colors = params.get("colors");
            if (colors != null && !colors.isEmpty()) {
                parseColorList(colors, parsed);
            }
            String from = params.get("from");
            String to = params.get("to");
            if (parsed.isEmpty() && from != null && to != null) {
                addColor(from, parsed);
                addColor(to, parsed);
            }
        }
        if (parsed.size() < 2) {
            return new int[0];
        }
        int[] result = new int[parsed.size()];
        for (int i = 0; i < parsed.size(); i++) {
            result[i] = parsed.get(i);
        }
        return result;
    }

    private static void parseColorList(String raw, List<Integer> output) {
        String[] tokens = raw.split(",");
        for (String token : tokens) {
            addColor(token, output);
        }
    }

    private static void addColor(String token, List<Integer> output) {
        if (token == null) {
            return;
        }
        String normalized = token.trim();
        if (normalized.isEmpty()) {
            return;
        }
        TextColor parsed = TextColor.parseColor(normalized);
        if (parsed != null) {
            output.add(0xFF000000 | parsed.getValue());
        }
    }

    private static int resolveSpan(Map<String, String> params) {
        int defaultSpan = 16;
        if (params == null || params.isEmpty()) {
            return defaultSpan;
        }
        String value = params.get("span");
        if (value == null) {
            value = params.get("length");
        }
        if (value == null) {
            return defaultSpan;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed > 1) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {
        }
        return defaultSpan;
    }

    private static float computeProgress(int index, int span) {
        if (span <= 1) {
            return 0.0F;
        }
        int cycle = Math.max(span, 2);
        int wrapped = Math.floorMod(index, cycle);
        return wrapped / (float) (cycle - 1);
    }

    private static int sampleGradient(int[] colors, float progress) {
        if (colors.length == 0) {
            return -1;
        }
        if (colors.length == 1) {
            return colors[0];
        }
        float clamped = Math.max(0.0F, Math.min(1.0F, progress));
        int segments = colors.length - 1;
        float scaled = clamped * segments;
        int segmentIndex = Math.min(segments - 1, (int) Math.floor(scaled));
        float local = scaled - segmentIndex;
        int start = colors[segmentIndex];
        int end = colors[segmentIndex + 1];
        return lerpArgb(start, end, local);
    }

    private static int lerpArgb(int start, int end, float progress) {
        float clamped = Math.max(0.0F, Math.min(1.0F, progress));
        int a1 = (start >>> 24) & 0xFF;
        int r1 = (start >>> 16) & 0xFF;
        int g1 = (start >>> 8) & 0xFF;
        int b1 = start & 0xFF;
        int a2 = (end >>> 24) & 0xFF;
        int r2 = (end >>> 16) & 0xFF;
        int g2 = (end >>> 8) & 0xFF;
        int b2 = end & 0xFF;
        int a = (int) (a1 + (a2 - a1) * clamped);
        int r = (int) (r1 + (r2 - r1) * clamped);
        int g = (int) (g1 + (g2 - g1) * clamped);
        int b = (int) (b1 + (b2 - b1) * clamped);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
