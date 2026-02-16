package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

/**
 * Shadow effect that modifies shadow layer properties.
 * <p>
 * Allows customization of text shadow appearance including position, color,
 * and transparency. Only affects the shadow rendering layer, leaving the
 * main text unchanged.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code x} (offset X, default: 0.0) - Horizontal shadow offset in pixels</li>
 *   <li>{@code y} (offset Y, default: 0.0) - Vertical shadow offset in pixels</li>
 *   <li>{@code c} (hex color, optional) - Shadow color (overrides r/g/b if specified)</li>
 *   <li>{@code r} (red, default: 0.0) - Red channel (0.0-1.0), used if 'c' not specified</li>
 *   <li>{@code g} (green, default: 0.0) - Green channel (0.0-1.0), used if 'c' not specified</li>
 *   <li>{@code b} (blue, default: 0.0) - Blue channel (0.0-1.0), used if 'c' not specified</li>
 *   <li>{@code a} (alpha, default: 1.0) - Shadow transparency multiplier (0.0-1.0)</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <shadow x=2 y=2>Text with offset shadow</shadow>
 * <shadow c=FF0000>Red shadow</shadow>
 * <shadow r=1.0 g=0.0 b=0.0>Red shadow (RGB)</shadow>
 * <shadow c=0000FF a=0.5>Blue semi-transparent</shadow>
 * <shadow x=3 y=3 c=FFFF00>Yellow offset shadow</shadow>
 * }</pre>
 *
 * <h3>Notes:</h3>
 * <ul>
 *   <li>Only affects shadow layer (isShadow=true), main text is unchanged</li>
 *   <li>Color parameter 'c' takes precedence over r/g/b if both are specified</li>
 *   <li>Default shadow color is black (0, 0, 0)</li>
 *   <li>Alpha is multiplicative - combines with other transparency effects</li>
 *   <li>Can be combined with position/rotation effects for dynamic shadows</li>
 * </ul>
 */
public class ShadowEffect extends BaseEffect {

    private final float dx;
    private final float dy;
    private final float r;
    private final float g;
    private final float b;
    private final float a;

    /**
     * Creates a new shadow effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public ShadowEffect(@NotNull Params params) {
        super(params);
        this.dx = params.getDouble("x").map(Number::floatValue).orElse(0.0f);
        this.dy = params.getDouble("y").map(Number::floatValue).orElse(0.0f);

        // Try to parse color parameter first
        float[] color = parseColor(params, "c", null);
        if (color != null) {
            this.r = color[0];
            this.g = color[1];
            this.b = color[2];
        } else {
            // Fall back to individual RGB components
            this.r = params.getDouble("r").map(Number::floatValue).orElse(0.0f);
            this.g = params.getDouble("g").map(Number::floatValue).orElse(0.0f);
            this.b = params.getDouble("b").map(Number::floatValue).orElse(0.0f);
        }

        this.a = params.getDouble("a").map(Number::floatValue).orElse(1.0f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Only affect shadow layer
        if (!settings.isShadow) {
            return;
        }

        // Apply position offset
        settings.x += dx;
        settings.y += dy;

        // Apply color
        settings.r = r;
        settings.g = g;
        settings.b = b;

        // Apply alpha multiplier
        settings.a *= a;
    }

    @NotNull
    @Override
    public String getName() {
        return "shadow";
    }
}
