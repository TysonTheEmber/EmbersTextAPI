package net.tysontheember.emberstextapi.core.render;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.core.style.GlyphEffect;
import net.tysontheember.emberstextapi.core.style.ShakeState;
import net.tysontheember.emberstextapi.core.style.SpanEffectState;
import net.tysontheember.emberstextapi.core.style.SpanGradient;
import net.tysontheember.emberstextapi.immersivemessages.api.ObfuscateMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Applies the various span-level effects to glyph rendering metadata.
 */
@OnlyIn(Dist.CLIENT)
public final class GlyphEffectRuntime {
    private static final ResourceLocation EFFECT_OBFUSCATE = ResourceLocation.fromNamespaceAndPath("emberstextapi", "obfuscate");
    private static final Map<SpanEffectState, ObfuscationContext> OBFUSCATION_CONTEXTS = new IdentityHashMap<>();

    private GlyphEffectRuntime() {
    }

    public static void apply(GlyphRenderSettings settings) {
        SpanEffectState state = settings.effectState();
        if (state == null || state.isEmpty()) {
            return;
        }

        if (!TypewriterRuntime.isGlyphVisible(state.typewriter())) {
            settings.setVisible(false);
            return;
        }

        applyGradient(settings, state.gradient());
        applyShake(settings, state.shakes());
        applyGlyphEffects(settings, state.glyphEffects());
    }

    private static void applyGradient(GlyphRenderSettings settings, SpanGradient gradient) {
        if (gradient == null || gradient.colors().isEmpty()) {
            return;
        }

        List<Integer> colors = gradient.colors();
        int count = colors.size();
        if (count == 1) {
            setColorFromArgb(settings, colors.get(0));
            return;
        }

        double position = settings.logicalIndex() + gradient.offset();
        if (gradient.speed() != 0.0f) {
            position += RenderTime.getTicks() * gradient.speed();
        }

        if (gradient.repeating()) {
            double wrapped = position % count;
            if (wrapped < 0.0d) {
                wrapped += count;
            }
            int lower = Mth.floor(wrapped);
            int upper = (lower + 1) % count;
            float fraction = (float) (wrapped - lower);
            setColorFromArgb(settings, lerpColor(colors.get(lower), colors.get(upper), fraction));
        } else {
            double clamped = Mth.clamp(position, 0.0d, count - 1.0d);
            int lower = Mth.floor(clamped);
            int upper = Math.min(count - 1, lower + 1);
            float fraction = (float) (clamped - lower);
            setColorFromArgb(settings, lerpColor(colors.get(lower), colors.get(upper), fraction));
        }
    }

    private static void setColorFromArgb(GlyphRenderSettings settings, int argb) {
        float alpha = ((argb >> 24) & 0xFF) / 255.0f;
        float red = ((argb >> 16) & 0xFF) / 255.0f;
        float green = ((argb >> 8) & 0xFF) / 255.0f;
        float blue = (argb & 0xFF) / 255.0f;
        settings.setAlpha(settings.alpha() * alpha);
        settings.setRed(red);
        settings.setGreen(green);
        settings.setBlue(blue);
    }

    private static int lerpColor(int lower, int upper, float fraction) {
        float inv = 1.0f - fraction;
        int a = (int) (((lower >> 24) & 0xFF) * inv + ((upper >> 24) & 0xFF) * fraction);
        int r = (int) (((lower >> 16) & 0xFF) * inv + ((upper >> 16) & 0xFF) * fraction);
        int g = (int) (((lower >> 8) & 0xFF) * inv + ((upper >> 8) & 0xFF) * fraction);
        int b = (int) ((lower & 0xFF) * inv + (upper & 0xFF) * fraction);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void applyShake(GlyphRenderSettings settings, List<ShakeState> shakes) {
        if (shakes == null || shakes.isEmpty()) {
            return;
        }
        double time = RenderTime.getTicks();
        for (ShakeState shake : shakes) {
            float amplitude = shake.amplitude();
            float offsetX = 0.0f;
            float offsetY = 0.0f;
            float speed = shake.speed();
            switch (shake.type()) {
                case WAVE -> {
                    float wavelength = shake.wavelength() != 0.0f ? shake.wavelength() : 1.0f;
                    double waveTime = time * 0.05d * speed + settings.logicalIndex() * 0.1d;
                    offsetY = (float) Math.sin(waveTime * (2 * Math.PI) / wavelength) * amplitude;
                }
                case CIRCLE -> {
                    double circleTime = time * 0.05d * speed + settings.logicalIndex() * 0.1d;
                    offsetX = (float) Math.cos(circleTime) * amplitude;
                    offsetY = (float) Math.sin(circleTime) * amplitude;
                }
                case RANDOM -> {
                    double seed = Math.floor(time * Math.max(1.0f, speed));
                    double hash = Double.longBitsToDouble(Double.doubleToLongBits(seed) ^ settings.logicalIndex() * 31L);
                    offsetX = (float) ((hash % 1.0d) * 2.0d - 1.0d) * amplitude;
                    offsetY = (float) ((((hash * 31.0d) % 1.0d) * 2.0d) - 1.0d) * amplitude;
                }
            }
            settings.translate(offsetX, offsetY);
        }
    }

    private static void applyGlyphEffects(GlyphRenderSettings settings, List<GlyphEffect> effects) {
        if (effects == null || effects.isEmpty()) {
            return;
        }
        for (GlyphEffect effect : effects) {
            if (EFFECT_OBFUSCATE.equals(effect.id())) {
                applyObfuscation(settings, effect);
            }
        }
    }

    private static void applyObfuscation(GlyphRenderSettings settings, GlyphEffect effect) {
        if (settings.effectState() == null) {
            return;
        }

        var parameters = effect.parameters();
        ObfuscateMode mode = ObfuscateMode.LEFT;
        float speed = 1.0f;
        if (parameters.contains("mode")) {
            try {
                mode = ObfuscateMode.valueOf(parameters.getString("mode").toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (parameters.contains("speed")) {
            speed = parameters.getFloat("speed");
        }

        long seed = parameters.contains("seed") ? parameters.getLong("seed") : System.identityHashCode(settings.effectState());
        ObfuscationContext context = OBFUSCATION_CONTEXTS.computeIfAbsent(settings.effectState(), state -> new ObfuscationContext(seed));
        int maxIndex = context.updateMaxIndex(settings.logicalIndex());
        int totalGlyphs = maxIndex + 1;
        if (totalGlyphs <= 0) {
            totalGlyphs = 1;
        }

        double progress = RenderTime.getTicks() * speed;
        double threshold = Double.NEGATIVE_INFINITY;
        switch (mode) {
            case RIGHT -> threshold = maxIndex - settings.logicalIndex() + 1;
            case CENTER -> {
                double centre = maxIndex / 2.0d;
                threshold = Math.abs(settings.logicalIndex() - centre) + 1.0d;
            }
            case RANDOM -> {
                double order = context.randomOrder(settings.logicalIndex());
                threshold = order * totalGlyphs + 1.0d;
            }
            case LEFT -> threshold = settings.logicalIndex() + 1.0d;
            case NONE -> {
                // Use default threshold so obfuscation is bypassed immediately.
            }
        }

        if (!(progress >= threshold)) {
            settings.setBakedGlyph(settings.fontSet().getRandomGlyph(settings.glyphInfo()));
        }
    }

    private static final class ObfuscationContext {
        private final long seed;
        private int maxIndex = -1;

        private ObfuscationContext(long seed) {
            this.seed = seed;
        }

        private int updateMaxIndex(int index) {
            if (index > maxIndex) {
                maxIndex = index;
            }
            return maxIndex;
        }

        private double randomOrder(int index) {
            long mixed = seed ^ (long) index * 0x9E3779B97F4A7C15L;
            mixed = mixed ^ (mixed >>> 30);
            mixed *= 0xBF58476D1CE4E5B9L;
            mixed = mixed ^ (mixed >>> 27);
            mixed *= 0x94D049BB133111EBL;
            mixed = mixed ^ (mixed >>> 31);
            return ((mixed >>> 11) & 0x1FFFFFFFFFFFFFL) / (double) (1L << 53);
        }
    }
}
