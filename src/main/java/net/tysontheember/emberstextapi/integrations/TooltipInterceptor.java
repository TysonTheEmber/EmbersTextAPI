package net.tysontheember.emberstextapi.integrations;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.tysontheember.emberstextapi.markup.EmberMarkup;
import net.tysontheember.emberstextapi.overlay.DrawOptions;

/**
 * Simple helper used by tooltip events. This class does not subscribe to Forge
 * events directly; those hooks live in the client initialization code.
 */
public final class TooltipInterceptor {
    private TooltipInterceptor() {
    }

    public static void draw(GuiGraphics graphics, Component component, int x, int y) {
        EmberMarkup.draw(graphics, component.getString(), x, y, DrawOptions.defaults());
    }
}
