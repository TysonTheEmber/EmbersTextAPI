package net.tysontheember.emberstextapi.immersivemessages.effects.message;

import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

public abstract class BaseMessageEffect implements MessageEffect {

    protected final Params params;

    public BaseMessageEffect(@NotNull Params params) {
        this.params = params;
    }

    @NotNull
    @Override
    public String serialize() {
        String paramStr = params.serialize();
        return paramStr.isEmpty() ? getName() : getName() + " " + paramStr;
    }

    @NotNull
    public Params getParams() {
        return params;
    }
}
