package net.tysontheember.emberstextapi.immersivemessages.effects.message.attr;

import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

public final class ScaleAttribute implements MessageAttribute {

    private final float value;

    public ScaleAttribute(@NotNull Params params) {
        this.value = params.getDouble("value").map(Number::floatValue).orElse(1.0f);
    }

    @Override
    public void apply(@NotNull ImmersiveMessage message) {
        message.scale(value);
    }

    @Override
    @NotNull
    public String getName() {
        return "scale";
    }

    @Override
    @NotNull
    public String serialize() {
        return "scale value=" + value;
    }
}
