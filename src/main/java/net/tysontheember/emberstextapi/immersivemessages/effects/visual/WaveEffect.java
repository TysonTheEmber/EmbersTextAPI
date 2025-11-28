package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

/**
 * Wave effect that creates vertical undulating motion like water waves.
 * <p>
 * Makes characters move up and down in a smooth sinusoidal wave pattern.
 * Phase offset between characters creates the classic wave propagation effect.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code a} (amplitude, default: 1.0) - Wave height in pixels</li>
 *   <li>{@code f} (frequency, default: 1.0) - Wave speed</li>
 *   <li>{@code w} (wavelength, default: 1.0) - Distance between wave peaks (affects character spacing)</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <wave>Flowing Text</wave>
 * <wave a=2.0>High Waves</wave>
 * <wave f=2.0>Fast Waves</wave>
 * <wave w=0.5>Tight Waves</wave>
 * }</pre>
 *
 * <h3>Notes:</h3>
 * <ul>
 *   <li>Vertical displacement only (Y axis)</li>
 *   <li>Wavelength affects the visual "tightness" of the wave between characters</li>
 *   <li>Compatible with existing ShakeCalculator WAVE mode</li>
 * </ul>
 */
public class WaveEffect extends BaseEffect {

    private final float amplitude;
    private final float frequency;
    private final float wavelength;

    /**
     * Creates a new wave effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public WaveEffect(@NotNull Params params) {
        super(params);
        this.amplitude = params.getDouble("a").map(Number::floatValue).orElse(1.0f);
        this.frequency = params.getDouble("f").map(Number::floatValue).orElse(1.0f);
        this.wavelength = params.getDouble("w").map(Number::floatValue).orElse(1.0f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Calculate time with frequency
        // Using millis * 0.002 for smooth, visible motion
        float time = Util.getMillis() * 0.002f * frequency;

        // Add character index as phase offset
        // wavelength * 0.2 scales the phase offset between characters
        float phase = time + settings.index * wavelength * 0.2f;

        // Calculate vertical offset using sine wave
        // 2π creates full wave cycle
        float safeWavelength = Math.max(0.0001f, wavelength);  // Prevent division by zero
        float offset = Mth.sin(phase * Mth.TWO_PI / safeWavelength) * amplitude;

        // Apply to Y position
        settings.y += offset;
    }

    @NotNull
    @Override
    public String getName() {
        return "wave";
    }
}
