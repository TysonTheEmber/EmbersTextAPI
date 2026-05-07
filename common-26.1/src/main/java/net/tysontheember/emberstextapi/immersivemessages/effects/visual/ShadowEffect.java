package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

public class ShadowEffect extends BaseEffect {

    private final float dx;
    private final float dy;
    private final float r;
    private final float g;
    private final float b;
    private final float a;

    public ShadowEffect(@NotNull Params params) {
        super(params);
        this.dx = params.getDouble("x").map(Number::floatValue).orElse(0.0f);
        this.dy = params.getDouble("y").map(Number::floatValue).orElse(0.0f);

        float[] color = parseColor(params, "c", null);
        if (color != null) {
            this.r = color[0];
            this.g = color[1];
            this.b = color[2];
        } else {

            this.r = params.getDouble("r").map(Number::floatValue).orElse(0.0f);
            this.g = params.getDouble("g").map(Number::floatValue).orElse(0.0f);
            this.b = params.getDouble("b").map(Number::floatValue).orElse(0.0f);
        }

        this.a = params.getDouble("a").map(Number::floatValue).orElse(1.0f);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {

        if (!settings.isShadow) {
            return;
        }

        settings.x += dx;
        settings.y += dy;

        settings.r = r;
        settings.g = g;
        settings.b = b;

        settings.a *= a;
    }

    @NotNull
    @Override
    public String getName() {
        return "shadow";
    }
}
