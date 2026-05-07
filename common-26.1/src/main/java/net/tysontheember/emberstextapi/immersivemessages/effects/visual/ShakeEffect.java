package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.util.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;

public class ShakeEffect extends BaseEffect {

    private final float amp;
    private final float speed;

    public ShakeEffect(@NotNull Params params) {
        super(params);
        this.amp = ValidationHelper.clamp("shake", "a",
                params.getDouble("a").map(Number::floatValue).orElse(1.0f), 0f, 50f);
        this.speed = ValidationHelper.clamp("shake", "f",
                params.getDouble("f").map(Number::floatValue).orElse(1.0f), 0.01f, 100f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {

        int seed = (int) (Util.getMillis() * 0.01f * speed + settings.codepoint + settings.index);

        float angle = (seed % 30) * (Mth.TWO_PI / 30f);
        float dirX = Mth.cos(angle);
        float dirY = Mth.sin(angle);

        settings.x += dirX * 0.6f * amp;
        settings.y += dirY * 0.6f * amp;
    }

    @NotNull
    @Override
    public String getName() {
        return "shake";
    }
}
