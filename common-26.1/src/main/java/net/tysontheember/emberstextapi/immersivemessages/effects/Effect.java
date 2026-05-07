package net.tysontheember.emberstextapi.immersivemessages.effects;

import net.tysontheember.emberstextapi.immersivemessages.effects.params.Params;
import org.jetbrains.annotations.NotNull;

public interface Effect {

    void apply(@NotNull EffectSettings settings);

    @NotNull
    String getName();

    @NotNull
    default String serialize() {
        return getName();
    }

    @NotNull
    static Effect create(@NotNull String name, @NotNull Params params) {
        return EffectRegistry.create(name, params);
    }
}
