package net.tysontheember.emberstextapi.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Extremely small overlay implementation that simply redraws vanilla text as a
 * fallback.  The class exists so that external mods can hook into a stable API
 * while we iterate on more advanced rendering strategies.
 */
@OnlyIn(Dist.CLIENT)
public final class OverlayRenderer {
    private static final OverlayRenderer INSTANCE = new OverlayRenderer();

    private final OverlayQueue queue = new OverlayQueue();

    private OverlayRenderer() {
    }

    public static OverlayRenderer instance() {
        return INSTANCE;
    }

    public OverlayQueue queue() {
        return queue;
    }

    public void render(GuiGraphics graphics) {
        Font font = Minecraft.getInstance().font;
        OverlayBatches batches = new OverlayBatches();
        for (OverlayQueue.Entry entry : queue.entries()) {
            batches.add(entry);
        }
        for (var batch : batches.batches().values()) {
            for (OverlayQueue.Entry entry : batch) {
                LayoutRun run = entry.run();
                Component component = run.component();
                graphics.drawString(font, component, (int) run.x(), (int) run.y(), 0xFFFFFF, true);
            }
        }
        queue.clear();
    }
}
