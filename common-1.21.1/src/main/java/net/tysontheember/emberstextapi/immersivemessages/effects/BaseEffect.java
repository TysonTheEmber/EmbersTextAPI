package net.tysontheember.emberstextapi.immersivemessages.effects;

import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import net.tysontheember.emberstextapi.immersivemessages.util.ColorParser;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class BaseEffect implements Effect {

    protected final Params params;

    public BaseEffect(@NotNull Params params) {
        this.params = params;
    }

    @NotNull
    protected static float[] parseColor(@NotNull Params params, @NotNull String key, @NotNull float[] def) {
        return params.getString(key).flatMap(ColorParser::parseToRgbFloats).orElse(def);
    }

    @NotNull
    protected static Optional<float[]> parseColor(@NotNull String s) {
        return ColorParser.parseToRgbFloats(s);
    }

    @NotNull
    protected static float[] intToRGB(int color) {
        return ColorParser.intToRgbFloats(color);
    }

    protected static int rgbToInt(float r, float g, float b) {
        return ColorParser.rgbFloatsToInt(r, g, b);
    }

    @NotNull
    @Override
    public String serialize() {
        String paramStr = params.serialize();
        if (paramStr.isEmpty()) {
            return getName();
        }
        return getName() + " " + paramStr;
    }

    @NotNull
    public Params getParams() {
        return params;
    }
}
