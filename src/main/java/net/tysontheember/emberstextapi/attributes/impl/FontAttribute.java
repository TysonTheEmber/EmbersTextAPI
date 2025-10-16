package net.tysontheember.emberstextapi.attributes.impl;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.markup.RSpan;

public final class FontAttribute implements AttributeHandler {
    @Override
    public String name() {
        return "font";
    }

    @Override
    public Style applyVanilla(Style base, RSpan span, AttributeContext ctx) {
        String value = span.attr("value");
        if (value == null || value.isEmpty()) {
            value = span.attrs().values().stream().findFirst().orElse(null);
        }
        if (value == null || value.isEmpty()) {
            return base;
        }
        return base.withFont(ResourceLocation.tryParse(value));
    }
}
