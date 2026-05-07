package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.util.Util;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;

public class TurbulenceEffect extends BaseEffect {

    private final float amp;
    private final float speed;

    public TurbulenceEffect(@NotNull Params params) {
        super(params);
        this.amp = ValidationHelper.clamp("turb", "a",
                params.getDouble("a").map(Number::floatValue).orElse(1.0f), 0f, 50f);
        this.speed = ValidationHelper.clamp("turb", "f",
                params.getDouble("f").map(Number::floatValue).orElse(1.0f), 0.01f, 100f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {

        float amplitude = this.amp * 1.5f;

        float t = Util.getMillis() * 0.002f * speed;

        float nx = Mth.sin(t * 1.7f + settings.index * 0.31f + settings.codepoint * 0.07f);

        float ny = Mth.sin(t * 2.3f + settings.index * 0.27f + settings.codepoint * 0.11f);

        settings.x += nx * amplitude;
        settings.y += ny * amplitude;
    }

    @NotNull
    @Override
    public String getName() {
        return "turb";
    }
}
