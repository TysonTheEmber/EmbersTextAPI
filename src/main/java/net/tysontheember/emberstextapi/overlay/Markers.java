package net.tysontheember.emberstextapi.overlay;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.tysontheember.emberstextapi.markup.RNode.RSpan;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

/**
 * Serialises overlay spans into lightweight invisible markers.
 */
public final class Markers {
    private static final String PREFIX = "\u200C{ember:attr:";
    private static final String SUFFIX = "}";

    private Markers() {
    }

    public static Optional<String> encode(RSpan span) {
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("tag", span.tag());
            JsonObject attrs = new JsonObject();
            for (Map.Entry<String, String> entry : span.attrs().entrySet()) {
                attrs.addProperty(entry.getKey(), entry.getValue());
            }
            obj.add("attrs", attrs);
            byte[] data = obj.toString().getBytes(StandardCharsets.UTF_8);
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(data);
            return Optional.of(PREFIX + encoded + SUFFIX);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public static Optional<RSpan> decode(Component component) {
        String literal = component.getString();
        if (literal == null || literal.length() <= PREFIX.length()) {
            return Optional.empty();
        }
        int idx = literal.indexOf(PREFIX);
        if (idx == -1) {
            return Optional.empty();
        }
        int start = idx + PREFIX.length();
        int end = literal.indexOf(SUFFIX, start);
        if (end == -1) {
            return Optional.empty();
        }
        String payload = literal.substring(start, end);
        try {
            byte[] data = Base64.getUrlDecoder().decode(payload);
            String json = new String(data, StandardCharsets.UTF_8);
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            String tag = obj.get("tag").getAsString();
            JsonObject attrs = obj.getAsJsonObject("attrs");
            java.util.Map<String, String> map = new java.util.HashMap<>();
            for (var entry : attrs.entrySet()) {
                map.put(entry.getKey(), entry.getValue().getAsString());
            }
            return Optional.of(new RSpan(tag, map, java.util.List.of()));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}
