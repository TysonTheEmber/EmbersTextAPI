package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.minecraft.util.Util;
import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.ValidationHelper;
import org.jetbrains.annotations.NotNull;

public class PendulumEffect extends BaseEffect {

    private final float speed;
    private final float maxAngle;
    private final float radius;

    public PendulumEffect(@NotNull Params params) {
        super(params);
        this.speed = ValidationHelper.clamp("pend", "f",
                params.getDouble("f").map(Number::floatValue).orElse(1.0f), 0.01f, 100f);
        this.maxAngle = ValidationHelper.clamp("pend", "a",
                params.getDouble("a").map(Number::floatValue).orElse(30.0f), 0f, 90f);
        this.radius = ValidationHelper.clamp("pend", "r",
                params.getDouble("r").map(Number::floatValue).orElse(0.0f), 0f, 50f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {

        double phase = (Util.getMillis() * 0.002 * speed) - (settings.index * 0.1);

        float angleRad = (float) Math.toRadians(maxAngle);

        settings.rot = (float) (Math.sin(phase) * angleRad);

        if (radius != 0) {

            settings.x += (float) (Math.cos(phase) * radius);

            settings.y += (float) (Math.sin(phase) * radius);
        }
    }

    @NotNull
    @Override
    public String getName() {
        return "pend";
    }
}
