package net.tysontheember.emberstextapi.overlay;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.tysontheember.emberstextapi.markup.RNode.RSpan;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility helpers for encoding overlay attribute markers into {@link net.minecraft.network.chat.Style#insertion}.
 */
public final class Markers {
    private static final String PREFIX = "ember:attr:";

    private Markers() {
    }

    public static @Nullable String encode(RSpan span) {
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("tag", span.tag());
            JsonObject attrs = new JsonObject();
            for (Map.Entry<String, String> entry : span.attrs().entrySet()) {
                attrs.addProperty(entry.getKey(), entry.getValue());
            }
            obj.add("attrs", attrs);
            return encodePayload(obj.toString());
        } catch (Exception ignored) {
            return null;
        }
    }

    public static @Nullable RSpan decode(@Nullable String insertion) {
        String json = decodeJson(insertion);
        if (json == null) {
            return null;
        }
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            String tag = obj.get("tag").getAsString();
            JsonObject attrs = obj.getAsJsonObject("attrs");
            Map<String, String> map = new HashMap<>();
            for (var entry : attrs.entrySet()) {
                map.put(entry.getKey(), entry.getValue().getAsString());
            }
            return new RSpan(tag, map, java.util.List.of());
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String encodePayload(String json) {
        return PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public static @Nullable String decodeJson(@Nullable String insertion) {
        if (insertion == null || !insertion.startsWith(PREFIX)) {
            return null;
        }
        String payload = insertion.substring(PREFIX.length());
        byte[] data = Base64.getUrlDecoder().decode(payload);
        return new String(data, StandardCharsets.UTF_8);
    }

    public static MutableComponent stripInsertions(Component component) {
        MutableComponent copy = component.copy();
        copy.setStyle(copy.getStyle().withInsertion(null));
        List<Component> children = new ArrayList<>(copy.getSiblings());
        copy.getSiblings().clear();
        for (Component child : children) {
            copy.append(stripInsertions(child));
        }
        return copy;
    }
}
