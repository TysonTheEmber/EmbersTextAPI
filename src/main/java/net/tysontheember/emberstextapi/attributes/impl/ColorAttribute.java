package net.tysontheember.emberstextapi.attributes.impl;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.MutableComponent;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.markup.RNode.RSpan;

import java.util.Locale;
import java.util.Map;

public final class ColorAttribute implements AttributeHandler {
    @Override
    public String name() {
        return "color";
    }

    @Override
    public void applyVanilla(MutableComponent component, RSpan span, AttributeContext ctx) {
        span.attrs().entrySet().stream()
            .filter(entry -> entry.getKey().equals("value") || entry.getKey().equals("color"))
            .map(Map.Entry::getValue)
            .map(ColorAttribute::parse)
            .filter(color -> color != null)
            .findFirst()
            .ifPresent(color -> component.withStyle(style -> style.withColor(color)));
    }

    private static TextColor parse(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("#")) {
            trimmed = trimmed.substring(1);
        }
        try {
            int rgb = (int) Long.parseLong(trimmed, 16);
            if (trimmed.length() > 6) {
                rgb &= 0xFFFFFF;
            }
            return TextColor.fromRgb(rgb);
        } catch (NumberFormatException ignored) {
            ChatFormatting fmt = ChatFormatting.getByName(trimmed.toUpperCase(Locale.ROOT));
            if (fmt != null) {
                return TextColor.fromLegacyFormat(fmt);
            }
        }
        return null;
    }
}
