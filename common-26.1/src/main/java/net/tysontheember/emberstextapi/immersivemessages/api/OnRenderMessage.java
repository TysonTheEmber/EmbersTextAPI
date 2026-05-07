package net.tysontheember.emberstextapi.immersivemessages.api;

import net.minecraft.client.gui.GuiGraphicsExtractor;

@FunctionalInterface
public interface OnRenderMessage {
    void render(GuiGraphicsExtractor graphics, ImmersiveMessage message, int x, int y, float alpha);
}
