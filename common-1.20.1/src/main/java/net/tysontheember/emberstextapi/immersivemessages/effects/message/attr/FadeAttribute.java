package net.tysontheember.emberstextapi.immersivemessages.effects.message.attr;

import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

public final class FadeAttribute implements MessageAttribute {

    private final Integer in;
    private final Integer out;

    public FadeAttribute(@NotNull Params params) {
        this.in = params.getDouble("in").map(Number::intValue).orElse(null);
        this.out = params.getDouble("out").map(Number::intValue).orElse(null);
    }

    @Override
    public void apply(@NotNull ImmersiveMessage message) {
        if (in != null) message.fadeInTicks(in);
        if (out != null) message.fadeOutTicks(out);
    }

    @Override @NotNull public String getName() { return "fade"; }

    @Override
    @NotNull
    public String serialize() {
        StringBuilder sb = new StringBuilder("fade");
        if (in != null) sb.append(" in=").append(in);
        if (out != null) sb.append(" out=").append(out);
        return sb.toString();
    }
}
