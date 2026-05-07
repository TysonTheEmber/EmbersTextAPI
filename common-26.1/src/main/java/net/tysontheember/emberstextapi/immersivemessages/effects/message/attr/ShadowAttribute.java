package net.tysontheember.emberstextapi.immersivemessages.effects.message.attr;

import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

public final class ShadowAttribute implements MessageAttribute {

    private final boolean value;

    public ShadowAttribute(@NotNull Params params) {
        this.value = params.getBoolean("value").orElse(true);
    }

    @Override public void apply(@NotNull ImmersiveMessage message) { message.shadow(value); }
    @Override @NotNull public String getName() { return "shadow"; }
    @Override @NotNull public String serialize() { return "shadow value=" + value; }
}
