package net.tysontheember.emberstextapi.debug;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * Renders the debug overlay on top of the HUD. The overlay currently displays configuration
 * state so that developers can validate that commands and hotkeys operate correctly.
 */
public final class DebugOverlayRenderer {
    private static final int LINE_HEIGHT = 10;

    private DebugOverlayRenderer() {
    }

    public static void render(GuiGraphics graphics, float partialTick) {
        if (!DebugOverlay.shouldRender()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return;
        }

        Font font = minecraft.font;
        List<Component> lines = DebugOverlay.gatherOverlayLines();
        int y = 4;
        for (Component line : lines) {
            graphics.drawString(font, line, 6, y, 0xFFFFFF, false);
            y += LINE_HEIGHT;
        }
    }
}
