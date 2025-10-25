package net.tysontheember.emberstextapi.client.text;

/**
 * Represents runtime data passed to effects.
 */
public final class EffectContext {
    private EffectContext() {
    }

    /**
     * Creates an empty context placeholder.
     *
     * @return new context stub
     */
    public static EffectContext empty() {
        return new EffectContext();
    }
}
