package net.tysontheember.emberstextapi.immersivemessages.api;

import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface OnRenderMessage {
    void render(GuiGraphics graphics, ImmersiveMessage message, int x, int y, float alpha);
}
