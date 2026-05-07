package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.util.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;

public class WaveEffect extends BaseEffect {

    private final float amplitude;
    private final float frequency;
    private final float wavelength;

    public WaveEffect(@NotNull Params params) {
        super(params);
        this.amplitude = ValidationHelper.clamp("wave", "a",
                params.getDouble("a").map(Number::floatValue).orElse(1.0f), 0f, 50f);
        this.frequency = ValidationHelper.clamp("wave", "f",
                params.getDouble("f").map(Number::floatValue).orElse(1.0f), 0.01f, 100f);
        this.wavelength = ValidationHelper.clamp("wave", "w",
                params.getDouble("w").map(Number::floatValue).orElse(1.0f), 0.001f, 100f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {

        float time = Util.getMillis() * 0.002f * frequency;

        float phase = time + settings.index * wavelength * 0.2f;

        float safeWavelength = Math.max(0.0001f, wavelength);
        float offset = Mth.sin(phase * Mth.TWO_PI / safeWavelength) * amplitude;

        settings.y += offset;
    }

    @NotNull
    @Override
    public String getName() {
        return "wave";
    }
}
