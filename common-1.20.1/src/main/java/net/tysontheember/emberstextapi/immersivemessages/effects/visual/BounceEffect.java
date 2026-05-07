package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;

public class BounceEffect extends BaseEffect {

    private final float amp;
    private final float speed;
    private final float phase;

    public BounceEffect(@NotNull Params params) {
        super(params);
        this.amp = ValidationHelper.clamp("bounce", "a",
                params.getDouble("a").map(Number::floatValue).orElse(1.0f), 0f, 50f);
        this.speed = ValidationHelper.clamp("bounce", "f",
                params.getDouble("f").map(Number::floatValue).orElse(1.0f), 0.01f, 100f);
        this.phase = ValidationHelper.clamp("bounce", "w",
                params.getDouble("w").map(Number::floatValue).orElse(1.0f), 0f, 10f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {

        float t = (Util.getMillis() * 0.001f * speed - settings.index * phase * 0.2f) % 1;

        float offset = 0f;

        if (t < 0.2f) {

            offset = Mth.sin(t / 0.2f * Mth.HALF_PI);

        } else if (t < 0.8f) {

            t = (t - 0.2f) / 0.6f;

            if (t < 1 / 2.75f) {
                offset = 7.5625f * t * t;
            } else if (t < 2 / 2.75f) {
                t -= 1.5f / 2.75f;
                offset = 7.5625f * t * t + 0.75f;
            } else if (t < 2.5f / 2.75f) {
                t -= 2.25f / 2.75f;
                offset = 7.5625f * t * t + 0.9375f;
            } else {
                t -= 2.625f / 2.75f;
                offset = 7.5625f * t * t + 0.984375f;
            }

            offset = 1 - offset;
        }

        settings.y -= offset * amp * 4f;
    }

    @NotNull
    @Override
    public String getName() {
        return "bounce";
    }
}
