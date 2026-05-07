package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.util.Util;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.platform.ConfigHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NeonEffect extends BaseEffect {

    private static final int LUT_SIZE = 24;
    private static final float[] COS_LUT = new float[LUT_SIZE];
    private static final float[] SIN_LUT = new float[LUT_SIZE];

    static {
        for (int i = 0; i < LUT_SIZE; i++) {
            double angle = (Math.PI * 2.0 * i) / LUT_SIZE;
            COS_LUT[i] = (float) Math.cos(angle);
            SIN_LUT[i] = (float) Math.sin(angle);
        }
    }

    private static final int[][] QUALITY_PRESETS = {
            {4, 2, 50},
            {6, 6, 45},
            {8, 12, 40},
    };

    private static final int MAX_SAMPLES = 28;

    private final float radius;
    private final float intensity;
    private final int quality;
    private final float pulseSpeed;
    private final float falloffPower;

    @Nullable
    private final float[] glowColor;

    private final int innerSamples;
    private final int outerSamples;
    private final float innerRadiusRatio;

    public NeonEffect(@NotNull Params params) {
        super(params);

        this.radius = clamp(params.getDouble("r").map(Number::floatValue).orElse(2.0f), 0.5f, 8.0f);
        this.intensity = clamp(params.getDouble("i").map(Number::floatValue).orElse(1.0f), 0.1f, 3.0f);
        int requestedQuality = (int) clamp(params.getDouble("q").orElse(2.0), 1, 3);
        try {
            int maxQuality = ConfigHelper.getInstance().getMaxNeonQuality();
            requestedQuality = Math.min(requestedQuality, maxQuality);
        } catch (Exception ignored) {
        }
        this.quality = requestedQuality;
        this.pulseSpeed = Math.max(0f, params.getDouble("p").map(Number::floatValue).orElse(0.0f));
        this.falloffPower = clamp(params.getDouble("f").map(Number::floatValue).orElse(2.0f), 0.5f, 4.0f);

        this.glowColor = parseColor(params, "c", null);

        int[] preset = QUALITY_PRESETS[quality - 1];
        this.innerSamples = Math.min(preset[0], MAX_SAMPLES / 2);
        this.outerSamples = Math.min(preset[1], MAX_SAMPLES - innerSamples);
        this.innerRadiusRatio = preset[2] / 100.0f;
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        float pulseModifier = 1.0f;
        if (pulseSpeed > 0) {
            double time = Util.getMillis() * 0.001 * pulseSpeed;
            pulseModifier = 0.85f + 0.15f * (float) Math.sin(time * Math.PI * 2);
        }

        float effectiveIntensity = intensity * pulseModifier;

        float glowR = glowColor != null ? glowColor[0] : settings.r;
        float glowG = glowColor != null ? glowColor[1] : settings.g;
        float glowB = glowColor != null ? glowColor[2] : settings.b;

        float centerAlpha = calculateAlpha(0.0f, effectiveIntensity);
        addGlowRing(settings, radius * 0.25f, Math.max(4, innerSamples / 2), centerAlpha, glowR, glowG, glowB);

        float innerRadius = radius * innerRadiusRatio;
        float innerAlpha = calculateAlpha(innerRadiusRatio, effectiveIntensity);
        addGlowRing(settings, innerRadius, innerSamples, innerAlpha, glowR, glowG, glowB);

        float outerAlpha = calculateAlpha(1.0f, effectiveIntensity);
        addGlowRing(settings, radius, outerSamples, outerAlpha, glowR, glowG, glowB);
    }

    private void addGlowRing(EffectSettings settings, float ringRadius, int samples,
                             float alpha, float r, float g, float b) {
        if (samples <= 0 || alpha <= 0.01f) {
            return;
        }

        int lutStep = LUT_SIZE / samples;
        if (lutStep < 1) lutStep = 1;

        for (int i = 0; i < samples; i++) {
            int lutIndex = (i * lutStep) % LUT_SIZE;

            float offsetX = COS_LUT[lutIndex] * ringRadius;
            float offsetY = SIN_LUT[lutIndex] * ringRadius;

            EffectSettings glowLayer = settings.copy();
            glowLayer.x += offsetX;
            glowLayer.y += offsetY;
            glowLayer.r = r;
            glowLayer.g = g;
            glowLayer.b = b;
            glowLayer.a *= alpha;

            settings.addSibling(glowLayer);
        }
    }

    private float calculateAlpha(float normalizedDistance, float intensityMod) {
        float falloff = (float) Math.pow(1.0f - normalizedDistance * 0.5f, falloffPower);
        float baseAlpha = 0.35f;
        return Math.min(0.7f, baseAlpha * intensityMod * falloff);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @NotNull
    @Override
    public String getName() {
        return "neon";
    }
}
