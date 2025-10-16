package net.tysontheember.emberstextapi.overlay;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.tysontheember.emberstextapi.markup.ComponentEmitter;
import net.tysontheember.emberstextapi.markup.RSpan;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Utility for encoding and decoding overlay marker payloads.
 */
public final class Markers {
    private static final String PREFIX = "\u200C{ember:attr:";
    private static final String SUFFIX = "}";

    private Markers() {
    }

    public static MutableComponent encode(RSpan span) {
        String payload = ComponentEmitter.encodeMarkerPayload(span);
        return Component.literal(PREFIX + payload + SUFFIX);
    }

    public static RSpan decode(Component component) {
        String str = component.getString();
        if (!str.startsWith(PREFIX) || !str.endsWith(SUFFIX)) {
            return null;
        }
        String payload = str.substring(PREFIX.length(), str.length() - SUFFIX.length());
        byte[] data = Base64.getUrlDecoder().decode(payload);
        JsonObject json = JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonObject();
        JsonObject attrs = json.getAsJsonObject("attrs");
        return new RSpan(json.get("tag").getAsString(), attrs.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAsString())),
                java.util.List.of());
    }
}
