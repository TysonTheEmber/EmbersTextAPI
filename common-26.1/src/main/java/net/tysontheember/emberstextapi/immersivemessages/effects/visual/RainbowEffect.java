package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.util.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

public class RainbowEffect extends BaseEffect {

    private final float speed;
    private final float phase;

    public RainbowEffect(@NotNull Params params) {
        super(params);
        this.speed = params.getDouble("speed").map(Number::floatValue).orElse(1.0f);
        this.phase = params.getDouble("phase").map(Number::floatValue).orElse(1.0f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        float hue = ((Util.getMillis() * 0.02f * speed + settings.index * phase) % 30) / 30f;

        int color = Mth.hsvToRgb(hue, 0.8f, 0.8f);

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
