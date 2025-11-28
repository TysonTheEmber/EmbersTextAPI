package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

/**
 * Pendulum effect that creates realistic pendulum swinging motion.
 * <p>
 * Simulates a pendulum swing with sinusoidal rotation and optional circular
 * arc motion. Characters swing back and forth like a pendulum, with configurable
 * maximum angle and optional radius for arc movement.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code a} (max angle, default: 30.0) - Maximum swing angle in degrees</li>
 *   <li>{@code f} (frequency, default: 1.0) - Swing speed</li>
 *   <li>{@code r} (radius, default: 0.0) - Arc radius for circular motion (0 = rotation only)</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <pend>Swinging Text</pend>
 * <pend a=45.0>Wide Swing</pend>
 * <pend f=2.0>Fast Pendulum</pend>
 * <pend a=30 r=5>Pendulum with Arc</pend>
 * }</pre>
 *
 * <h3>Notes:</h3>
 * <ul>
 *   <li>Rotation creates the swinging motion</li>
 *   <li>Radius parameter adds circular arc movement (like a real pendulum)</li>
 *   <li>Phase offset between characters creates wave effect</li>
 *   <li>Angle is specified in degrees for convenience, converted to radians internally</li>
 * </ul>
 */
public class PendulumEffect extends BaseEffect {

    private final float speed;
    private final float maxAngle;
    private final float radius;

    /**
     * Creates a new pendulum effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public PendulumEffect(@NotNull Params params) {
        super(params);
        this.speed = params.getDouble("f").map(Number::floatValue).orElse(1.0f);
        this.maxAngle = params.getDouble("a").map(Number::floatValue).orElse(30.0f);
        this.radius = params.getDouble("r").map(Number::floatValue).orElse(0.0f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Calculate time with speed and character phase offset
        // 0.002 controls swing period, index * 0.1 creates wave between characters
        double phase = (Util.getMillis() * 0.002 * speed) - (settings.index * 0.1);

        // Convert max angle from degrees to radians
        float angleRad = (float) Math.toRadians(maxAngle);

        // Apply pendulum rotation using sine wave
        // Oscillates between -maxAngle and +maxAngle
        settings.rot = (float) (Math.sin(phase) * angleRad);

        // If radius is set, add circular arc motion
        if (radius != 0) {
            // Horizontal component of circular motion
            settings.x += (float) (Math.cos(phase) * radius);
            // Vertical component of circular motion
            settings.y += (float) (Math.sin(phase) * radius);
        }
    }

    @NotNull
    @Override
    public String getName() {
        return "pend";
    }
}
