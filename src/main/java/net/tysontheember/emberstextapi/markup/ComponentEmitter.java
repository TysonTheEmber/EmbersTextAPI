package net.tysontheember.emberstextapi.markup;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.attributes.AttributeContext;
import net.tysontheember.emberstextapi.attributes.AttributeHandler;
import net.tysontheember.emberstextapi.attributes.TextAttributes;
import net.tysontheember.emberstextapi.overlay.Markers;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Converts {@link RNode} trees into vanilla {@link Component} hierarchies.
 */
public final class ComponentEmitter {
    private ComponentEmitter() {
    }

    public static MutableComponent emit(RNode node) {
        return emit(node, Style.EMPTY);
    }

    public static MutableComponent emit(RNode node, Style parentStyle) {
        if (node instanceof RText text) {
            return Component.literal(text.text()).setStyle(parentStyle);
        }
        if (node instanceof RSpan span) {
            AttributeContext ctx = new AttributeContext();
            AttributeHandler handler = TextAttributes.get(span.tag());
            Style style = handler != null ? handler.applyVanilla(parentStyle, span, ctx) : parentStyle;
            List<MutableComponent> children = new ArrayList<>();
            for (RNode child : span.children()) {
                children.add(emit(child, style));
            }
            if (children.isEmpty()) {
                children.add(Component.empty().setStyle(style));
            }
            if (!ctx.requiresOverlay() && children.size() == 1) {
                return children.get(0);
            }
            MutableComponent root = Component.empty();
            for (MutableComponent child : children) {
                root.append(child);
            }
            if (ctx.requiresOverlay()) {
                root.append(Markers.encode(span));
            }
            return root;
        }
        return Component.empty();
    }

    public static String encodeMarkerPayload(RSpan span) {
        JsonObject json = new JsonObject();
        json.addProperty("tag", span.tag());
        JsonObject attrs = new JsonObject();
        for (Map.Entry<String, String> entry : span.attrs().entrySet()) {
            attrs.add(entry.getKey(), new JsonPrimitive(entry.getValue()));
        }
        json.add("attrs", attrs);
        String raw = json.toString();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
