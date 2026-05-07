package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.util.Util;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.util.ColorPalette;
import net.tysontheember.emberstextapi.util.Palettes;
import org.jetbrains.annotations.NotNull;

public class GradientEffect extends BaseEffect {

    private static final String DEFAULT_COLORS = "5BCEFA,F5A9B8";

    private enum Mode { CHAR, UNIFORM }

    private final ColorPalette palette;
    private final float cosA;
    private final Mode mode;
    private final float speed;
    private final float span;

    public GradientEffect(@NotNull Params params) {
        super(params);

        boolean hsv = params.getBoolean("hue").orElse(false);
        boolean cyclic = params.getBoolean("cyclic").orElse(true);
        ColorPalette.SampleMode sampleMode = cyclic
                ? ColorPalette.SampleMode.PINGPONG
                : ColorPalette.SampleMode.CLAMP;

        String spec = params.getString("colors").orElse(DEFAULT_COLORS);
        this.palette = Palettes.parse(spec, hsv, sampleMode);

        float angleDeg = params.getDouble("angle").map(Number::floatValue).orElse(0.0f);
        float angleRadians = (float) Math.toRadians(angleDeg);
        this.cosA = (float) Math.cos(angleRadians);

        String modeStr = params.getString("mode").orElse("char").trim().toLowerCase();
        this.mode = "uniform".equals(modeStr) ? Mode.UNIFORM : Mode.CHAR;

        this.speed = params.getDouble("speed").map(Number::floatValue).orElse(1.0f);
        this.span = params.getDouble("span").map(Number::floatValue).orElse(20.0f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        float timeOffset = speed != 0f ? (float) (Util.getMillis() * 0.001 * speed % 2.0) : 0f;
        float t;

        if (mode == Mode.UNIFORM || span <= 0f) {
            t = timeOffset;
        } else {
            float position = settings.index * cosA;
            t = position / span + timeOffset;
        }

        float[] rgba = palette.sample(t);
        settings.r = rgba[0];
        settings.g = rgba[1];
        settings.b = rgba[2];
        settings.a *= rgba[3];
    }

    @NotNull
    @Override
    public String getName() {
        return "grad";
    }
}
