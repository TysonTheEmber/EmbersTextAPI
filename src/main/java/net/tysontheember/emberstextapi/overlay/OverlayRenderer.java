package net.tysontheember.emberstextapi.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.tysontheember.emberstextapi.markup.ComponentEmitter;
import net.tysontheember.emberstextapi.markup.RNode;

/**
 * Extremely small overlay renderer that currently draws fallback text while still
 * keeping track of spans that require advanced effects. The batching hooks are in
 * place so future revisions can perform animated rendering without breaking the API.
 */
public final class OverlayRenderer {
    private static final OverlayRenderer INSTANCE = new OverlayRenderer();

    private final OverlayBatches batches = new OverlayBatches();

    private OverlayRenderer() {
    }

    public static OverlayRenderer get() {
        return INSTANCE;
    }

    public void draw(GuiGraphics graphics, RNode node, float x, float y, DrawOptions options) {
        Component component = ComponentEmitter.emit(node);
        Font font = Minecraft.getInstance().font;
        if (options.wrapWidth() != null && options.wrapWidth() > 0) {
            graphics.drawWordWrap(font, component, (int) x, (int) y, options.wrapWidth(), options.colorOverride());
        } else {
            graphics.drawString(font, component, (int) x, (int) y, options.colorOverride(), options.shadow());
        }
    }

    public OverlayBatches batches() {
        return batches;
    }
}
