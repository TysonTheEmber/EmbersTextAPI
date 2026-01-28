package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
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
            float[] hsv1 = rgbToHsv(fromRGB);
            float[] hsv2 = rgbToHsv(toRGB);
            float h = lerpHue(hsv1[0], hsv2[0], t);
            float s = lerp(hsv1[1], hsv2[1], t);
            float v = lerp(hsv1[2], hsv2[2], t);
            rgb = hsvToRgb(h, s, v);
        } else {
            // Linear RGB interpolation
            rgb = new float[]{
                    lerp(fromRGB[0], toRGB[0], t),
                    lerp(fromRGB[1], toRGB[1], t),
                    lerp(fromRGB[2], toRGB[2], t)
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

    /**
     * Convert RGB to HSV color space.
     *
     * @param rgb RGB color as float array [r, g, b]
     * @return HSV color as float array [h, s, v]
     */
    private static float[] rgbToHsv(float[] rgb) {
        float r = rgb[0], g = rgb[1], b = rgb[2];
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float h, s;
        float d = max - min;
        s = max == 0 ? 0 : d / max;

        if (d == 0) {
            h = 0; // Achromatic (gray)
        } else if (max == r) {
            h = (g - b) / d + (g < b ? 6 : 0);
        } else if (max == g) {
            h = (b - r) / d + 2;
        } else {
            h = (r - g) / d + 4;
        }
        h /= 6f;
        return new float[]{h, s, max};
    }

    /**
     * Convert HSV to RGB color space.
     *
     * @param h Hue (0.0-1.0)
     * @param s Saturation (0.0-1.0)
     * @param v Value/Brightness (0.0-1.0)
     * @return RGB color as float array [r, g, b]
     */
    private static float[] hsvToRgb(float h, float s, float v) {
        int i = (int) (h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        return switch (i % 6) {
            case 0 -> new float[]{v, t, p};
            case 1 -> new float[]{q, v, p};
            case 2 -> new float[]{p, v, t};
            case 3 -> new float[]{p, q, v};
            case 4 -> new float[]{t, p, v};
            default -> new float[]{v, p, q};
        };
    }

    /**
     * Interpolate hue values with wraparound.
     * Takes shortest path around hue circle.
     *
     * @param a Start hue (0.0-1.0)
     * @param b End hue (0.0-1.0)
     * @param t Interpolation factor (0.0-1.0)
     * @return Interpolated hue
     */
    private static float lerpHue(float a, float b, float t) {
        float diff = (b - a + 1f) % 1f;
        if (diff > 0.5f) {
            diff -= 1f; // Take shorter path
        }
        return (a + diff * t + 1f) % 1f;
    }

    /**
     * Linear interpolation between two values.
     *
     * @param a Start value
     * @param b End value
     * @param t Interpolation factor (0.0-1.0)
     * @return Interpolated value
     */
    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
