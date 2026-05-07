package net.tysontheember.emberstextapi.immersivemessages.effects.message.attr;

import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.api.TextAlign;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

public final class AlignAttribute implements MessageAttribute {

    private final TextAlign value;

    public AlignAttribute(@NotNull Params params) {
        this.value = params.getString("value")
                .map(s -> {
                    try { return TextAlign.valueOf(s.toUpperCase()); }
                    catch (IllegalArgumentException ex) { return TextAlign.LEFT; }
                })
                .orElse(TextAlign.LEFT);
    }

    @Override public void apply(@NotNull ImmersiveMessage message) { message.align(value); }
    @Override @NotNull public String getName() { return "align"; }
    @Override @NotNull public String serialize() { return "align value=" + value.name(); }
}
