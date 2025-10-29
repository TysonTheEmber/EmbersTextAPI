package net.tysontheember.emberstextapi.debug;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tysontheember.emberstextapi.EmbersTextAPI;

/**
 * Hooks Forge GUI overlay rendering to draw the debug overlay when enabled.
 */
@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID, value = Dist.CLIENT)
public final class DebugOverlayEvents {
    private DebugOverlayEvents() {
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        DebugOverlayRenderer.render(event.getGuiGraphics(), event.getPartialTick());
    }
}
