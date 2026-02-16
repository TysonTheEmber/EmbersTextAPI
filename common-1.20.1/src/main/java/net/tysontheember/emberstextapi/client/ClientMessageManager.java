package net.tysontheember.emberstextapi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

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
