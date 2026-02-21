package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

/**
 * Solid color effect that applies a single color to text.
 * <p>
 * Like {@link GradientEffect} but with a single color instead of a gradient.
 * Useful for applying a color via the effect system so it can be stacked with
 * other effects.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code col} (hex color, default: "FFFFFF") - The color to apply</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <color col=FF0000>Red text</color>
 * <color col=#00FF00>Green text</color>
 * <color col=5BCEFA>Blue text</color>
 * }</pre>
 */
public class ColorEffect extends BaseEffect {

    private static final float[] DEFAULT_COLOR = new float[]{1.0f, 1.0f, 1.0f}; // white

    private final float[] rgb;

    /**
     * Creates a new color effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public ColorEffect(@NotNull Params params) {
        super(params);
        this.rgb = parseColor(params, "col", DEFAULT_COLOR);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Skip shadow layer - shadows keep original color
        if (settings.isShadow) {
            return;
        }

        settings.r = rgb[0];
        settings.g = rgb[1];
        settings.b = rgb[2];
    }

    @NotNull
    @Override
    public String getName() {
        return "color";
    }
}
