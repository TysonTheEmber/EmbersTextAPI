package net.tysontheember.emberstextapi.markup;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.BuiltinAttributes;

/**
 * Public API for Ember Markup.
 */
public final class EmberMarkup {
    private static final ResourceLocation SOURCE = new ResourceLocation("emberstextapi", "inline");

    static {
        BuiltinAttributes.init();
    }

    private EmberMarkup() {
    }

    public static RNode parse(String input) {
        return EmberParser.parse(input);
    }

    public static MutableComponent toComponent(String input) {
        return toComponent(parse(input));
    }

    public static MutableComponent toComponent(RNode node) {
        AttributeContext ctx = new AttributeContext(false, SOURCE);
        return ComponentEmitter.toComponent(node, ctx);
    }

    public static void draw(GuiGraphics graphics, String input, float x, float y, DrawOptions options) {
        draw(graphics, parse(input), x, y, options);
    }

    public static void draw(GuiGraphics graphics, RNode node, float x, float y, DrawOptions options) {
        net.tysontheember.emberstextapi.overlay.OverlayQueue queue = new net.tysontheember.emberstextapi.overlay.OverlayQueue();
        AttributeContext ctx = new AttributeContext(true, SOURCE, queue);
        MutableComponent component = ComponentEmitter.toComponent(node, ctx);
        Font font = Minecraft.getInstance().font;
        float scale = options.scale;
        boolean shadow = options.shadow;
        int color = options.colorOverride != null ? options.colorOverride : 0xFFFFFFFF;
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(x, y, 0.0F);
            if (scale != 1.0F) {
                graphics.pose().scale(scale, scale, 1.0F);
            }
            graphics.drawString(font, component, 0, 0, color, shadow);
        } finally {
            graphics.pose().popPose();
        }
        net.tysontheember.emberstextapi.overlay.OverlayRenderer.render(graphics, queue, 0.0F);
    }

    public static DrawOptions defaults() {
        return new DrawOptions(null, true, null, 1.0F);
    }

    public record DrawOptions(Integer colorOverride, boolean shadow, Integer wrapWidth, float scale) {
        public DrawOptions {
            scale = scale <= 0 ? 1.0F : scale;
        }

        public DrawOptions color(int value) {
            return new DrawOptions(value, shadow, wrapWidth, scale);
        }

        public DrawOptions shadow(boolean enabled) {
            return new DrawOptions(colorOverride, enabled, wrapWidth, scale);
        }

        public DrawOptions wrapWidth(Integer width) {
            return new DrawOptions(colorOverride, shadow, width, scale);
        }

        public DrawOptions scale(float newScale) {
            return new DrawOptions(colorOverride, shadow, wrapWidth, newScale);
        }
    }
}
