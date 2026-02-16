package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

/**
 * Glitch effect that creates digital distortion with slicing, jitter, and color shifts.
 * <p>
 * Simulates digital display errors with horizontal slice displacement, position jitter,
 * alpha blinks, and RGB color channel shifting on shadows. The effect runs continuously
 * with configurable probability-based triggering for an authentic glitchy appearance.
 * </p>
 *
 * <h3>Visual Effects:</h3>
 * <ul>
 *   <li><b>Slice Displacement:</b> Splits characters horizontally with opposite offsets</li>
 *   <li><b>Position Jitter:</b> Random position displacement during pulse phases</li>
 *   <li><b>Alpha Blink:</b> Random transparency flickers</li>
 *   <li><b>Shadow Color Shift:</b> RGB channel manipulation on shadow layers</li>
 *   <li><b>Chromatic Fringing:</b> Optional RGB separation on displaced slices</li>
 * </ul>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code f} (frequency, default: 1.0) - Animation speed multiplier</li>
 *   <li>{@code s} (shift/slice chance, default: 0.08) - Probability of slice displacement per frame</li>
 *   <li>{@code j} (jitter chance, default: 0.015) - Probability of position jitter per frame</li>
 *   <li>{@code b} (blink chance, default: 0.003) - Probability of alpha blink per frame</li>
 *   <li>{@code o} (offset, default: 1.0) - Slice offset multiplier (affects displacement distance)</li>
 *   <li>{@code c} (chromatic, default: 0.0) - Chromatic aberration intensity on slices</li>
 *   <li>{@code slices} (default: 2) - Number of horizontal slices (2-5)</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <glitch>ERROR</glitch>
 * <glitch s=0.15>More Frequent Glitches</glitch>
 * <glitch f=2.0>Faster Animation</glitch>
 * <glitch o=2.0>Larger Displacement</glitch>
 * <glitch c=0.5>With Chromatic Aberration</glitch>
 * <glitch slices=4 s=0.2>Multi-slice Chaos</glitch>
 * <glitch j=0.05 b=0.01>More Jitter and Blinks</glitch>
 * }</pre>
 *
 * <h3>Technical Details:</h3>
 * <ul>
 *   <li>Uses a 3-phase pulse system for jitter timing</li>
 *   <li>Slice effect runs at 2x speed for snappier response</li>
 *   <li>Shadow layers get complementary color shifts for depth</li>
 *   <li>All randomness is seeded for deterministic per-character variation</li>
 * </ul>
 */
public class GlitchEffect extends BaseEffect {

    private final float frequency;
    private final float shiftChance;
    private final float jitterChance;
    private final float blinkChance;
    private final float offsetMultiplier;
    private final float chromatic;
    private final int numSlices;

    /**
     * Creates a new glitch effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public GlitchEffect(@NotNull Params params) {
        super(params);
        this.frequency = params.getDouble("f").map(Number::floatValue).orElse(1.0f);
        this.shiftChance = params.getDouble("s").map(Number::floatValue).orElse(0.08f);
        this.jitterChance = params.getDouble("j").map(Number::floatValue).orElse(0.015f);
        this.blinkChance = params.getDouble("b").map(Number::floatValue).orElse(0.003f);
        this.offsetMultiplier = params.getDouble("o").map(Number::floatValue).orElse(1.0f);
        this.chromatic = params.getDouble("c").map(Number::floatValue).orElse(0.0f);
        this.numSlices = (int) Math.max(2, Math.min(5, params.getDouble("slices").orElse(2.0)));
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        double time = Util.getMillis() * 0.025 * frequency;
        int pulse = (int) time % 3;

        // Generate deterministic random values based on character and time
        long seed = settings.index + settings.codepoint + (long) (time * 1000);

        // === JITTER EFFECT ===
        // Only triggers during pulse phase 1
        if (pulse == 1 && seedToFloat(seed) < jitterChance) {
            float jitterX = (seedToFloat(seed * 31 + 1) - 0.5f) * 8f * offsetMultiplier;
            float jitterY = (seedToFloat(seed * 31 + 2) - 0.5f) * 4f * offsetMultiplier;
            settings.x += jitterX;
            settings.y += jitterY;
        }

        // === BLINK EFFECT ===
        if (seedToFloat(seed * 37 + 3) < blinkChance) {
            settings.a *= seedToFloat(seed * 37 + 4) < 0.3f ? 0.0f : 0.3f;
        }

        // === SLICE/SHIFT EFFECT ===
        // Runs at 2x speed for snappier glitches
        double sliceTime = time * 2;
        long sliceSeed = (long) sliceTime * 1000L * hashCode();

        if (seedToFloat(sliceSeed) < shiftChance) {
            if (numSlices == 2) {
                applyTwoSliceGlitch(settings, sliceSeed);
            } else {
                applyMultiSliceGlitch(settings, sliceSeed);
            }
        }
    }

    /**
     * Applies the classic 2-slice glitch effect (like Text Animator).
     * Splits character into top and bottom with opposite horizontal offsets.
     */
    private void applyTwoSliceGlitch(EffectSettings settings, long seed) {
        // Handle shadow layer specially - create unshifted copy first
        if (settings.isShadow) {
            settings.addSibling(settings.copy());
            settings.x -= settings.shadowOffset;
            settings.y -= settings.shadowOffset;
        }

        // Calculate split point (0.25 to 0.75 range)
        float mask = 0.5f + (seedToFloat(seed * 41 + 1) - 0.5f) * 0.5f;

        // Calculate offset (0.75 to 1.5 base, scaled by multiplier)
        float offset = (0.75f + seedToFloat(seed * 41 + 2) * 0.75f) * offsetMultiplier;
        if (seedToFloat(seed * 41 + 3) < 0.5f) {
            offset = -offset;
        }

        // === TOP SLICE (sibling) ===
        EffectSettings topSlice = settings.copy();
        topSlice.x += offset;
        topSlice.a *= Math.min(1f, 0.5f + seedToFloat(seed * 41 + 4));
        topSlice.maskBottom = mask;

        // Shadow color shift for top slice
        if (topSlice.isShadow) {
            topSlice.y -= 1;
            topSlice.r = topSlice.r > 0.5f ? topSlice.r - 0.5f : topSlice.r + 0.5f;
            topSlice.g *= 0.2f;
            topSlice.b *= 0.2f;
        }

        // Add chromatic aberration to top slice if enabled
        if (chromatic > 0 && Math.abs(offset) > 0.3f) {
            addSliceChromatic(settings, topSlice, offset);
        }

        settings.addSibling(topSlice);

        // === BOTTOM SLICE (main settings) ===
        settings.maskTop = 1 - mask;
        settings.x -= offset;
        settings.a *= Math.min(1f, 0.5f + seedToFloat(seed * 41 + 5));

        // Shadow color shift for bottom slice
        if (settings.isShadow) {
            settings.y += 1;
            settings.r *= 0.2f;
            settings.g = settings.g > 0.5f ? settings.g - 0.5f : settings.g + 0.5f;
            settings.b = settings.b > 0.5f ? settings.b - 0.5f : settings.b + 0.5f;
        }

        // Add chromatic aberration to bottom slice if enabled
        if (chromatic > 0 && Math.abs(offset) > 0.3f) {
            addSliceChromatic(settings, settings, -offset);
        }
    }

    /**
     * Applies a multi-slice glitch effect for more chaotic appearance.
     * Splits character into multiple horizontal bands with varying offsets.
     */
    private void applyMultiSliceGlitch(EffectSettings settings, long seed) {
        // Handle shadow layer specially
        if (settings.isShadow) {
            settings.addSibling(settings.copy());
            settings.x -= settings.shadowOffset;
            settings.y -= settings.shadowOffset;
        }

        // Generate slice boundaries
        float[] boundaries = new float[numSlices + 1];
        boundaries[0] = 0f;
        boundaries[numSlices] = 1f;

        for (int i = 1; i < numSlices; i++) {
            float minBound = boundaries[i - 1] + (0.8f / numSlices);
            float maxBound = 1f - (numSlices - i) * (0.8f / numSlices);
            boundaries[i] = minBound + seedToFloat(seed * 47 + i * 7) * (maxBound - minBound);
        }

        // Generate offsets for each slice (alternating directions)
        float[] offsets = new float[numSlices];
        for (int i = 0; i < numSlices; i++) {
            float baseOffset = (0.75f + seedToFloat(seed * 53 + i * 11) * 0.75f) * offsetMultiplier;
            offsets[i] = (i % 2 == 0) ? baseOffset : -baseOffset;
            // Add some randomness to direction
            if (seedToFloat(seed * 59 + i * 13) < 0.3f) {
                offsets[i] = -offsets[i];
            }
        }

        // First slice becomes the main settings
        settings.maskTop = boundaries[0];
        settings.maskBottom = 1f - boundaries[1];
        settings.x += offsets[0];
        applySliceShadowColor(settings, 0);

        if (chromatic > 0 && Math.abs(offsets[0]) > 0.3f) {
            addSliceChromatic(settings, settings, offsets[0]);
        }

        // Create siblings for remaining slices
        for (int i = 1; i < numSlices; i++) {
            EffectSettings slice = settings.copy();
            slice.maskTop = boundaries[i];
            slice.maskBottom = 1f - boundaries[i + 1];
            slice.x += offsets[i] - offsets[0]; // Relative to first slice offset
            slice.a *= Math.min(1f, 0.5f + seedToFloat(seed * 61 + i * 17));

            applySliceShadowColor(slice, i);

            if (chromatic > 0 && Math.abs(offsets[i]) > 0.3f) {
                addSliceChromatic(settings, slice, offsets[i]);
            }

            settings.addSibling(slice);
        }
    }

    /**
     * Applies color shifting to shadow layers for visual depth.
     */
    private void applySliceShadowColor(EffectSettings slice, int sliceIndex) {
        if (!slice.isShadow) return;

        // Alternate color shift patterns based on slice index
        switch (sliceIndex % 3) {
            case 0:
                slice.r = slice.r > 0.5f ? slice.r - 0.5f : slice.r + 0.5f;
                slice.g *= 0.2f;
                slice.b *= 0.2f;
                break;
            case 1:
                slice.r *= 0.2f;
                slice.g = slice.g > 0.5f ? slice.g - 0.5f : slice.g + 0.5f;
                slice.b *= 0.2f;
                break;
            case 2:
                slice.r *= 0.2f;
                slice.g *= 0.2f;
                slice.b = slice.b > 0.5f ? slice.b - 0.5f : slice.b + 0.5f;
                break;
        }
    }

    /**
     * Adds chromatic aberration (RGB fringing) to a slice.
     */
    private void addSliceChromatic(EffectSettings parent, EffectSettings slice, float offset) {
        float chromaticOffset = Math.signum(offset) * chromatic;

        // Red fringe
        EffectSettings redFringe = slice.copy();
        redFringe.x += chromaticOffset * 0.6f;
        redFringe.g = 0f;
        redFringe.b = 0f;
        redFringe.a *= 0.4f;
        parent.addSibling(redFringe);

        // Cyan fringe
        EffectSettings cyanFringe = slice.copy();
        cyanFringe.x -= chromaticOffset * 0.6f;
        cyanFringe.r = 0f;
        cyanFringe.a *= 0.4f;
        parent.addSibling(cyanFringe);
    }

    /**
     * Convert a seed to a deterministic float in range [0.0, 1.0).
     */
    private static float seedToFloat(long seed) {
        seed = (seed ^ (seed >>> 33)) * 0xff51afd7ed558ccdL;
        seed = (seed ^ (seed >>> 33)) * 0xc4ceb9fe1a85ec53L;
        seed = seed ^ (seed >>> 33);
        return (seed & 0x7FFFFFFFL) / (float) 0x80000000L;
    }

    @NotNull
    @Override
    public String getName() {
        return "glitch";
    }
}
