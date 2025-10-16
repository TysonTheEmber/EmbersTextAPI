package net.tysontheember.emberstextapi.markup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import net.tysontheember.emberstextapi.attributes.BuiltinAttributes;
import net.tysontheember.emberstextapi.overlay.LayoutRun;
import net.tysontheember.emberstextapi.overlay.OverlayQueue;
import net.tysontheember.emberstextapi.overlay.OverlayRenderer;

import javax.annotation.Nullable;

import static net.tysontheember.emberstextapi.markup.RNode.RSpan;

/**
 * Entry point for Ember markup parsing and rendering.
 */
public final class EmberMarkup {
    static {
        BuiltinAttributes.init();
    }

    private EmberMarkup() {
    }

    public static RSpan parse(String input) {
        return EmberParser.parse(input);
    }

    public static MutableComponent toComponent(String input) {
        return toComponent(parse(input));
    }

    public static MutableComponent toComponent(RSpan ast) {
        return ComponentEmitter.emit(ast);
    }

    public static void draw(GuiGraphics graphics, String input, float x, float y, DrawOptions options) {
        draw(graphics, parse(input), x, y, options);
    }

    public static void draw(GuiGraphics graphics, RSpan ast, float x, float y, DrawOptions options) {
        MutableComponent component = ComponentEmitter.emit(ast);
        drawComponent(graphics, component, x, y, options);
    }

    private static void drawComponent(GuiGraphics graphics, MutableComponent component, float x, float y, DrawOptions options) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int color = options.colorOverride != null ? options.colorOverride : 0xFFFFFF;
        graphics.drawString(font, component, (int) x, (int) y, color, options.shadow);
        if (options.enableOverlay) {
            OverlayRenderer renderer = OverlayRenderer.instance();
            OverlayQueue queue = renderer.queue();
            queue.enqueue("root", new RSpan("root", java.util.Map.of(), java.util.List.of()), new LayoutRun(component, x, y, options.scale));
        }
    }

    public static DrawOptions defaults() {
        return new DrawOptions(true, 1.0f, null, true, null);
    }

    public record DrawOptions(boolean shadow, float scale, @Nullable Integer wrapWidth, boolean enableOverlay, @Nullable Integer colorOverride) {
        public static DrawOptions defaults() {
            return new DrawOptions(true, 1.0f, null, true, null);
        }
    }
}
