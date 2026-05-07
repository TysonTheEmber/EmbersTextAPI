package net.tysontheember.emberstextapi.immersivemessages.effects;

import org.jetbrains.annotations.NotNull;

public class NoOpEffect implements Effect {

    private final String name;

    public NoOpEffect(String name) {
        this.name = name;
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {

    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public String serialize() {
        return name;
    }
}
