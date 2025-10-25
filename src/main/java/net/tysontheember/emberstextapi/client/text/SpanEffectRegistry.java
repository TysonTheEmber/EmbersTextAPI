package net.tysontheember.emberstextapi.client.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.mojang.blaze3d.font.GlyphInfo;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.duck.ETAStyle;
import net.tysontheember.emberstextapi.immersivemessages.api.ObfuscateMode;
import net.tysontheember.emberstextapi.immersivemessages.api.ShakeType;

/**
 * Central location for evaluating span effects registered on {@link ETAStyle}.
 */
public final class SpanEffectRegistry {
    private static final String EFFECT_SHAKE = "emberstextapi:shake";
    private static final String EFFECT_CHAR_SHAKE = "emberstextapi:charshake";
    private static final String EFFECT_OBFUSCATE = "emberstextapi:obfuscate";
    private static final String[] GRADIENT_IDS = {
            "emberstextapi:gradient",
            "emberstextapi:grad",
            "gradient",
            "grad"
    };
    private static final char[] OBFUSCATE_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final float TWO_PI = (float) (Math.PI * 2.0);

    private SpanEffectRegistry() {
    }

    public static void applyEffects(EffectContext context, EffectSettings settings, List<SpanEffect> effects,
            ETAStyle etaStyle, GlyphInfo glyphInfo) {
        if (effects.isEmpty()) {
            return;
        }

        if (!EffectContext.areAnimationsEnabled()) {
            return;
        }

        long nowNanos = EffectContext.nowNanos();
        float seconds = nowNanos / 1_000_000_000.0F;

        for (SpanEffect effect : effects) {
            String id = effect.id();
            Map<String, String> params = effect.parameters();
            if (isGradient(id)) {
                continue; // handled via applyTint
            }
            if (EFFECT_CHAR_SHAKE.equalsIgnoreCase(id)) {
                applyShakeEffect(settings, params, seconds, glyphInfo, true);
                continue;
            }
            if (EFFECT_SHAKE.equalsIgnoreCase(id)) {
                applyShakeEffect(settings, params, seconds, glyphInfo, false);
                continue;
            }
            if (EFFECT_OBFUSCATE.equalsIgnoreCase(id)) {
                applyObfuscate(settings, params, seconds);
            }
        }
    }

    public static int applyTint(Style style, int index, int codePoint, boolean allowAnimatedColor) {
        if (!allowAnimatedColor) {
            return -1;
        }
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

    private static void applyShakeEffect(EffectSettings settings, Map<String, String> params, float seconds,
            GlyphInfo glyphInfo, boolean perGlyph) {
        float amplitude = parseFloat(params, "amplitude", perGlyph ? 0.5F : 1.0F);
        if (amplitude == 0.0F) {
            return;
        }
        float speed = parseFloat(params, "speed", 8.0F);
        float wavelength = parseFloat(params, "wavelength", 1.0F);
        String typeKey = params != null ? params.getOrDefault("type", "random") : "random";
        ShakeType type;
        try {
            type = ShakeType.valueOf(typeKey.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            type = ShakeType.RANDOM;
        }

        float basePhase = seconds * speed;
        float glyphPhase = perGlyph ? settings.getGlyphIndex() / Math.max(0.0001F, wavelength) : 0.0F;
        float phase = basePhase + glyphPhase;

        switch (type) {
            case WAVE -> {
                float offset = (float) Math.sin(phase * TWO_PI);
                settings.setY(settings.getY() + offset * amplitude);
            }
            case CIRCLE -> {
                float angle = phase * TWO_PI;
                settings.setX(settings.getX() + (float) Math.cos(angle) * amplitude);
                settings.setY(settings.getY() + (float) Math.sin(angle) * amplitude);
            }
            case RANDOM -> {
                float seed = phase + settings.getGlyphIndex() * 0.6180339F;
                float offsetX = pseudoNoise(seed + 13.37F) * amplitude;
                float offsetY = pseudoNoise(seed + 7.07F) * amplitude;
                settings.setX(settings.getX() + offsetX);
                settings.setY(settings.getY() + offsetY);
            }
        }

        if (perGlyph && glyphInfo != null) {
            settings.setX(settings.getX() + amplitude * 0.1F);
        }
    }

    private static void applyObfuscate(EffectSettings settings, Map<String, String> params, float seconds) {
        if (Character.isWhitespace(settings.getCodePoint())) {
            return;
        }

        float speed = parseFloat(params, "speed", 12.0F);
        long frame = (long) Math.floor(seconds * Math.max(0.1F, speed));
        if (frame <= 0L) {
            frame = 1L;
        }

        String modeKey = params != null ? params.getOrDefault("mode", "random") : "random";
        ObfuscateMode mode;
        try {
            mode = ObfuscateMode.valueOf(modeKey.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            mode = ObfuscateMode.RANDOM;
        }

        boolean revealOriginal = switch (mode) {
            case NONE -> true;
            case LEFT -> settings.getGlyphIndex() < frame;
            case RIGHT -> false;
            case CENTER -> (settings.getGlyphIndex() % Math.max(1L, frame)) == 0L;
            case RANDOM -> ThreadLocalRandom.current().nextFloat() < 0.2F;
        };

        if (revealOriginal) {
            return;
        }

        int randomIndex = (int) Math.floorMod(frame + settings.getGlyphIndex() * 37L, OBFUSCATE_TABLE.length);
        char replacement = OBFUSCATE_TABLE[randomIndex];
        settings.setCodePoint(replacement);
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

    private static float parseFloat(Map<String, String> params, String key, float fallback) {
        if (params == null) {
            return fallback;
        }
        String raw = params.get(key);
        if (raw == null || raw.isEmpty()) {
            return fallback;
        }
        try {
            return Float.parseFloat(raw);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static float pseudoNoise(float value) {
        float sine = (float) Math.sin(value * 12.9898F + 78.233F) * 43758.5453F;
        return (sine - (float) Math.floor(sine)) * 2.0F - 1.0F;
    }
}
