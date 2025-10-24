package net.tysontheember.emberstextapi.client.text;

/**
 * Provides clock and option state for effect evaluation. Stub implementation for Phase D1.
 */
public final class EffectContext {
    private static final ThreadLocal<EffectContext> LOCAL = ThreadLocal.withInitial(EffectContext::new);
    private static volatile boolean animationsEnabled = true;

    private long tickCount;
    private float partialTicks;

    private EffectContext() {
    }

    public static EffectContext obtain() {
        EffectContext context = LOCAL.get();
        context.tickCount = 0L;
        context.partialTicks = 0.0F;
        return context;
    }

    public static boolean areAnimationsEnabled() {
        return animationsEnabled;
    }

    public static void setAnimationsEnabled(boolean enabled) {
        animationsEnabled = enabled;
    }

    public long getTickCount() {
        return tickCount;
    }

    public void setTickCount(long tickCount) {
        this.tickCount = tickCount;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }
}
