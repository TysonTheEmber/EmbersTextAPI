package net.tysontheember.emberstextapi.attributes.impl;

import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.markup.RSpan;

public final class ItalicAttribute implements AttributeHandler {
    @Override
    public String name() {
        return "italic";
    }

    @Override
    public Style applyVanilla(Style base, RSpan span, AttributeContext ctx) {
        return base.withItalic(true);
    }
}
