package net.tysontheember.emberstextapi.immersivemessages.effects.preset;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry for effect presets.
 * <p>
 * Presets are loaded from JSON resource files and registered here so the
 * {@link net.tysontheember.emberstextapi.immersivemessages.api.MarkupParser}
 * can resolve custom tag names (e.g. {@code <legendary>}) to bundled effects
 * and styles.
 * </p>
 */
public final class PresetRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(PresetRegistry.class);

    private static final Map<String, PresetDefinition> PRESETS = new ConcurrentHashMap<>();

    /**
     * Built-in tag names from MarkupParser that presets must never shadow.
     */
    private static final Set<String> RESERVED_TAG_NAMES = Set.of(
            // Text formatting
            "bold", "b", "italic", "i", "underline", "u", "strikethrough", "s", "obfuscated", "obf",
            // Color / font
            "color", "c", "font",
            // Effect tags
            "grad", "gradient", "typewriter", "type",
            "shake", "charshake", "wave",
            "obfuscate", "scramble",
            "rainbow", "rainb", "glitch", "bounce", "pulse", "swing",
            "turb", "turbulence", "circle", "wiggle", "pend", "pendulum", "scroll", "neon", "glow",
            "shadow", "fade",
            // Global message attributes
            "background", "bg", "backgroundgradient", "bggradient",
            "scale", "offset", "anchor", "align",
            // Special rendering
            "item", "entity"
    );

    private PresetRegistry() {}

    /**
     * Register a preset definition. Rejects names that conflict with built-in tags.
     *
     * @param preset the preset to register
     * @throws IllegalArgumentException if the name is reserved
     */
    public static void register(@NotNull PresetDefinition preset) {
        String name = preset.getName().toLowerCase();
        if (RESERVED_TAG_NAMES.contains(name)) {
            LOGGER.error("Cannot register preset '{}': name is reserved by a built-in tag", name);
            throw new IllegalArgumentException("Preset name is reserved: " + name);
        }
        PRESETS.put(name, preset);
        LOGGER.debug("Registered preset: {}", name);
    }

    /**
     * Look up a preset by tag name.
     *
     * @param name tag name (case-insensitive)
     * @return the preset definition, or {@code null} if not found
     */
    @Nullable
    public static PresetDefinition get(@NotNull String name) {
        return PRESETS.get(name.toLowerCase());
    }

    /**
     * Check whether a name corresponds to a registered preset.
     */
    public static boolean isPreset(@NotNull String name) {
        return PRESETS.containsKey(name.toLowerCase());
    }

    /**
     * Check whether a name is reserved by a built-in tag and therefore cannot
     * be used as a preset name.
     */
    public static boolean isReserved(@NotNull String name) {
        return RESERVED_TAG_NAMES.contains(name.toLowerCase());
    }

    /**
     * Remove all registered presets. Called before reloading.
     */
    public static void clear() {
        PRESETS.clear();
        LOGGER.debug("Cleared preset registry");
    }

    /**
     * Get all registered preset names (unmodifiable view).
     */
    @NotNull
    public static Set<String> getRegisteredPresets() {
        return Collections.unmodifiableSet(PRESETS.keySet());
    }
}
