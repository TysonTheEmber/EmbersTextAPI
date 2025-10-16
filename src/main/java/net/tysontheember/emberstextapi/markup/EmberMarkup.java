package net.tysontheember.emberstextapi.markup;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.tysontheember.emberstextapi.overlay.DrawOptions;
import net.tysontheember.emberstextapi.overlay.OverlayRenderer;

/**
 * Primary entry point for parsing and rendering Ember Markup.
 */
public final class EmberMarkup {
    private EmberMarkup() {
    }

    public static RSpan parse(String input) {
        return EmberParser.parse(input);
    }

    public static MutableComponent toComponent(String input) {
        return toComponent(parse(input));
    }

    public static MutableComponent toComponent(RNode node) {
        return ComponentEmitter.emit(node);
    }

    public static void draw(GuiGraphics graphics, String input, float x, float y, DrawOptions options) {
        draw(graphics, parse(input), x, y, options);
    }

    public static void draw(GuiGraphics graphics, RNode node, float x, float y, DrawOptions options) {
        OverlayRenderer.get().draw(graphics, node, x, y, options == null ? DrawOptions.defaults() : options);
    }
}
