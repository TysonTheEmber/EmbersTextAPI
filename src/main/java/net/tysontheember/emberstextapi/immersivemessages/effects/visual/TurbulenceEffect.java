package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Turbulence effect that creates noise-based random displacement.
 * <p>
 * Uses dual-frequency sine waves to create pseudo-random organic motion
 * that looks like wind or turbulence. Each character moves independently
 * based on its index and codepoint.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code a} (amplitude, default: 1.0) - Displacement strength</li>
 *   <li>{@code f} (frequency, default: 1.0) - Turbulence speed</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <turb>Windy Text</turb>
 * <turb a=2.0>Strong Turbulence</turb>
 * <turb f=2.0>Fast Turbulence</turb>
 * <turb a=3.0 f=0.5>Slow Drift</turb>
 * }</pre>
 *
 * <h3>Notes:</h3>
 * <ul>
 *   <li>Uses dual sine waves with different frequencies (1.7 and 2.3) for organic look</li>
 *   <li>Character index and codepoint add variety to motion</li>
 *   <li>Creates continuous, smooth displacement (not jerky like shake)</li>
 * </ul>
 */
public class TurbulenceEffect extends BaseEffect {

    private final float amp;
    private final float speed;

    /**
     * Creates a new turbulence effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public TurbulenceEffect(@NotNull Params params) {
        super(params);
        this.amp = ValidationHelper.clamp("turb", "a",
                params.getDouble("a").map(Number::floatValue).orElse(1.0f), 0f, 50f);
        this.speed = ValidationHelper.clamp("turb", "f",
                params.getDouble("f").map(Number::floatValue).orElse(1.0f), 0.01f, 100f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Amplify for visible effect
        float amplitude = this.amp * 1.5f;

        // Time component
        float t = Util.getMillis() * 0.002f * speed;

        // Calculate X displacement using sine wave
        // Frequency 1.7, phase offset by character index and codepoint
        float nx = Mth.sin(t * 1.7f + settings.index * 0.31f + settings.codepoint * 0.07f);

        // Calculate Y displacement using different frequency
        // Frequency 2.3 creates independent motion from X
        float ny = Mth.sin(t * 2.3f + settings.index * 0.27f + settings.codepoint * 0.11f);

        // Apply displacement
        settings.x += nx * amplitude;
        settings.y += ny * amplitude;
    }

    @NotNull
    @Override
    public String getName() {
        return "turb";
    }
}
