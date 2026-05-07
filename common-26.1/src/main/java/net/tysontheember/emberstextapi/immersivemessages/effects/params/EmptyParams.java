package net.tysontheember.emberstextapi.immersivemessages.effects.params;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EmptyParams implements Params {

    public static final EmptyParams INSTANCE = new EmptyParams();

    private EmptyParams() {
    }

    @NotNull
    @Override
    public Optional<Double> getDouble(@NotNull String key) {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<String> getString(@NotNull String key) {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<Boolean> getBoolean(@NotNull String key) {
        return Optional.empty();
    }

    @Override
    public boolean has(@NotNull String key) {
        return false;
    }

    @NotNull
    @Override
    public String serialize() {
        return "";
    }

    @Override
    public String toString() {
        return "EmptyParams[]";
    }
}
