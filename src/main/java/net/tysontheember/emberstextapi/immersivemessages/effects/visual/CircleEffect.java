package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;

/**
 * Circle effect that creates circular motion around the origin.
 * <p>
 * Makes characters move in a circle around their original position,
 * creating a rotating or orbiting appearance. The circular motion
 * is smooth and continuous.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code a} (amplitude/radius, default: 1.0) - Circle radius</li>
 *   <li>{@code f} (frequency, default: 1.0) - Rotation speed</li>
 *   <li>{@code w} (wave, default: 0.0) - Phase offset between characters</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <circle>Orbiting Text</circle>
 * <circle a=2.0>Large Circle</circle>
 * <circle f=2.0>Fast Rotation</circle>
 * <circle w=0.2>Wave Circle</circle>
 * <circle a=1.5 f=2.0>Fast Large Orbit</circle>
 * }</pre>
 *
 * <h3>Notes:</h3>
 * <ul>
 *   <li>Creates smooth circular motion using cos/sin</li>
 *   <li>Amplitude controls the radius of the circle</li>
 *   <li>Frequency controls rotation speed</li>
 *   <li>Wave parameter creates phase offset between characters</li>
 * </ul>
 */
public class CircleEffect extends BaseEffect {

    private final float radius;
    private final float speed;
    private final float phase;

    /**
     * Creates a new circle effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public CircleEffect(@NotNull Params params) {
        super(params);
        this.radius = ValidationHelper.clamp("circle", "a",
                params.getDouble("a").map(Number::floatValue).orElse(1.0f), 0f, 50f);
        this.speed = ValidationHelper.clamp("circle", "f",
                params.getDouble("f").map(Number::floatValue).orElse(1.0f), 0.01f, 100f);
        this.phase = ValidationHelper.clamp("circle", "w",
                params.getDouble("w").map(Number::floatValue).orElse(0.0f), 0f, 10f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Calculate time with speed and character phase offset
        // 0.002 controls rotation speed, index * phase creates offset between characters
        float t = Util.getMillis() * 0.002f * speed + settings.index * phase;

        // Apply circular motion using cos/sin
        settings.x += Mth.cos(t) * radius;
        settings.y += Mth.sin(t) * radius;
    }

    @NotNull
    @Override
    public String getName() {
        return "circle";
    }
}
