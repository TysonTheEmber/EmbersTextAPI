package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Pulse brightness effect that creates breathing/pulsing brightness fluctuation.
 * <p>
 * Modulates the overall brightness of characters over time using a sine wave,
 * creating a pulsing or breathing effect. Does not affect the shadow layer.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code base} (min brightness, default: 0.75) - Minimum brightness multiplier (0.0-1.0)</li>
 *   <li>{@code a} (amplitude, default: 1.0) - Brightness variation amount</li>
 *   <li>{@code f} (frequency, default: 1.0) - Pulse speed</li>
 *   <li>{@code w} (wave, default: 0.0) - Phase offset between characters</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <pulse>Pulsing Text</pulse>
 * <pulse base=0.5 a=1.5>Strong Pulse</pulse>
 * <pulse f=2.0>Fast Pulse</pulse>
 * <pulse w=0.2>Wave Pulse</pulse>
 * }</pre>
 *
 * <h3>Notes:</h3>
 * <ul>
 *   <li>Shadow layer is skipped (shadows remain unaffected)</li>
 *   <li>Brightness is multiplied, so base color affects final result</li>
 *   <li>Formula: brightness = base + amp * 0.25 * (0.5 + 0.5 * sin(time))</li>
 * </ul>
 */
public class PulseEffect extends BaseEffect {

    private final float base;
    private final float amp;
    private final float speed;
    private final float phase;

    /**
     * Creates a new pulse effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public PulseEffect(@NotNull Params params) {
        super(params);
        this.base = ValidationHelper.clamp("pulse", "base",
                params.getDouble("base").map(Number::floatValue).orElse(0.75f), 0f, 1f);
        this.amp = ValidationHelper.clamp("pulse", "a",
                params.getDouble("a").map(Number::floatValue).orElse(1.0f), 0f, 5f);
        this.speed = ValidationHelper.clamp("pulse", "f",
                params.getDouble("f").map(Number::floatValue).orElse(1.0f), 0.01f, 100f);
        this.phase = ValidationHelper.clamp("pulse", "w",
                params.getDouble("w").map(Number::floatValue).orElse(0.0f), 0f, 10f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Skip shadow layer - shadows remain unaffected
        if (settings.isShadow) {
            return;
        }

        // Calculate time with speed and phase offset
        float t = Util.getMillis() * 0.002f * speed + settings.index * phase;

        // Calculate brightness multiplier
        // 0.5 + 0.5 * sin(t) creates a 0.0-1.0 oscillation
        // Multiplied by amp * 0.25 and added to base
        float k = base + amp * 0.25f * (0.5f + 0.5f * Mth.sin(t));

        // Apply brightness to all color channels
        settings.r *= k;
        settings.g *= k;
        settings.b *= k;
    }

    @NotNull
    @Override
    public String getName() {
        return "pulse";
    }
}
