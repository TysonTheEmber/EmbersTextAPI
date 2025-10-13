package net.tysontheember.emberstextapi.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.immersivemessages.api.ImmersiveMessage;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
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

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
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

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (ACTIVE.isEmpty()) {
            return;
        }
        for (ActiveMessage active : ACTIVE.values()) {
            active.render(event.getGuiGraphics(), event.getPartialTick());
        }
    }
}
