package net.tysontheember.emberstextapi.immersivemessages.effects.message;

import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

public interface MessageEffect {

    void apply(@NotNull MessageEffectContext ctx);

    @NotNull
    String getName();

    @NotNull
    default String serialize() {
        return getName();
    }

    @NotNull
    static MessageEffect create(@NotNull String name, @NotNull Params params) {
        return MessageEffectRegistry.create(name, params);
    }
}
