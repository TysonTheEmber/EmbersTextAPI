package net.tysontheember.emberstextapi.attributes.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.markup.RSpan;

import java.util.Locale;

public final class ColorAttribute implements AttributeHandler {
    @Override
    public String name() {
        return "color";
    }

    @Override
    public Style applyVanilla(Style base, RSpan span, AttributeContext ctx) {
        String value = span.attr("value");
        if (value == null || value.isEmpty()) {
            value = span.attr("hex");
        }
        if (value == null || value.isEmpty()) {
            value = span.attrs().values().stream().findFirst().orElse(null);
        }
        if (value == null || value.isEmpty()) {
            return base;
        }
        value = value.trim();
        ChatFormatting fmt = ChatFormatting.getByName(value.toUpperCase(Locale.ROOT));
        if (fmt != null && fmt.getColor() != null) {
            return base.withColor(fmt);
        }
        TextColor parsed = TextColor.parseColor(value.startsWith("#") ? value : "#" + value.replace("0x", ""));
        return parsed != null ? base.withColor(parsed) : base;
    }
}
