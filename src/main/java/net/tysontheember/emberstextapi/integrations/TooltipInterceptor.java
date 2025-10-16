package net.tysontheember.emberstextapi.integrations;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.tysontheember.emberstextapi.markup.EmberMarkup;

/**
 * Stub tooltip integration to showcase the API surface.
 */
public final class TooltipInterceptor {
    private TooltipInterceptor() {
    }

    public static void drawTooltip(GuiGraphics graphics, Component component, int x, int y) {
        EmberMarkup.draw(graphics, component.getString(), x, y, EmberMarkup.defaults());
    }
}
