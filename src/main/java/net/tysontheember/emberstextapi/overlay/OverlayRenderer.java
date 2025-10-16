package net.tysontheember.emberstextapi.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Simplified overlay renderer stub.
 */
public final class OverlayRenderer {
    private OverlayRenderer() {
    }

    public static void render(GuiGraphics graphics, OverlayQueue queue, float partialTick) {
        if (queue == null || queue.tasks().isEmpty()) {
            return;
        }
        PoseStack pose = graphics.pose();
        pose.pushPose();
        try {
            for (OverlayQueue.OverlayTask task : queue.tasks()) {
                drawFallback(graphics, task);
            }
        } finally {
            pose.popPose();
            queue.clear();
        }
    }

    private static void drawFallback(GuiGraphics graphics, OverlayQueue.OverlayTask task) {
        // For now we simply re-draw the component as-is which guarantees a readable fallback.
        Minecraft minecraft = Minecraft.getInstance();
        graphics.drawString(minecraft.font, task.run().component(), (int) task.run().x(), (int) task.run().y(), 0xFFFFFFFF, false);
    }
}
