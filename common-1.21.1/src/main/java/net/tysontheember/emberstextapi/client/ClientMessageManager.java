package net.tysontheember.emberstextapi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;

import java.util.*;

/**
 * Client-side manager for all active immersive messages.
 * <p>
 * Maintains a map of active messages keyed by UUID, handles per-tick lifecycle
 * (age advancement, expiry removal), and drives rendering via the GUI overlay hook.
 * Also detects GUI scale changes and clears the {@link TextLayoutCache} accordingly.
 * </p>
 * <p>
 * This class is platform-agnostic. Loader-specific event handlers should call
 * {@link #tick(Minecraft)} and {@link #render(GuiGraphics, float)}.
 * </p>
 */
public final class ClientMessageManager {
    private static final Map<UUID, ActiveMessage> ACTIVE = new LinkedHashMap<>();
    private static int lastGuiScale = -1;

    // Queue system
    private static final Map<String, Deque<QueueStep>> CHANNEL_QUEUES = new LinkedHashMap<>();
    private static final Map<String, Set<UUID>> CHANNEL_ACTIVE_IDS = new HashMap<>();

    private ClientMessageManager() {
    }

    public static void open(UUID id, ImmersiveMessage message) {
        if (id == null || message == null) {
            return;
        }
        ACTIVE.remove(id);
        ACTIVE.put(id, new ActiveMessage(id, message));
    }

    public static void update(UUID id, ImmersiveMessage message) {
        if (id == null || message == null) {
            return;
        }
        ActiveMessage active = ACTIVE.get(id);
        if (active != null) {
            active.update(message);
        } else {
            ACTIVE.put(id, new ActiveMessage(id, message));
        }
    }

    public static void close(UUID id) {
        if (id == null) {
            return;
        }
        ACTIVE.remove(id);
    }

    public static void closeAll() {
        ACTIVE.clear();
    }

    /**
     * Enqueue a list of steps onto a named channel. If the channel has no active
     * messages and no pending steps, the first step starts immediately.
     */
    public static void enqueueSteps(String channel, List<QueueStep> steps) {
        if (channel == null || steps == null || steps.isEmpty()) {
            return;
        }

        Set<UUID> activeIds = CHANNEL_ACTIVE_IDS.get(channel);
        Deque<QueueStep> queue = CHANNEL_QUEUES.get(channel);
        boolean channelIdle = (queue == null || queue.isEmpty())
                && (activeIds == null || activeIds.stream().noneMatch(ACTIVE::containsKey));

        if (channelIdle) {
            // Start step 0 immediately
            QueueStep first = steps.get(0);
            Set<UUID> newActiveIds = new LinkedHashSet<>();
            for (QueuedMessage qm : first.messages()) {
                open(qm.id(), qm.message());
                newActiveIds.add(qm.id());
            }
            CHANNEL_ACTIVE_IDS.put(channel, newActiveIds);

            // Enqueue remaining steps
            Deque<QueueStep> newQueue = new ArrayDeque<>();
            for (int i = 1; i < steps.size(); i++) {
                newQueue.add(steps.get(i));
            }
            CHANNEL_QUEUES.put(channel, newQueue);
        } else {
            // Append all steps
            if (queue == null) {
                queue = new ArrayDeque<>();
                CHANNEL_QUEUES.put(channel, queue);
            }
            queue.addAll(steps);
        }
    }

    /**
     * Clear pending (not-yet-started) steps from a channel queue.
     * The currently active step plays to completion.
     */
    public static void clearQueue(String channel) {
        if (channel == null) {
            return;
        }
        Deque<QueueStep> queue = CHANNEL_QUEUES.get(channel);
        if (queue != null) {
            queue.clear();
        }
        Set<UUID> activeIds = CHANNEL_ACTIVE_IDS.get(channel);
        boolean noActive = activeIds == null || activeIds.stream().noneMatch(ACTIVE::containsKey);
        if (noActive) {
            CHANNEL_QUEUES.remove(channel);
            CHANNEL_ACTIVE_IDS.remove(channel);
        }
    }

    /**
     * Clear all channel queues and immediately close all active messages.
     */
    public static void clearAllQueues() {
        closeAll();
        CHANNEL_QUEUES.clear();
        CHANNEL_ACTIVE_IDS.clear();
    }

    /**
     * Tick all active messages. Should be called from platform-specific client tick event.
     * @param minecraft The Minecraft client instance
     */
    public static void tick(Minecraft minecraft) {
        if (minecraft == null || minecraft.isPaused()) {
            return;
        }
        int currentScale = (int) Math.round(minecraft.getWindow().getGuiScale());
        if (lastGuiScale != currentScale) {
            TextLayoutCache.clear();
            lastGuiScale = currentScale;
        }
        Iterator<Map.Entry<UUID, ActiveMessage>> iterator = ACTIVE.entrySet().iterator();
        while (iterator.hasNext()) {
            ActiveMessage active = iterator.next().getValue();
            active.tick();
            if (active.isExpired()) {
                iterator.remove();
            }
        }

        // Advance channel queues
        for (String channel : new ArrayList<>(CHANNEL_QUEUES.keySet())) {
            Set<UUID> activeIds = CHANNEL_ACTIVE_IDS.get(channel);
            boolean stepDone = activeIds == null || activeIds.stream().noneMatch(ACTIVE::containsKey);
            if (stepDone) {
                Deque<QueueStep> queue = CHANNEL_QUEUES.get(channel);
                if (queue != null) {
                    QueueStep next = queue.poll();
                    if (next != null) {
                        Set<UUID> newActiveIds = new LinkedHashSet<>();
                        for (QueuedMessage qm : next.messages()) {
                            open(qm.id(), qm.message());
                            newActiveIds.add(qm.id());
                        }
                        CHANNEL_ACTIVE_IDS.put(channel, newActiveIds);
                    } else {
                        // Queue exhausted — auto-clear channel entry
                        CHANNEL_QUEUES.remove(channel);
                        CHANNEL_ACTIVE_IDS.remove(channel);
                    }
                }
            }
        }
    }

    /**
     * Render all active messages. Should be called from platform-specific GUI render event.
     * @param guiGraphics The GUI graphics context
     * @param partialTick The partial tick time
     */
    public static void render(GuiGraphics guiGraphics, float partialTick) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        for (ActiveMessage active : ACTIVE.values()) {
            active.render(guiGraphics, partialTick);
        }
    }
}
