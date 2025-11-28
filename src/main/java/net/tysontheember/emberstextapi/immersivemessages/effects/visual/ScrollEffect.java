package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

/**
 * Scroll effect that creates continuous horizontal scrolling motion.
 * <p>
 * Applies a time-based horizontal offset to characters, making them scroll
 * continuously from right to left. The scrolling loops with a configurable period.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code f} (frequency/speed, default: 1.0) - Scrolling speed</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <scroll>Scrolling Text</scroll>
 * <scroll f=2.0>Fast Scroll</scroll>
 * <scroll f=0.5>Slow Scroll</scroll>
 * }</pre>
 *
 * <h3>Notes:</h3>
 * <ul>
 *   <li>Scrolls from right to left (negative X offset)</li>
 *   <li>Period is 40 units - text repeats after scrolling this distance</li>
 *   <li>All characters scroll together (no wave effect)</li>
 *   <li>Useful for marquee-style text displays</li>
 * </ul>
 */
public class ScrollEffect extends BaseEffect {

    private final float speed;

    /**
     * Creates a new scroll effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public ScrollEffect(@NotNull Params params) {
        super(params);
        this.speed = params.getDouble("f").map(Number::floatValue).orElse(1.0f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Scrolling period in pixels
        float period = 40f;

        // Calculate scroll offset
        // 0.04 controls base scroll speed
        // Modulo period creates looping effect
        float offset = (Util.getMillis() * 0.04f * speed) % period;

        // Apply leftward scroll (subtract from X)
        settings.x -= offset;
    }

    @NotNull
    @Override
    public String getName() {
        return "scroll";
    }
}
