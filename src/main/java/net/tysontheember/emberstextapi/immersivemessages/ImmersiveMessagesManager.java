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
    private static ImmersiveMessage current;
    private static float nextDelay;
    private static float currentDelay;

    /** Called from a GUI render event on the client. */
    public static void render(GuiGraphics graphics, float partialTick) {
        if (current == null) {
            if (nextDelay > 0) {
                nextDelay -= partialTick;
                return;
            }
            current = QUEUE.poll();
            if (current != null) currentDelay = current.getDelay();
        }
        if (current == null) return;
        if (currentDelay > 0) {
            currentDelay -= partialTick;
            return;
        }
        current.tick(partialTick);
        if (current.isFinished()) {
            current = null;
            nextDelay = 20f; // 1 second between messages
            return;
        }
        current.render(graphics);
        if (current.isFinished()) {
            current = null;
            nextDelay = 20f; // 1 second between messages
        }
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
