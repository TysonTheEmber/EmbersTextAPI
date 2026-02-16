package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.util.ColorMath;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

/**
 * Gradient effect that creates smooth color transitions across text.
 * <p>
 * Interpolates colors across characters, creating a gradient effect. Supports
 * both RGB and HSV color spaces, with optional animation and cyclic modes.
 * The gradient can be static or animated over time.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code from} (hex color, default: "5BCEFA") - Starting color</li>
 *   <li>{@code to} (hex color, default: "F5A9B8") - Ending color</li>
 *   <li>{@code hue} (boolean, default: false) - Use HSV color space for smoother hue transitions</li>
 *   <li>{@code f} (frequency, default: 0.0) - Animation speed (0 = static gradient)</li>
 *   <li>{@code sp} (span, default: 20.0) - Gradient span in characters</li>
 *   <li>{@code uni} (boolean, default: false) - Unidirectional mode (false = cyclic/back-and-forth)</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <grad>Rainbow Gradient</grad>
 * <grad from=FF0000 to=0000FF>Red to Blue</grad>
 * <grad hue=true>Smooth Hue Gradient</grad>
 * <grad f=1.0>Animated Gradient</grad>
 * <grad sp=10 uni=true>Short Unidirectional</grad>
 * }</pre>
 *
 * <h3>Technical Details:</h3>
 * <ul>
 *   <li><b>RGB Mode:</b> Linear interpolation in RGB space</li>
 *   <li><b>HSV Mode:</b> Interpolation in HSV space for smoother color transitions</li>
 *   <li><b>Cyclic Mode:</b> Gradient goes from→to→from (default)</li>
 *   <li><b>Unidirectional Mode:</b> Gradient goes from→to only</li>
 *   <li><b>Animation:</b> Gradient shifts over time when f > 0</li>
 * </ul>
 *
 * <h3>Notes:</h3>
 * <ul>
 *   <li>Shadow layer is skipped (shadows keep original color)</li>
 *   <li>Default colors are trans pride flag colors (blue to pink)</li>
 *   <li>HSV mode recommended for rainbow-like gradients</li>
 *   <li>Span controls how many characters the gradient covers before repeating</li>
 * </ul>
 */
public class GradientEffect extends BaseEffect {

    private static final float[] DEFAULT_FROM = parseColor("5BCEFA").orElse(new float[]{0.36f, 0.81f, 0.98f});
    private static final float[] DEFAULT_TO = parseColor("F5A9B8").orElse(new float[]{0.96f, 0.66f, 0.72f});

    private final float[] fromRGB;
    private final float[] toRGB;
    private final boolean useHSV;
    private final float speed;
    private final float span;
    private final boolean cyclic;

    /**
     * Creates a new gradient effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public GradientEffect(@NotNull Params params) {
        super(params);
        this.fromRGB = parseColor(params, "from", DEFAULT_FROM);
        this.toRGB = parseColor(params, "to", DEFAULT_TO);
        this.useHSV = params.getBoolean("hue").orElse(false);
        this.speed = params.getDouble("f").map(Number::floatValue).orElse(0.0f);
        this.span = params.getDouble("sp").map(Number::floatValue).orElse(20.0f);
        this.cyclic = !params.getBoolean("uni").orElse(false);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Skip shadow layer - shadows keep original color
        if (settings.isShadow) {
            return;
        }

        // Calculate position in gradient based on character index
        float tIndex = span > 0 ? (settings.index % span) / span : 0;

        // Calculate time offset for animation
        float tTime = speed > 0 ? (float) ((Util.getMillis() * 0.001) * speed % 1.0) : 0;

        // Combine position and time
        float t = (tIndex + tTime) % 1;

        // Apply cyclic mode (gradient goes from→to→from)
        if (cyclic) {
            t = (t * 2) % 2;
            if (t > 1) {
                t = 2 - t; // Reverse direction
            }
        }

        // Interpolate colors
        float[] rgb;
        if (useHSV) {
            // HSV interpolation for smoother hue transitions
            float[] hsv1 = ColorMath.rgbToHsv(fromRGB);
            float[] hsv2 = ColorMath.rgbToHsv(toRGB);
            float h = ColorMath.lerpHue(hsv1[0], hsv2[0], t);
            float s = ColorMath.lerp(hsv1[1], hsv2[1], t);
            float v = ColorMath.lerp(hsv1[2], hsv2[2], t);
            rgb = ColorMath.hsvToRgb(h, s, v);
        } else {
            // Linear RGB interpolation
            rgb = new float[]{
                    ColorMath.lerp(fromRGB[0], toRGB[0], t),
                    ColorMath.lerp(fromRGB[1], toRGB[1], t),
                    ColorMath.lerp(fromRGB[2], toRGB[2], t)
            };
        }

        // Apply gradient color
        settings.r = rgb[0];
        settings.g = rgb[1];
        settings.b = rgb[2];
    }

    @NotNull
    @Override
    public String getName() {
        return "grad";
    }

    // Note: Color conversion methods moved to ColorMath utility class
}
