package net.tysontheember.emberstextapi.inline;

import net.tysontheember.emberstextapi.inline.attrs.*;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class TagRegistry {
    private static final Map<String, TagFactory> FACTORIES = new HashMap<>();

    static {
        register("typewriter", (token, ctx) -> List.of(Typewriter.of(token.getFloat("speed", 1.0f), token.getFloat("delay", 0f), token.getString("cursor", ""))));
        register("wiggle", (token, ctx) -> List.of(Wiggle.of(token.getFloat("amp", 1.0f), token.getFloat("freq", 2.0f))));
        register("wave", (token, ctx) -> List.of(Wave.of(token.getFloat("amp", 1.0f), token.getFloat("freq", 1.0f))));
        register("rainb", (token, ctx) -> List.of(Rainbow.withSpeed(token.getFloat("speed", 1.0f))));
        register("rainbow", (token, ctx) -> List.of(Rainbow.withSpeed(token.getFloat("speed", 1.0f))));
        register("bold", (token, ctx) -> List.of(Bold.INSTANCE));
        register("italic", (token, ctx) -> List.of(Italic.INSTANCE));
        register("obf", (token, ctx) -> List.of(Obfuscated.INSTANCE));
        register("color", TagRegistry::parseColorAttribute);
        register("colour", TagRegistry::parseColorAttribute);
        register("gradient", TagRegistry::parseGradientAttribute);
    }

    private TagRegistry() {
    }

    public static void register(String name, TagFactory factory) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(factory, "factory");
        FACTORIES.put(normalise(name), factory);
    }

    public static TagFactory get(String name) {
        return FACTORIES.get(normalise(name));
    }

    private static String normalise(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private static List<TagAttribute> parseColorAttribute(TagToken token, TagParserContext context) {
        String value = token.getString("value", null);
        if (value == null && !token.attributes().isEmpty()) {
            value = token.attributes().values().iterator().next();
        }
        if (value == null) {
            return List.of(Color.of(0xFFFFFFFF));
        }
        return List.of(Color.of(parseColour(value)));
    }

    private static List<TagAttribute> parseGradientAttribute(TagToken token, TagParserContext context) {
        int from = parseColour(token.getString("from", "#FFFFFF"));
        int to = parseColour(token.getString("to", "#FFFFFF"));
        return List.of(Gradient.of(from, to));
    }

    private static int parseColour(String value) {
        String v = value.trim();
        if (v.startsWith("#")) {
            v = v.substring(1);
        }
        if (v.length() == 6) {
            return (int) (0xFF000000L | Long.parseLong(v, 16));
        }
        if (v.length() == 8) {
            return (int) Long.parseLong(v, 16);
        }
        return NamedColors.lookup(v);
    }

    private static final class NamedColors {
        private static final Map<String, Integer> COLORS = Map.ofEntries(
                Map.entry("black", 0xFF000000),
                Map.entry("dark_blue", 0xFF0000AA),
                Map.entry("dark_green", 0xFF00AA00),
                Map.entry("dark_aqua", 0xFF00AAAA),
                Map.entry("dark_red", 0xFFAA0000),
                Map.entry("dark_purple", 0xFFAA00AA),
                Map.entry("gold", 0xFFFFAA00),
                Map.entry("gray", 0xFFAAAAAA),
                Map.entry("grey", 0xFFAAAAAA),
                Map.entry("dark_gray", 0xFF555555),
                Map.entry("dark_grey", 0xFF555555),
                Map.entry("blue", 0xFF5555FF),
                Map.entry("green", 0xFF55FF55),
                Map.entry("aqua", 0xFF55FFFF),
                Map.entry("red", 0xFFFF5555),
                Map.entry("light_purple", 0xFFFF55FF),
                Map.entry("yellow", 0xFFFFFF55),
                Map.entry("white", 0xFFFFFFFF)
        );

        private static int lookup(String value) {
            return COLORS.getOrDefault(value.toLowerCase(Locale.ROOT), 0xFFFFFFFF);
        }
    }
}
