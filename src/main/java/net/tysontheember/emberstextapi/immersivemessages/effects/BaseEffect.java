package net.tysontheember.emberstextapi.immersivemessages.effects;

import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.util.ColorParser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Abstract base class for effects providing common functionality.
 * <p>
 * This class provides:
 * <ul>
 *   <li>Parameter storage and access</li>
 *   <li>Color parsing utilities</li>
 *   <li>Default serialization implementation</li>
 * </ul>
 * </p>
 * <p>
 * Most effects should extend this class rather than implementing Effect directly.
 * </p>
 *
 * <h3>Example Effect Implementation:</h3>
 * <pre>{@code
 * public class MyEffect extends BaseEffect {
 *     private final float amplitude;
 *     private final float frequency;
 *
 *     public MyEffect(Params params) {
 *         super(params);
 *         this.amplitude = (float) params.getDouble("a").orElse(1.0);
 *         this.frequency = (float) params.getDouble("f").orElse(1.0);
 *     }
 *
 *     @Override
 *     public void apply(EffectSettings settings) {
 *         // Implementation
 *     }
 *
 *     @Override
 *     public String getName() {
 *         return "myeffect";
 *     }
 * }
 * }</pre>
 */
public abstract class BaseEffect implements Effect {

    /**
     * Parameters for this effect instance.
     */
    protected final Params params;

    /**
     * Creates a new effect with the given parameters.
     *
     * @param params Effect parameters from markup parsing
     */
    public BaseEffect(@NotNull Params params) {
        this.params = params;
    }

    /**
     * Parse a color from parameters.
     * <p>
     * Delegates to centralized {@link ColorParser} utility.
     * Color format can be:
     * <ul>
     *   <li>Hex string: "FF0000" or "#FF0000" for red</li>
     *   <li>Short form: "F00" (expanded to FF0000)</li>
     *   <li>Hex value: 0xFF0000</li>
     * </ul>
     * </p>
     *
     * @param params Parameters to read from
     * @param key Parameter key
     * @param def Default color if parsing fails
     * @return RGB color as float array [r, g, b] in range 0.0-1.0
     */
    @NotNull
    protected static float[] parseColor(@NotNull Params params, @NotNull String key, @NotNull float[] def) {
        return params.getString(key).flatMap(ColorParser::parseToRgbFloats).orElse(def);
    }

    /**
     * Parse a hex color string to RGB float array.
     * <p>
     * Delegates to centralized {@link ColorParser} utility.
     * </p>
     *
     * @param s Color string to parse
     * @return Optional containing RGB array [r, g, b] in range 0.0-1.0, or empty if parsing fails
     */
    @NotNull
    protected static Optional<float[]> parseColor(@NotNull String s) {
        return ColorParser.parseToRgbFloats(s);
    }

    /**
     * Parse an integer color value to RGB float array.
     * <p>
     * Delegates to centralized {@link ColorParser} utility.
     * </p>
     *
     * @param color Packed RGB integer (0xRRGGBB format)
     * @return RGB array [r, g, b] in range 0.0-1.0
     */
    @NotNull
    protected static float[] intToRGB(int color) {
        return ColorParser.intToRgbFloats(color);
    }

    /**
     * Pack RGB floats to integer color.
     * <p>
     * Delegates to centralized {@link ColorParser} utility.
     * </p>
     *
     * @param r Red (0.0-1.0)
     * @param g Green (0.0-1.0)
     * @param b Blue (0.0-1.0)
     * @return Packed RGB integer (0xRRGGBB format)
     */
    protected static int rgbToInt(float r, float g, float b) {
        return ColorParser.rgbFloatsToInt(r, g, b);
    }

    @NotNull
    @Override
    public String serialize() {
        String paramStr = params.serialize();
        if (paramStr.isEmpty()) {
            return getName();
        }
        return getName() + " " + paramStr;
    }

    /**
     * Get the parameters for this effect.
     *
     * @return Parameters object
     */
    @NotNull
    public Params getParams() {
        return params;
    }
}
