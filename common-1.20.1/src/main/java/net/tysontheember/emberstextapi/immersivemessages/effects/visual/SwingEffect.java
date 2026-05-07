package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;

public class SwingEffect extends BaseEffect {

    private final float amp;
    private final float speed;
    private final float phase;

    public SwingEffect(@NotNull Params params) {
        super(params);
        this.amp = ValidationHelper.clamp("swing", "a",
                params.getDouble("a").map(Number::floatValue).orElse(1.0f), 0f, 10f);
        this.speed = ValidationHelper.clamp("swing", "f",
                params.getDouble("f").map(Number::floatValue).orElse(1.0f), 0.01f, 100f);
        this.phase = ValidationHelper.clamp("swing", "w",
                params.getDouble("w").map(Number::floatValue).orElse(0.0f), 0f, 10f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {

        float t = Util.getMillis() * 0.003f * speed + settings.index * phase;

        settings.rot += Mth.sin(t) * amp * 0.5f;
    }

    @NotNull
    @Override
    public String getName() {
        return "swing";
    }
}
