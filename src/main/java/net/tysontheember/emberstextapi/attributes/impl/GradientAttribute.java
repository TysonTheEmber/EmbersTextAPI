package net.tysontheember.emberstextapi.attributes.impl;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.markup.RSpan;

public final class GradientAttribute implements AttributeHandler {
    @Override
    public String name() {
        return "gradient";
    }

    @Override
    public Style applyVanilla(Style base, RSpan span, AttributeContext ctx) {
        String from = span.attr("from");
        if (from != null) {
            TextColor color = TextColor.parseColor(from.startsWith("#") ? from : "#" + from);
            if (color != null) {
                base = base.withColor(color);
            }
        }
        ctx.markOverlay(span);
        return base;
    }
}
