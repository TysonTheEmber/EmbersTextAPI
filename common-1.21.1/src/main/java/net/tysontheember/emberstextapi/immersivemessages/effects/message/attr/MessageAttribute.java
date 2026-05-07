package net.tysontheember.emberstextapi.immersivemessages.effects.message.attr;

import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

public interface MessageAttribute {

    void apply(@NotNull ImmersiveMessage message);

    @NotNull
    String getName();

    @NotNull
    default String serialize() {
        return getName();
    }

    @NotNull
    static MessageAttribute create(@NotNull String name, @NotNull Params params) {
        return MessageAttributeRegistry.create(name, params);
    }
}
