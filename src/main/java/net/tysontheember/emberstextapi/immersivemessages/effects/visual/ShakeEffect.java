package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

/**
 * Shake effect that creates random jittery displacement.
 * <p>
 * Applies random directional displacement to characters, creating a shaking
 * or vibrating appearance. The shake direction changes over time based on
 * a pseudo-random direction calculated from time and character properties.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code a} (amplitude, default: 1.0) - Shake intensity/distance</li>
 *   <li>{@code f} (frequency, default: 1.0) - Shake speed (how often direction changes)</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <shake>Shaky Text</shake>
 * <shake a=2.0>Intense Shake</shake>
 * <shake f=3.0>Fast Shake</shake>
 * <shake a=0.5 f=2.0>Subtle Fast Shake</shake>
 * }</pre>
 *
 * <h3>Notes:</h3>
 * <ul>
 *   <li>Uses pseudo-random direction based on time, character index, and codepoint</li>
 *   <li>Creates discrete position changes (unlike smooth turbulence)</li>
 *   <li>Direction is calculated using unit circle (cos/sin)</li>
 * </ul>
 */
public class ShakeEffect extends BaseEffect {

    private final float amp;
    private final float speed;

    /**
     * Creates a new shake effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public ShakeEffect(@NotNull Params params) {
        super(params);
        this.amp = params.getDouble("a").map(Number::floatValue).orElse(1.0f);
        this.speed = params.getDouble("f").map(Number::floatValue).orElse(1.0f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Calculate pseudo-random seed from time and character properties
        // 0.01 * speed creates discrete time steps for shake direction changes
        int seed = (int) (Util.getMillis() * 0.01f * speed + settings.codepoint + settings.index);

        // Get pseudo-random direction using unit circle
        // Use modulo to create cyclic pattern from seed
        float angle = (seed % 30) * (Mth.TWO_PI / 30f);
        float dirX = Mth.cos(angle);
        float dirY = Mth.sin(angle);

        // Apply shake displacement
        // 0.6 scales the unit vector to reasonable shake distance
        settings.x += dirX * 0.6f * amp;
        settings.y += dirY * 0.6f * amp;
    }

    @NotNull
    @Override
    public String getName() {
        return "shake";
    }
}
