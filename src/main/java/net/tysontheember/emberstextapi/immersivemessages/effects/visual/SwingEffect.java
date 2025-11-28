package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

/**
 * Swing rotation effect that makes characters rotate back and forth.
 * <p>
 * Applies sinusoidal rotation to characters, creating a swinging or rocking motion.
 * Characters rotate around their center point.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code a} (amplitude, default: 1.0) - Maximum rotation angle in radians (multiplied by 0.5)</li>
 *   <li>{@code f} (frequency, default: 1.0) - Swing speed</li>
 *   <li>{@code w} (wave, default: 0.0) - Phase offset between characters</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <swing>Swinging Text</swing>
 * <swing a=2.0>Strong Swing</swing>
 * <swing f=2.0>Fast Swing</swing>
 * <swing w=0.3>Wave Swing</swing>
 * }</pre>
 *
 * <h3>Notes:</h3>
 * <ul>
 *   <li>Rotation is in radians (1.0 ≈ 57 degrees)</li>
 *   <li>Final rotation = sin(time) * amplitude * 0.5</li>
 *   <li>Requires rotation support in the renderer</li>
 * </ul>
 */
public class SwingEffect extends BaseEffect {

    private final float amp;
    private final float speed;
    private final float phase;

    /**
     * Creates a new swing effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public SwingEffect(@NotNull Params params) {
        super(params);
        this.amp = params.getDouble("a").map(Number::floatValue).orElse(1.0f);
        this.speed = params.getDouble("f").map(Number::floatValue).orElse(1.0f);
        this.phase = params.getDouble("w").map(Number::floatValue).orElse(0.0f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Calculate time with speed and phase offset
        float t = Util.getMillis() * 0.003f * speed + settings.index * phase;

        // Apply sinusoidal rotation
        // amp * 0.5 keeps rotations reasonable (1.0 amp = ±0.5 radians = ±28.6°)
        settings.rot += Mth.sin(t) * amp * 0.5f;
    }

    @NotNull
    @Override
    public String getName() {
        return "swing";
    }
}
