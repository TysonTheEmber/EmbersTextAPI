package net.tysontheember.emberstextapi.attributes.impl;

import net.minecraft.network.chat.MutableComponent;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.markup.RNode.RSpan;

public final class UnderlineAttribute implements AttributeHandler {
    @Override
    public String name() {
        return "underline";
    }

    @Override
    public void applyVanilla(MutableComponent component, RSpan span, AttributeContext ctx) {
        component.withStyle(style -> style.withUnderlined(true));
    }
}
