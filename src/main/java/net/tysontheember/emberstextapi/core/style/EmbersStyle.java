package net.tysontheember.emberstextapi.core.style;

/**
 * Duck interface applied to {@link net.minecraft.network.chat.Style} so that custom span metadata
 * can be attached to vanilla styles.
 */
public interface EmbersStyle {
    /**
     * Returns the span effect state attached to this style, or {@code null} if none is present.
     */
    SpanEffectState emberstextapi$getSpanEffectState();

    /**
     * Replaces the span effect state attached to this style. Implementations should store a defensive copy.
     */
    void emberstextapi$setSpanEffectState(SpanEffectState state);

    /**
     * Retrieves the existing span effect state or creates a fresh one when absent.
     */
    default SpanEffectState emberstextapi$getOrCreateSpanEffectState() {
        SpanEffectState state = emberstextapi$getSpanEffectState();
        if (state == null) {
            state = new SpanEffectState();
            emberstextapi$setSpanEffectState(state);
        }
        return state;
    }
}
