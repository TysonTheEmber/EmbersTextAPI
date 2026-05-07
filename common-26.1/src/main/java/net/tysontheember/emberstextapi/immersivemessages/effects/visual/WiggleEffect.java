package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.util.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;

public class WiggleEffect extends BaseEffect {

    private final float amp;
    private final float speed;
    private final float phase;

    public WiggleEffect(@NotNull Params params) {
        super(params);
        this.amp = ValidationHelper.clamp("wiggle", "a",
                params.getDouble("a").map(Number::floatValue).orElse(1.0f), 0f, 50f);
        this.speed = ValidationHelper.clamp("wiggle", "f",
                params.getDouble("f").map(Number::floatValue).orElse(1.0f), 0.01f, 100f);
        this.phase = ValidationHelper.clamp("wiggle", "w",
                params.getDouble("w").map(Number::floatValue).orElse(1.0f), 0f, 10f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {

        float angle = (settings.codepoint % 30) * (Mth.TWO_PI / 30f);
        float dirX = Mth.cos(angle);
        float dirY = Mth.sin(angle);

        float delta = Mth.sin(Util.getMillis() * 0.01f * speed + settings.index * 2f * phase) * 1.5f * amp;

        settings.x += dirX * delta;
        settings.y += dirY * delta;
    }

    @NotNull
    @Override
    public String getName() {
        return "wiggle";
    }
}
