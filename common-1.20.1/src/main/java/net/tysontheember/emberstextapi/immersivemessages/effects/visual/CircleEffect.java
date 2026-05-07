package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;

public class CircleEffect extends BaseEffect {

    private final float radius;
    private final float speed;
    private final float phase;

    public CircleEffect(@NotNull Params params) {
        super(params);
        this.radius = ValidationHelper.clamp("circle", "a",
                params.getDouble("a").map(Number::floatValue).orElse(1.0f), 0f, 50f);
        this.speed = ValidationHelper.clamp("circle", "f",
                params.getDouble("f").map(Number::floatValue).orElse(1.0f), 0.01f, 100f);
        this.phase = ValidationHelper.clamp("circle", "w",
                params.getDouble("w").map(Number::floatValue).orElse(0.0f), 0f, 10f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {

        float t = Util.getMillis() * 0.002f * speed + settings.index * phase;

        settings.x += Mth.cos(t) * radius;
        settings.y += Mth.sin(t) * radius;
    }

    @NotNull
    @Override
    public String getName() {
        return "circle";
    }
}
