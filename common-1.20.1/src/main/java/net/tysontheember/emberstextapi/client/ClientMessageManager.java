package net.tysontheember.emberstextapi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;
import net.tysontheember.emberstextapi.platform.ConfigHelper;

import java.util.*;

public final class ClientMessageManager {
    private static final Map<UUID, ActiveMessage> ACTIVE = new LinkedHashMap<>();
    private static int lastGuiScale = -1;

    private static final Map<String, Deque<QueueStep>> CHANNEL_QUEUES = new LinkedHashMap<>();
    private static final Map<String, Set<UUID>> CHANNEL_ACTIVE_IDS = new HashMap<>();

    private ClientMessageManager() {
    }

    public static synchronized void open(UUID id, ImmersiveMessage message) {
        if (id == null || message == null) {
            return;
        }

        try {
            if (!ConfigHelper.getInstance().isImmersiveMessagesEnabled()) {
                return;
            }
        } catch (Exception ignored) {
        }

        try {
            int max = ConfigHelper.getInstance().getMaxActiveMessages();
            if (max > 0 && !ACTIVE.containsKey(id) && ACTIVE.size() >= max) {
                return;
            }
        } catch (Exception ignored) {
        }

        try {
            int maxDuration = ConfigHelper.getInstance().getMaxMessageDuration();
            if (maxDuration > 0) {
                int msgDuration = message.durationTicks();
                if (msgDuration <= 0 || msgDuration > maxDuration) {
                    message.setDuration(maxDuration);
                }
            }
        } catch (Exception ignored) {
        }

        ACTIVE.remove(id);
        ACTIVE.put(id, new ActiveMessage(id, message));
    }

    public static synchronized void update(UUID id, ImmersiveMessage message) {
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

    public static synchronized void close(UUID id) {
        if (id == null) {
            return;
        }
        ACTIVE.remove(id);
    }

    public static synchronized int getActiveMessageCount() {
        return ACTIVE.size();
    }

    public static synchronized void closeAll() {
        ACTIVE.clear();
    }

    public static synchronized void enqueueSteps(String channel, List<QueueStep> steps) {
        if (channel == null || steps == null || steps.isEmpty()) {
            return;
        }

        try {
            int maxQueueSize = ConfigHelper.getInstance().getMaxQueueSize();
            if (maxQueueSize > 0) {
                Deque<QueueStep> existing = CHANNEL_QUEUES.get(channel);
                int currentSize = existing != null ? existing.size() : 0;
                int available = maxQueueSize - currentSize;
                if (available <= 0) {
                    return;
                }
                if (steps.size() > available) {
                    steps = steps.subList(0, available);
                }
            }
        } catch (Exception ignored) {
        }

        Set<UUID> activeIds = CHANNEL_ACTIVE_IDS.get(channel);
        Deque<QueueStep> queue = CHANNEL_QUEUES.get(channel);
        boolean channelIdle = (queue == null || queue.isEmpty())
                && (activeIds == null || activeIds.stream().noneMatch(ACTIVE::containsKey));

        if (channelIdle) {

            QueueStep first = steps.get(0);
            Set<UUID> newActiveIds = new LinkedHashSet<>();
            for (QueuedMessage qm : first.messages()) {
                open(qm.id(), qm.message());
                newActiveIds.add(qm.id());
            }
            CHANNEL_ACTIVE_IDS.put(channel, newActiveIds);

            Deque<QueueStep> newQueue = new ArrayDeque<>();
            for (int i = 1; i < steps.size(); i++) {
                newQueue.add(steps.get(i));
            }
            CHANNEL_QUEUES.put(channel, newQueue);
        } else {

            if (queue == null) {
                queue = new ArrayDeque<>();
                CHANNEL_QUEUES.put(channel, queue);
            }
            queue.addAll(steps);
        }
    }

    public static synchronized void clearQueue(String channel) {
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

    public static synchronized void stopQueue(String channel) {
        if (channel == null) {
            return;
        }
        Set<UUID> activeIds = CHANNEL_ACTIVE_IDS.remove(channel);
        if (activeIds != null) {
            activeIds.forEach(ACTIVE::remove);
        }
        CHANNEL_QUEUES.remove(channel);
    }

    public static synchronized void clearAllQueuesPending() {
        for (String channel : new ArrayList<>(CHANNEL_QUEUES.keySet())) {
            clearQueue(channel);
        }
    }

    public static synchronized void clearAllQueues() {
        closeAll();
        CHANNEL_QUEUES.clear();
        CHANNEL_ACTIVE_IDS.clear();
    }

    public static synchronized void tick(Minecraft minecraft) {
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

                        CHANNEL_QUEUES.remove(channel);
                        CHANNEL_ACTIVE_IDS.remove(channel);
                    }
                }
            }
        }
    }

    public static synchronized void render(GuiGraphics guiGraphics, float partialTick) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        for (ActiveMessage active : ACTIVE.values()) {
            active.render(guiGraphics, partialTick);
        }
    }
}
