package net.tysontheember.emberstextapi.immersivemessages;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Handles queuing and rendering of {@link ImmersiveMessage} instances.
 * This mirrors the behaviour of the original Immersive Messages mod but in a
 * much smaller form suitable for the example project.
 */
public class ImmersiveMessagesManager {
    private static final Queue<ImmersiveMessage> QUEUE = new LinkedList<>();
    private static final float MESSAGE_GAP_TICKS = 20f; // 1 second between messages

    private static ImmersiveMessage current;
    private static float nextDelay;
    private static float currentDelay;

    /** Advances message timing once per client tick. */
    public static void tick() {
        if (current == null) {
            if (nextDelay > 0f) {
                nextDelay = Math.max(0f, nextDelay - 1f);
                if (nextDelay > 0f) return;
            }
            current = QUEUE.poll();
            if (current != null) {
                currentDelay = current.getDelay();
            }
        }

        if (current == null) return;

        if (currentDelay > 0f) {
            currentDelay = Math.max(0f, currentDelay - 1f);
            if (currentDelay > 0f) return;
        }

        current.tick(1f);

        if (current.isFinished()) {
            finishCurrent();
        }
    }

    /** Called from a GUI render event on the client. */
    public static void render(GuiGraphics graphics) {
        if (current != null && currentDelay <= 0f) {
            current.render(graphics);
        }
    }

    private static void finishCurrent() {
        current = null;
        nextDelay = MESSAGE_GAP_TICKS;
    }

    public static void showToPlayer(LocalPlayer player, ImmersiveMessage message) {
        QUEUE.add(message);
    }

    public static void clear() {
        QUEUE.clear();
        current = null;
        nextDelay = 0f;
        currentDelay = 0f;
    }
}
