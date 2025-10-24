package net.tysontheember.emberstextapi.client.text;

import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * Provides clock and option state for effect evaluation. Stub implementation for Phase D1.
 */
public final class EffectContext {
    private static final ThreadLocal<EffectContext> LOCAL = ThreadLocal.withInitial(EffectContext::new);
    private static volatile boolean animationsEnabled = true;
    private static volatile LongSupplier clock = System::nanoTime;

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

    public static void setClock(LongSupplier supplier) {
        clock = Objects.requireNonNullElse(supplier, System::nanoTime);
    }

    public static long nowNanos() {
        return clock.getAsLong();
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
