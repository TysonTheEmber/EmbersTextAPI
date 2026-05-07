package net.tysontheember.emberstextapi.immersivemessages.effects.message.attr;

import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

public final class OffsetAttribute implements MessageAttribute {

    private final float x;
    private final float y;

    public OffsetAttribute(@NotNull Params params) {
        this.x = params.getDouble("x").map(Number::floatValue).orElse(0.0f);
        this.y = params.getDouble("y").map(Number::floatValue).orElse(0.0f);
    }

    @Override
    public void apply(@NotNull ImmersiveMessage message) {
        message.offset(x, y);
    }

    @Override @NotNull public String getName() { return "offset"; }
    @Override @NotNull public String serialize() { return "offset x=" + x + " y=" + y; }
}
