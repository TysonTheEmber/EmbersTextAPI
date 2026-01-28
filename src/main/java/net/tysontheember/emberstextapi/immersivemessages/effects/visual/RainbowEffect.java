package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

/**
 * Rainbow color effect that cycles through HSV colors over time.
 * <p>
 * Creates a smooth rainbow color transition with per-character phase offsets
 * to create a wave effect. Colors cycle through the full hue spectrum while
 * maintaining constant saturation and value.
 * </p>
 *
 * <h3>Parameters:</h3>
 * <ul>
 *   <li>{@code f} (frequency, default: 1.0) - Color cycling speed</li>
 *   <li>{@code w} (wave, default: 1.0) - Phase offset between characters (creates wave effect)</li>
 * </ul>
 *
 * <h3>Example Usage:</h3>
 * <pre>{@code
 * <rainb>Rainbow Text!</rainb>
 * <rainb f=2.0>Fast Rainbow</rainb>
 * <rainb f=1.0 w=0.5>Tight Wave</rainb>
 * }</pre>
 *
 * <h3>Notes:</h3>
 * <ul>
 *   <li>Shadow layer is skipped (shadows remain original color)</li>
 *   <li>Uses HSV color space for smooth hue transitions</li>
 *   <li>Saturation and value are fixed at 0.8 for vibrant colors</li>
 * </ul>
 */
public class RainbowEffect extends BaseEffect {

    private final float speed;
    private final float phase;

    /**
     * Creates a new rainbow effect with the given parameters.
     *
     * @param params Effect parameters
     */
    public RainbowEffect(@NotNull Params params) {
        super(params);
        this.speed = params.getDouble("f").map(Number::floatValue).orElse(1.0f);
        this.phase = params.getDouble("w").map(Number::floatValue).orElse(1.0f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Skip shadow layer - shadows keep original color
        if (settings.isShadow) {
            return;
        }

        // Calculate hue based on time and character index
        // time * 0.02 * speed gives ~3 second cycle at speed=1.0
        // + index * phase creates wave effect between characters
        // % 30 / 30 normalizes to 0.0-1.0 range for hue
        float hue = ((Util.getMillis() * 0.02f * speed + settings.index * phase) % 30) / 30f;

        // Convert HSV to RGB
        // Saturation and value fixed at 0.8 for vibrant, not-too-bright colors
        int color = Mth.hsvToRgb(hue, 0.8f, 0.8f);

        // Extract RGB components and update settings
        settings.r = ((color >> 16) & 255) / 255f;
        settings.g = ((color >> 8) & 255) / 255f;
        settings.b = (color & 255) / 255f;
    }

    @NotNull
    @Override
    public String getName() {
        return "rainb";
    }
}
