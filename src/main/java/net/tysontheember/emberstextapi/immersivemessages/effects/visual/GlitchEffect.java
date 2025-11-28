package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Glitch effect that creates digital distortion with slicing, jitter, and flicker.
 * <p>
 * Simulates screen glitches with multiple effects:
 * <ul>
 *   <li>Position jitter (random horizontal/vertical displacement)</li>
 *   <li>Alpha blink (sudden transparency changes)</li>
 *   <li>Horizontal slicing (splits character into offset layers with masking)</li>
 * </ul>
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code f} (frequency, default: 1.0) - Glitch speed/intensity</li>
 *   <li>{@code j} (jitter chance, default: 0.015) - Probability of position jitter per frame</li>
 *   <li>{@code b} (blink chance, default: 0.003) - Probability of alpha blink per frame</li>
 *   <li>{@code s} (shift chance, default: 0.08) - Probability of horizontal slicing per frame</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <glitch>ERROR</glitch>
 * <glitch f=2.0>Fast Glitch</glitch>
 * <glitch j=0.03 b=0.01>Intense Jitter</glitch>
 * <glitch s=0.15>More Slicing</glitch>
 * }</pre>
 *
 * <h3>Technical Details:</h3>
 * <ul>
 *   <li><b>Pulse System:</b> 3-phase cycle (0, 1, 2) with phase 1 being active</li>
 *   <li><b>Jitter:</b> ±8px horizontal, ±4px vertical displacement</li>
 *   <li><b>Blink:</b> Sets alpha to 0% or 30%</li>
 *   <li><b>Slicing:</b> Creates sibling layers with:
 *     <ul>
 *       <li>Horizontal offset (±0.75-1.5px)</li>
 *       <li>Vertical masking (maskTop/maskBottom)</li>
 *       <li>Modified alpha and colors</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h3>Notes:</h3>
 * <ul>
 *   <li>Uses per-character random seeds for variety</li>
 *   <li>Shadow layer gets special color shifting when sliced</li>
 *   <li>Multiple sibling layers create the sliced appearance</li>
 * </ul>
 */
public class GlitchEffect extends BaseEffect {

    private final float frequency;
    private final float jitterChance;
    private final float blinkChance;
    private final float shiftChance;

    /**
     * Creates a new glitch effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public GlitchEffect(@NotNull Params params) {
        super(params);
        this.frequency = params.getDouble("f").map(Number::floatValue).orElse(1.0f);
        this.jitterChance = params.getDouble("j").map(Number::floatValue).orElse(0.015f);
        this.blinkChance = params.getDouble("b").map(Number::floatValue).orElse(0.003f);
        this.shiftChance = params.getDouble("s").map(Number::floatValue).orElse(0.08f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        double time = Util.getMillis() * 0.025 * frequency;

        // 3-phase pulse system (0, 1, 2)
        int pulse = (int) time % 3;

        // Per-character random seed for consistent but varied effects
        Random random = new Random(settings.index + settings.codepoint + (long) (time * 1000));
        random.nextFloat(); // Skip one for variety

        // === JITTER EFFECT ===
        // Only jitters during pulse phase 1
        if (pulse == 1 && random.nextFloat() < jitterChance) {
            settings.x += (random.nextFloat() - 0.5f) * 8f;  // ±4px horizontal
            settings.y += (random.nextFloat() - 0.5f) * 4f;  // ±2px vertical
        }

        // === BLINK EFFECT ===
        // Randomly makes character mostly or fully transparent
        if (random.nextFloat() < blinkChance) {
            settings.a *= random.nextFloat() < 0.3f ? 0.0f : 0.3f;
        }

        // === SLICE/SHIFT EFFECT ===
        // Create horizontal slicing with sibling layers
        time *= 2;  // Double speed for slicing
        Random random2 = new Random((long) time * 1000L * hashCode());

        if (random2.nextFloat() < shiftChance) {
            // If this is a shadow, adjust for shadow offset first
            if (settings.isShadow) {
                // Create sibling for full character (before slicing)
                settings.siblings.add(settings.copy());
                settings.x -= settings.shadowOffset;
                settings.y -= settings.shadowOffset;
            }

            // Calculate mask split point (where to slice the character)
            // 0.5 ± 0.25 = range of 0.25 to 0.75 (avoids slicing too close to edges)
            float mask = 0.5f + (random2.nextFloat() - 0.5f) * 0.5f;

            // Calculate horizontal offset for the slice
            float offset = 0.75f + random2.nextFloat() * 0.75f;  // 0.75 to 1.5
            if (random2.nextBoolean()) {
                offset = -offset;  // Random direction
            }

            // === CREATE TOP SLICE ===
            EffectSettings topSlice = settings.copy();
            topSlice.x += offset;
            topSlice.a *= Math.min(1f, 0.5f + random2.nextFloat());  // Random alpha

            // Shadow layer gets special color shifting
            if (topSlice.isShadow) {
                topSlice.y -= 1;
                topSlice.r = topSlice.r > 0.5f ? topSlice.r - 0.5f : topSlice.r + 0.5f;
                topSlice.g *= 0.2f;
                topSlice.b *= 0.2f;
            }

            topSlice.maskBottom = mask;  // Top portion: show from 0 to mask
            settings.siblings.add(topSlice);

            // === MODIFY MAIN (BOTTOM SLICE) ===
            settings.maskTop = 1 - mask;  // Bottom portion: show from mask to 1
            settings.x -= offset;  // Opposite direction
            settings.a *= Math.min(1f, 0.5f + random2.nextFloat());

            if (settings.isShadow) {
                settings.y += 1;
                settings.r *= 0.2f;
                settings.g = settings.g > 0.5f ? settings.g - 0.5f : settings.g + 0.5f;
                settings.b = settings.b > 0.5f ? settings.b - 0.5f : settings.b + 0.5f;
            }
        }
    }

    @NotNull
    @Override
    public String getName() {
        return "glitch";
    }
}
