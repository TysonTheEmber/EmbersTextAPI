package net.tysontheember.emberstextapi.immersivemessages.effects.params;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface Params {

    @NotNull
    Optional<Double> getDouble(@NotNull String key);

    @NotNull
    Optional<String> getString(@NotNull String key);

    @NotNull
    Optional<Boolean> getBoolean(@NotNull String key);

    boolean has(@NotNull String key);

    @NotNull
    String serialize();
}
