package net.tysontheember.emberstextapi.immersivemessages.effects;

import org.jetbrains.annotations.NotNull;

/**
 * A no-op effect that does nothing. Used as a replacement when an effect is disabled via config.
 * Text renders as plain when this effect is applied.
 */
public class NoOpEffect implements Effect {

    private final String name;

    public NoOpEffect(String name) {
        this.name = name;
    }

    @Override
    public void apply(@NotNull EffectSettings settings) {
        // Intentionally empty — disabled effect renders as plain text
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
