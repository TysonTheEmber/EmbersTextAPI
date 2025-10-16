package net.tysontheember.emberstextapi.attributes.impl;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.markup.RNode.RSpan;

public final class FontAttribute implements AttributeHandler {
    @Override
    public String name() {
        return "font";
    }

    @Override
    public void applyVanilla(MutableComponent component, RSpan span, AttributeContext ctx) {
        String value = span.attrs().get("value");
        if (value == null || value.isEmpty()) {
            return;
        }
        ResourceLocation font = ResourceLocation.tryParse(value);
        if (font != null) {
            component.withStyle(style -> style.withFont(font));
        }
    }
}
