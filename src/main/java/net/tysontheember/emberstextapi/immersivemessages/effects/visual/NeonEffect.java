package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * High-performance neon/glow effect with Gaussian-like falloff.
 * <p>
 * Creates a realistic glow effect using multi-ring sampling with alpha falloff.
 * Optimized for performance with quality presets and pre-computed trigonometry.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code r} (radius, default: 2.0) - Glow radius in pixels (0.5-8.0)</li>
 *   <li>{@code i} (intensity, default: 1.0) - Glow brightness multiplier (0.1-3.0)</li>
 *   <li>{@code q} (quality, default: 2) - Quality preset: 1=fast, 2=balanced, 3=quality</li>
 *   <li>{@code c} (color, optional) - Glow color override (hex: FF0000, #FF0000)</li>
 *   <li>{@code p} (pulse, default: 0) - Pulse animation speed (0=disabled)</li>
 *   <li>{@code f} (falloff, default: 2.0) - Alpha falloff curve power (1.0=linear, 2.0=quadratic)</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <neon>Glowing Text</neon>
 * <neon r=3 i=1.5>Stronger Glow</neon>
 * <neon q=1>Fast/Low Quality</neon>
 * <neon q=3>High Quality</neon>
 * <neon c=00FFFF>Cyan Glow</neon>
 * <neon p=2>Pulsing Glow</neon>
 * <neon r=4 f=1.5>Wide Soft Glow</neon>
 * }</pre>
 *
 * <h3>Quality Presets:</h3>
 * <ul>
 *   <li><b>q=1 (Fast):</b> 6 samples - minimal impact, suitable for many glowing elements</li>
 *   <li><b>q=2 (Balanced):</b> 12 samples - good visual quality with reasonable performance</li>
 *   <li><b>q=3 (Quality):</b> 20 samples - best visual quality for hero text</li>
 * </ul>
 *
 * <h3>Performance Notes:</h3>
 * <ul>
 *   <li>Uses pre-computed trigonometry lookup table</li>
 *   <li>Multi-ring sampling provides better visuals with fewer samples than single-ring</li>
 *   <li>Alpha falloff reduces visual noise while maintaining glow appearance</li>
 *   <li>Capped at maximum 24 samples regardless of settings</li>
 * </ul>
 */
public class NeonEffect extends BaseEffect {

    // ===== Pre-computed Trigonometry Lookup =====
    // 24 sample points evenly distributed around a circle
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

    // ===== Quality Preset Configurations =====
    // Each preset defines: [innerRingSamples, outerRingSamples, innerRadiusRatio]
    private static final int[][] QUALITY_PRESETS = {
            {4, 2, 50},   // q=1: Fast - 6 total samples (4 inner + 2 outer), inner at 50% radius
            {6, 6, 45},   // q=2: Balanced - 12 total samples, inner at 45% radius
            {8, 12, 40},  // q=3: Quality - 20 total samples, inner at 40% radius
    };

    // Maximum absolute samples to prevent performance issues
    // With 3 rings: center (4) + inner (up to 8) + outer (up to 12) = 24 max for quality 3
    private static final int MAX_SAMPLES = 28;

    // ===== Effect Parameters =====
    private final float radius;
    private final float intensity;
    private final int quality;
    private final float pulseSpeed;
    private final float falloffPower;

    // Color override (null = use text color)
    @Nullable
    private final float[] glowColor;

    // Pre-computed from quality preset
    private final int innerSamples;
    private final int outerSamples;
    private final float innerRadiusRatio;

    /**
     * Creates a new neon effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public NeonEffect(@NotNull Params params) {
        super(params);

        // Parse and clamp parameters
        this.radius = clamp(params.getDouble("r").map(Number::floatValue).orElse(2.0f), 0.5f, 8.0f);
        this.intensity = clamp(params.getDouble("i").map(Number::floatValue).orElse(1.0f), 0.1f, 3.0f);
        this.quality = (int) clamp(params.getDouble("q").orElse(2.0), 1, 3);
        this.pulseSpeed = Math.max(0f, params.getDouble("p").map(Number::floatValue).orElse(0.0f));
        this.falloffPower = clamp(params.getDouble("f").map(Number::floatValue).orElse(2.0f), 0.5f, 4.0f);

        // Parse optional color override
        this.glowColor = parseColor(params, "c", null);

        // Load quality preset
        int[] preset = QUALITY_PRESETS[quality - 1];
        this.innerSamples = Math.min(preset[0], MAX_SAMPLES / 2);
        this.outerSamples = Math.min(preset[1], MAX_SAMPLES - innerSamples);
        this.innerRadiusRatio = preset[2] / 100.0f;
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Calculate pulse modulation if enabled
        float pulseModifier = 1.0f;
        if (pulseSpeed > 0) {
            double time = Util.getMillis() * 0.001 * pulseSpeed;
            // Smooth sine wave pulse between 0.7 and 1.0
            pulseModifier = 0.85f + 0.15f * (float) Math.sin(time * Math.PI * 2);
        }

        float effectiveIntensity = intensity * pulseModifier;

        // Determine glow color
        float glowR = glowColor != null ? glowColor[0] : settings.r;
        float glowG = glowColor != null ? glowColor[1] : settings.g;
        float glowB = glowColor != null ? glowColor[2] : settings.b;

        // === Center glow (very close to text, brightest) ===
        float centerAlpha = calculateAlpha(0.0f, effectiveIntensity);
        addGlowRing(settings, radius * 0.25f, Math.max(4, innerSamples / 2), centerAlpha, glowR, glowG, glowB);

        // === Inner Ring (closer to text, high alpha) ===
        float innerRadius = radius * innerRadiusRatio;
        float innerAlpha = calculateAlpha(innerRadiusRatio, effectiveIntensity);
        addGlowRing(settings, innerRadius, innerSamples, innerAlpha, glowR, glowG, glowB);

        // === Outer Ring (further from text, lower alpha with falloff) ===
        float outerAlpha = calculateAlpha(1.0f, effectiveIntensity);
        addGlowRing(settings, radius, outerSamples, outerAlpha, glowR, glowG, glowB);
    }

    /**
     * Add a ring of glow samples at the specified radius.
     */
    private void addGlowRing(EffectSettings settings, float ringRadius, int samples,
                             float alpha, float r, float g, float b) {
        if (samples <= 0 || alpha <= 0.01f) {
            return;
        }

        // Calculate step size through the LUT
        // Distribute samples evenly, using modular arithmetic for the LUT
        int lutStep = LUT_SIZE / samples;
        if (lutStep < 1) lutStep = 1;

        for (int i = 0; i < samples; i++) {
            int lutIndex = (i * lutStep) % LUT_SIZE;

            float offsetX = COS_LUT[lutIndex] * ringRadius;
            float offsetY = SIN_LUT[lutIndex] * ringRadius;

            // Create glow layer
            EffectSettings glowLayer = settings.copy();
            glowLayer.x += offsetX;
            glowLayer.y += offsetY;

            // Apply glow color and alpha
            glowLayer.r = r;
            glowLayer.g = g;
            glowLayer.b = b;
            glowLayer.a *= alpha;

            settings.addSibling(glowLayer);
        }
    }

    /**
     * Calculate alpha with Gaussian-like falloff.
     * Distance 0 = full intensity, distance 1 = minimum intensity
     *
     * @param normalizedDistance Distance from center (0.0 to 1.0)
     * @param intensityMod Intensity modifier
     * @return Calculated alpha value
     */
    private float calculateAlpha(float normalizedDistance, float intensityMod) {
        // Inverse falloff: alpha decreases as distance increases
        // Use softer falloff curve for more visible outer glow
        // At distance 0: full falloff (1.0)
        // At distance 1: falloff of 0.5^power
        float falloff = (float) Math.pow(1.0f - normalizedDistance * 0.5f, falloffPower);

        // Base alpha tuned for clearly visible glow
        // With default falloffPower=2 and intensity=1:
        // - Inner ring (dist 0.45): falloff=0.60, alpha = 0.35 * 0.60 = 0.21
        // - Outer ring (dist 1.0): falloff=0.25, alpha = 0.35 * 0.25 = 0.09
        float baseAlpha = 0.35f;

        return Math.min(0.7f, baseAlpha * intensityMod * falloff);
    }

    /**
     * Clamp a float value between min and max.
     */
    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Clamp a double value between min and max.
     */
    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @NotNull
    @Override
    public String getName() {
        return "neon";
    }
}
