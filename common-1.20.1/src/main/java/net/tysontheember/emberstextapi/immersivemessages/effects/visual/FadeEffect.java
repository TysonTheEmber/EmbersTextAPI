package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Fade effect that creates smooth alpha transparency oscillation.
 * <p>
 * Modulates character transparency over time using a sine wave, creating
 * a pulsing fade in/out effect. The minimum alpha can be controlled to
 * prevent complete invisibility.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code a} (min alpha, default: 0.3) - Minimum transparency (0.0=invisible, 1.0=opaque)</li>
 *   <li>{@code f} (frequency, default: 1.0) - Fade speed</li>
 *   <li>{@code w} (wave, default: 0.0) - Phase offset between characters</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <fade>Fading Text</fade>
 * <fade a=0.0>Full Fade (invisible at min)</fade>
 * <fade f=2.0>Fast Fade</fade>
 * <fade w=0.2>Wave Fade</fade>
 * <fade a=0.5 f=1.5>Gentle Fade</fade>
 * }</pre>
 *
 * <h3>Notes:</h3>
 * <ul>
 *   <li>Alpha oscillates between minA and 1.0</li>
 *   <li>Formula: alpha *= minA + (1 - minA) * (0.5 + 0.5 * sin(time))</li>
 *   <li>Different from PulseEffect which affects brightness, not transparency</li>
 * </ul>
 */
public class FadeEffect extends BaseEffect {

    private final float minA;
    private final float speed;
    private final float phase;

    /**
     * Creates a new fade effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public FadeEffect(@NotNull Params params) {
        super(params);
        this.minA = ValidationHelper.clamp("fade", "a",
                params.getDouble("a").map(Number::floatValue).orElse(0.3f), 0f, 1f);
        this.speed = ValidationHelper.clamp("fade", "f",
                params.getDouble("f").map(Number::floatValue).orElse(1.0f), 0.01f, 100f);
        this.phase = ValidationHelper.clamp("fade", "w",
                params.getDouble("w").map(Number::floatValue).orElse(0.0f), 0f, 10f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Calculate time with speed and phase offset
        float t = Util.getMillis() * 0.002f * speed + settings.index * phase;

        // Calculate alpha multiplier
        // 0.5 + 0.5 * sin(t) creates 0.0-1.0 oscillation
        // Scale to range [minA, 1.0]
        float k = minA + (1f - minA) * (0.5f + 0.5f * Mth.sin(t));

        // Apply alpha modulation
        settings.a *= k;
    }

    @NotNull
    @Override
    public String getName() {
        return "fade";
    }
}
