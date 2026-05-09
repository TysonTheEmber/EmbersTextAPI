package net.tysontheember.emberstextapi.compat.patchouli;

public final class PatchouliBypass {

    private static final ThreadLocal<int[]> DEPTH = ThreadLocal.withInitial(() -> new int[1]);

    private PatchouliBypass() {
    }

    public static void enter() {
        DEPTH.get()[0]++;
    }

    public static void exit() {
        int[] cell = DEPTH.get();
        if (cell[0] > 0) {
            cell[0]--;
        }
    }

    public static boolean active() {
        return DEPTH.get()[0] > 0;
    }
}
