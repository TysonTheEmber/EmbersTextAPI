package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import net.tysontheember.emberstextapi.util.ColorPalette;
import net.tysontheember.emberstextapi.util.Palettes;
import org.jetbrains.annotations.NotNull;

public class PulseEffect extends BaseEffect {

    private final ColorPalette palette;
    private final boolean hasColors;
    private final float base;
    private final float amp;
    private final float speed;
    private final float phase;

    public PulseEffect(@NotNull Params params) {
        super(params);

        this.base = ValidationHelper.clamp("pulse", "base",
                params.getDouble("base").map(Number::floatValue).orElse(0.75f), 0f, 1f);
        this.amp = ValidationHelper.clamp("pulse", "amp",
                params.getDouble("amp").map(Number::floatValue).orElse(1.0f), 0f, 5f);
        this.speed = ValidationHelper.clamp("pulse", "speed",
                params.getDouble("speed").map(Number::floatValue).orElse(1.0f), 0.01f, 100f);
        this.phase = ValidationHelper.clamp("pulse", "phase",
                params.getDouble("phase").map(Number::floatValue).orElse(0.0f), 0f, 10f);

        String spec = params.getString("colors").orElse(null);
        if (spec != null && !spec.trim().isEmpty()) {
            boolean hsv = params.getBoolean("hue").orElse(false);
            this.palette = Palettes.parse(spec, hsv, ColorPalette.SampleMode.PINGPONG);
            this.hasColors = true;
        } else {
            this.palette = null;
            this.hasColors = false;
        }
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        float t = Util.getMillis() * 0.002f * speed + settings.index * phase;
        float s = 0.5f + 0.5f * Mth.sin(t);
        float k = base + amp * 0.25f * s;

        if (hasColors) {
            float[] rgba = palette.sample(s);
            settings.r = rgba[0] * k;
            settings.g = rgba[1] * k;
            settings.b = rgba[2] * k;
            settings.a *= rgba[3];
        } else {
            settings.r *= k;
            settings.g *= k;
            settings.b *= k;
        }
    }

    @NotNull
    @Override
    public String getName() {
        return "pulse";
    }
}
