package net.tysontheember.emberstextapi.overlay;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.markup.RNode;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Utilities for encoding/decoding overlay markers.
 */
public final class Markers {
    private static final String PREFIX = "\u200C{ember:attr:";
    private static final String SUFFIX = "}";

    private Markers() {
    }

    public static MutableComponent encode(RNode.RSpan span) {
        JsonObject json = new JsonObject();
        json.addProperty("tag", span.tag());
        JsonObject attrs = new JsonObject();
        for (Map.Entry<String, String> entry : span.attrs().entrySet()) {
            attrs.addProperty(entry.getKey(), entry.getValue());
        }
        json.add("attrs", attrs);
        String payload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(json.toString().getBytes(StandardCharsets.UTF_8));
        return Component.literal(PREFIX + payload + SUFFIX).withStyle(Style.EMPTY.withObfuscated(true));
    }

    public static RNode.RSpan decode(Component component) {
        String str = component.getString();
        if (!str.startsWith(PREFIX) || !str.endsWith(SUFFIX)) {
            return null;
        }
        String payload = str.substring(PREFIX.length(), str.length() - SUFFIX.length());
        try {
            String json = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            String tag = obj.get("tag").getAsString();
            JsonObject attrs = obj.getAsJsonObject("attrs");
            java.util.Map<String, String> map = new java.util.HashMap<>();
            if (attrs != null) {
                for (Map.Entry<String, com.google.gson.JsonElement> entry : attrs.entrySet()) {
                    map.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
            return new RNode.RSpan(tag, map, java.util.List.of());
        } catch (IllegalArgumentException | JsonSyntaxException ex) {
            return null;
        }
    }
}
