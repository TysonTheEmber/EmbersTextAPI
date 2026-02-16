package net.tysontheember.emberstextapi.immersivemessages.api;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Callback used to customise how a message is drawn.
 */
@FunctionalInterface
public interface OnRenderMessage {
    void render(GuiGraphics graphics, ImmersiveMessage message, int x, int y, float alpha);
}
