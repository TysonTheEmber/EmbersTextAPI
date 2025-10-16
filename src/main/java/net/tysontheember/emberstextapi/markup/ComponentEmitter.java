package net.tysontheember.emberstextapi.markup;

import com.google.common.collect.Sets;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.attributes.TextAttributes;
import net.tysontheember.emberstextapi.overlay.Markers;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static net.tysontheember.emberstextapi.markup.RNode.RSpan;
import static net.tysontheember.emberstextapi.markup.RNode.RText;

/**
 * Converts the parsed markup tree into vanilla {@link Component} instances.
 * Vanilla friendly attributes are applied directly while advanced effects are
 * hinted using invisible marker payloads so that the overlay renderer can pick
 * them up later.
 */
public final class ComponentEmitter {
    private static final Set<String> OVERLAY_TAGS = Sets.newHashSet(
        "gradient",
        "wave",
        "shake",
        "typewriter",
        "fade",
        "shadow",
        "outline",
        "bg"
    );

    private ComponentEmitter() {
    }

    public static MutableComponent emit(RSpan span) {
        return emit(span, AttributeContext.vanillaOnly());
    }

    public static MutableComponent emit(RSpan span, AttributeContext context) {
        MutableComponent root = Component.empty();
        for (RNode child : span.children()) {
            root.append(emitNode(child, context));
        }
        if (root.getSiblings().size() == 1 && root.getString().equals(root.getSiblings().get(0).getString())) {
            Component child = root.getSiblings().get(0);
            if (child instanceof MutableComponent mutable) {
                return mutable;
            }
            return child.copy();
        }
        return root;
    }

    private static MutableComponent emitNode(RNode node, AttributeContext context) {
        if (node instanceof RText text) {
            return Component.literal(text.text());
        }

        RSpan span = (RSpan) node;
        MutableComponent component = Component.empty();
        for (RNode child : span.children()) {
            component.append(emitNode(child, context));
        }

        AttributeHandler handler = TextAttributes.get(span.tag());
        if (handler != null) {
            handler.applyVanilla(component, span, context);
        } else {
            applySimpleDefaults(component, span);
        }

        if (requiresOverlay(handler, span.tag())) {
            String insertion = Markers.encode(span);
            if (insertion != null) {
                component = component.withStyle(style -> style.withInsertion(insertion));
            }
        }

        return component;
    }

    private static boolean requiresOverlay(AttributeHandler handler, String tag) {
        if (handler != null && handler.isOverlayOnly()) {
            return true;
        }
        return OVERLAY_TAGS.contains(tag.toLowerCase());
    }

    private static void applySimpleDefaults(MutableComponent component, RSpan span) {
        String name = span.tag().toLowerCase();
        switch (name) {
            case "bold" -> component.withStyle(style -> style.withBold(true));
            case "italic" -> component.withStyle(style -> style.withItalic(true));
            case "underline" -> component.withStyle(style -> style.withUnderlined(true));
            case "strikethrough" -> component.withStyle(style -> style.withStrikethrough(true));
            case "obfuscated" -> component.withStyle(style -> style.withObfuscated(true));
            case "color" -> colorFromAttrs(span.attrs()).ifPresent(color -> component.withStyle(style -> style.withColor(color)));
            case "font" -> fontFromAttrs(span.attrs()).ifPresent(font -> component.withStyle(style -> style.withFont(font)));
            default -> {
            }
        }
    }

    private static Optional<net.minecraft.network.chat.TextColor> colorFromAttrs(Map<String, String> attrs) {
        String value = attrs.getOrDefault("value", attrs.get("color"));
        if (value == null) {
            return Optional.empty();
        }
        String normalised = value.startsWith("#") ? value.substring(1) : value;
        try {
            int parsed = (int) Long.parseLong(normalised, 16);
            if (normalised.length() <= 6) {
                return Optional.of(net.minecraft.network.chat.TextColor.fromRgb(parsed));
            }
            return Optional.of(net.minecraft.network.chat.TextColor.fromRgb(parsed & 0xFFFFFF));
        } catch (NumberFormatException ignored) {
            ChatFormatting formatting = ChatFormatting.getByName(value.toUpperCase());
            if (formatting != null) {
                return Optional.of(net.minecraft.network.chat.TextColor.fromLegacyFormat(formatting));
            }
        }
        return Optional.empty();
    }

    private static Optional<ResourceLocation> fontFromAttrs(Map<String, String> attrs) {
        String value = attrs.get("value");
        if (StringUtil.isNullOrEmpty(value)) {
            return Optional.empty();
        }
        ResourceLocation font = ResourceLocation.tryParse(value);
        return Optional.ofNullable(font);
    }

}
