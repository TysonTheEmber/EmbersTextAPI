package net.tysontheember.emberstextapi.immersivemessages.effects.visual;

import net.tysontheember.emberstextapi.immersivemessages.effects.BaseEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.util.ColorParser;
import org.jetbrains.annotations.NotNull;

public class ColorEffect extends BaseEffect {

    private static final float[] DEFAULT_COLOR = {1.0f, 1.0f, 1.0f, 1.0f};

    private final float[] rgba;

    public ColorEffect(@NotNull Params params) {
        super(params);
        String raw = params.getString("col").orElse(params.getString("value").orElse(null));
        this.rgba = raw == null
                ? DEFAULT_COLOR
                : ColorParser.parseToRgbaFloats(raw).orElse(DEFAULT_COLOR);
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        settings.r = rgba[0];
        settings.g = rgba[1];
        settings.b = rgba[2];
        settings.a *= rgba[3];
    }

    @NotNull
    @Override
    public String getName() {
        return "color";
    }
}
