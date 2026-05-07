package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.util.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;

public class FadeEffect extends BaseEffect {

    private final float minA;
    private final float speed;
    private final float phase;

    public FadeEffect(@NotNull Params params) {
        super(params);
        this.minA = ValidationHelper.clamp("fade", "a",
                params.getDouble("a").map(Number::floatValue).orElse(0.3f), 0f, 1f);
        this.speed = ValidationHelper.clamp("fade", "f",
                params.getDouble("f").map(Number::floatValue).orElse(1.0f), 0.01f, 100f);
        this.phase = ValidationHelper.clamp("fade", "w",
                params.getDouble("w").map(Number::floatValue).orElse(0.0f), 0f, 10f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {

        float t = Util.getMillis() * 0.002f * speed + settings.index * phase;

        float k = minA + (1f - minA) * (0.5f + 0.5f * Mth.sin(t));

        settings.a *= k;
    }

    @NotNull
    @Override
    public String getName() {
        return "fade";
    }
}
