package net.tysontheember.emberstextapi.immersivemessages.effects.preset;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class PresetRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(PresetRegistry.class);

    private static final Map<String, PresetDefinition> PRESETS = new ConcurrentHashMap<>();

    private static final Set<String> RESERVED_TAG_NAMES = Set.of(

            "bold", "b", "italic", "i", "underline", "u", "strikethrough", "s", "obfuscated", "obf",

            "color", "c", "font",

            "grad", "gradient", "typewriter", "type",
            "shake", "charshake", "wave",
            "obfuscate", "scramble",
            "rainbow", "rainb", "glitch", "bounce", "pulse", "swing",
            "turb", "turbulence", "circle", "wiggle", "pend", "pendulum", "neon", "glow",
            "shadow", "fade",

            "background", "bg", "backgroundgradient", "bggradient",
            "scale", "offset", "anchor", "align",

            "item", "entity"
    );

    private PresetRegistry() {}

    public static void register(@NotNull PresetDefinition preset) {
        String name = preset.getName().toLowerCase();
        if (RESERVED_TAG_NAMES.contains(name)) {
            LOGGER.error("Cannot register preset '{}': name is reserved by a built-in tag", name);
            throw new IllegalArgumentException("Preset name is reserved: " + name);
        }
        PRESETS.put(name, preset);
        LOGGER.debug("Registered preset: {}", name);
    }

    @Nullable
    public static PresetDefinition get(@NotNull String name) {
        return PRESETS.get(name.toLowerCase());
    }

    public static boolean isPreset(@NotNull String name) {
        return PRESETS.containsKey(name.toLowerCase());
    }

    public static boolean isReserved(@NotNull String name) {
        return RESERVED_TAG_NAMES.contains(name.toLowerCase());
    }

    public static void clear() {
        PRESETS.clear();
        LOGGER.debug("Cleared preset registry");
    }

    @NotNull
    public static Set<String> getRegisteredPresets() {
        return Collections.unmodifiableSet(PRESETS.keySet());
    }
}
