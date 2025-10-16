package net.tysontheember.emberstextapi.markup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.tysontheember.emberstextapi.attributes.BuiltinAttributes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.overlay.LayoutRun;
import net.tysontheember.emberstextapi.overlay.Markers;
import net.tysontheember.emberstextapi.overlay.OverlayQueue;
import net.tysontheember.emberstextapi.overlay.OverlayRenderer;

import javax.annotation.Nullable;
import java.util.Optional;

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
            collectOverlayMarkers(queue, component, x, y, options.scale);
        }
    }

    private static void collectOverlayMarkers(OverlayQueue queue, Component component, float x, float y, float scale) {
        component.visit((style, text) -> {
            if (text.isEmpty()) {
                return Optional.<Component>empty();
            }
            var header = Markers.decode(style.getInsertion());
            if (header != null) {
                MutableComponent segment = Component.literal(text).withStyle(style);
                queue.enqueue(header.tag(), header, new LayoutRun(segment, x, y, scale));
            }
            return Optional.empty();
        }, Style.EMPTY);
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
