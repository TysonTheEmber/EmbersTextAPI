package net.tysontheember.emberstextapi.util;

public final class ImmersiveRenderBypass {

    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private ImmersiveRenderBypass() {}

    public static boolean isActive() {
        return ACTIVE.get();
    }

    public static void enter() {
        ACTIVE.set(Boolean.TRUE);
    }

    public static void exit() {
        ACTIVE.set(Boolean.FALSE);
    }
}
