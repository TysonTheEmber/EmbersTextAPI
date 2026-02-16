package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Wiggle effect that creates organic oscillating displacement.
 * <p>
 * Makes characters wiggle in a consistent direction (based on their codepoint)
 * with sinusoidal motion. Unlike turbulence which uses multiple frequencies,
 * wiggle creates a simple back-and-forth motion in a character-specific direction.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code a} (amplitude, default: 1.0) - Wiggle distance/intensity</li>
 *   <li>{@code f} (frequency, default: 1.0) - Wiggle speed</li>
 *   <li>{@code w} (wave, default: 1.0) - Phase offset between characters</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <wiggle>Wiggling Text</wiggle>
 * <wiggle a=2.0>Strong Wiggle</wiggle>
 * <wiggle f=2.0>Fast Wiggle</wiggle>
 * <wiggle w=0.5>Wave Wiggle</wiggle>
 * }</pre>
 *
 * <h3>Notes:</h3>
 * <ul>
 *   <li>Each character wiggles in a consistent direction based on its codepoint</li>
 *   <li>Direction is calculated using unit circle (30 evenly spaced directions)</li>
 *   <li>Creates smoother, more organic motion than shake effect</li>
 *   <li>Different from turbulence: single frequency, character-specific direction</li>
 * </ul>
 */
public class WiggleEffect extends BaseEffect {

    private final float amp;
    private final float speed;
    private final float phase;

    /**
     * Creates a new wiggle effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public WiggleEffect(@NotNull Params params) {
        super(params);
        this.amp = ValidationHelper.clamp("wiggle", "a",
                params.getDouble("a").map(Number::floatValue).orElse(1.0f), 0f, 50f);
        this.speed = ValidationHelper.clamp("wiggle", "f",
                params.getDouble("f").map(Number::floatValue).orElse(1.0f), 0.01f, 100f);
        this.phase = ValidationHelper.clamp("wiggle", "w",
                params.getDouble("w").map(Number::floatValue).orElse(1.0f), 0f, 10f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Get consistent direction for this character based on codepoint
        // Use modulo 30 to create 30 evenly spaced directions around the unit circle
        float angle = (settings.codepoint % 30) * (Mth.TWO_PI / 30f);
        float dirX = Mth.cos(angle);
        float dirY = Mth.sin(angle);

        // Calculate oscillation using sine wave
        // 0.01 * speed controls wiggle speed
        // index * 2 * phase creates phase offset between characters
        float delta = Mth.sin(Util.getMillis() * 0.01f * speed + settings.index * 2f * phase) * 1.5f * amp;

        // Apply wiggle in character's direction
        settings.x += dirX * delta;
        settings.y += dirY * delta;
    }

    @NotNull
    @Override
    public String getName() {
        return "wiggle";
    }
}
