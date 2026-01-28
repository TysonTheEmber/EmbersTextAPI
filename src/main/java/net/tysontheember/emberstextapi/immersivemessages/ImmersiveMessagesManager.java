package net.tysontheember.emberstextapi.immersivemessages;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Handles queuing and rendering of {@link ImmersiveMessage} instances.
 * Supports concurrent rendering of multiple messages.
 */
public class ImmersiveMessagesManager {
    private static final Queue<ImmersiveMessage> QUEUE = new LinkedList<>();
    private static final List<ActiveRender> ACTIVE_MESSAGES = new ArrayList<>();
    private static final float MESSAGE_GAP_TICKS = 20f; // 1 second between messages

    private static float nextDelay;

    private static class ActiveRender {
        final ImmersiveMessage message;
        float delay;

        ActiveRender(ImmersiveMessage message) {
            this.message = message;
            this.delay = message.getDelay();
        }
    }

    /** Advances message timing once per client tick. */
    public static void tick() {
        // Start new messages from queue
        if (nextDelay > 0f) {
            nextDelay = Math.max(0f, nextDelay - 1f);
        }
        
        if (nextDelay <= 0f && !QUEUE.isEmpty()) {
            ImmersiveMessage newMessage = QUEUE.poll();
            if (newMessage != null) {
                ACTIVE_MESSAGES.add(new ActiveRender(newMessage));
                nextDelay = MESSAGE_GAP_TICKS;
            }
        }

        // Tick all active messages
        Iterator<ActiveRender> iterator = ACTIVE_MESSAGES.iterator();
        while (iterator.hasNext()) {
            ActiveRender active = iterator.next();
            
            if (active.delay > 0f) {
                active.delay = Math.max(0f, active.delay - 1f);
                continue;
            }

            active.message.tick(1f);

            if (active.message.isFinished()) {
                iterator.remove();
            }
        }
    }

    /** Called from a GUI render event on the client. */
    public static void render(GuiGraphics graphics) {
        for (ActiveRender active : ACTIVE_MESSAGES) {
            if (active.delay <= 0f) {
                active.message.render(graphics);
            }
        }
    }

    public static void showToPlayer(LocalPlayer player, ImmersiveMessage message) {
        QUEUE.add(message);
    }

    public static void clear() {
        QUEUE.clear();
        ACTIVE_MESSAGES.clear();
        nextDelay = 0f;
    }
}
