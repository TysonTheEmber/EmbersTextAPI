package net.tysontheember.emberstextapi.immersivemessages.effects.params;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Singleton implementation of Params for effects with no parameters.
 * <p>
 * This class is used when an effect is specified without any parameters,
 * e.g., {@code <rainbow>} instead of {@code <rainbow f=2.0>}.
 * </p>
 * <p>
 * Using a singleton avoids allocating new objects for parameterless effects.
 * </p>
 */
public class EmptyParams implements Params {

    /**
     * Singleton instance.
     */
    public static final EmptyParams INSTANCE = new EmptyParams();

    /**
     * Private constructor to enforce singleton pattern.
     */
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
