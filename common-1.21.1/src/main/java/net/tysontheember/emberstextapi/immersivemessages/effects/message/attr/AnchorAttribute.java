package net.tysontheember.emberstextapi.immersivemessages.effects.message.attr;

import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.immersivemessages.api.TextAnchor;
import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

public final class AnchorAttribute implements MessageAttribute {

    private final TextAnchor value;

    public AnchorAttribute(@NotNull Params params) {
        this.value = params.getString("value")
                .map(s -> {
                    try { return TextAnchor.valueOf(s.toUpperCase()); }
                    catch (IllegalArgumentException ex) { return TextAnchor.TOP_CENTER; }
                })
                .orElse(TextAnchor.TOP_CENTER);
    }

    @Override public void apply(@NotNull ImmersiveMessage message) { message.anchor(value); }
    @Override @NotNull public String getName() { return "anchor"; }
    @Override @NotNull public String serialize() { return "anchor value=" + value.name(); }
}
