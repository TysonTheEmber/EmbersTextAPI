package net.tysontheember.emberstextapi.immersivemessages.effects.preset;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Loads effect preset definitions from {@code assets/<namespace>/presets/*.json}
 * via the Minecraft resource manager.
 * <p>
 * Each JSON file defines a single preset. The filename (minus {@code .json})
 * becomes the tag name. Resource packs can override individual presets.
 * </p>
 */
public final class PresetLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PresetLoader.class);
    private static final Gson GSON = new Gson();
    private static final String PRESETS_PATH = "presets";
    private static final int SUPPORTED_FORMAT_VERSION = 1;

    private PresetLoader() {}

    /**
     * Load all preset JSON files from the resource manager.
     *
     * @param resourceManager the client resource manager
     * @return list of successfully parsed preset definitions
     */
    public static List<PresetDefinition> loadAll(ResourceManager resourceManager) {
        Map<ResourceLocation, Resource> resources = resourceManager.listResources(
                PRESETS_PATH, loc -> loc.getPath().endsWith(".json"));

        List<PresetDefinition> presets = new ArrayList<>();

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation location = entry.getKey();
            Resource resource = entry.getValue();

            try {
                PresetDefinition preset = loadSingle(location, resource);
                if (preset != null) {
                    presets.add(preset);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load preset from {}: {}", location, e.getMessage());
            }
        }

        LOGGER.info("Loaded {} effect presets", presets.size());
        return presets;
    }

    /**
     * Parse a single preset JSON file into a {@link PresetDefinition}.
     * Returns {@code null} if validation fails (errors are logged).
     */
    private static PresetDefinition loadSingle(ResourceLocation location, Resource resource) {
        // Derive tag name from filename: "presets/legendary.json" -> "legendary"
        String path = location.getPath();
        String filename = path.substring(path.lastIndexOf('/') + 1);
        String presetName = filename.replace(".json", "").toLowerCase();

        String jsonContent;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            jsonContent = sb.toString();
        } catch (Exception e) {
            LOGGER.error("Preset '{}' (from {}) could not be read: {}", presetName, location, e.getMessage());
            return null;
        }

        return parseJson(presetName, jsonContent);
    }

    /**
     * Parse a JSON string into a {@link PresetDefinition}.
     * <p>
     * Validates format version, effect types, and reserved name conflicts.
     * Returns {@code null} if validation fails (errors are logged).
     * </p>
     *
     * @param presetName the tag name for this preset
     * @param jsonContent raw JSON string
     * @return parsed preset, or {@code null} on validation failure
     */
    public static PresetDefinition parseJson(String presetName, String jsonContent) {
        presetName = presetName.toLowerCase();

        if (PresetRegistry.isReserved(presetName)) {
            LOGGER.error("Preset '{}' conflicts with a built-in tag name — skipping", presetName);
            return null;
        }

        JsonObject root;
        try {
            root = GSON.fromJson(jsonContent, JsonObject.class);
        } catch (JsonSyntaxException e) {
            LOGGER.error("Preset '{}' contains invalid JSON: {}", presetName, e.getMessage());
            return null;
        }

        if (root == null) {
            LOGGER.error("Preset '{}' is empty", presetName);
            return null;
        }

        // Validate format_version
        if (!root.has("format_version")) {
            LOGGER.error("Preset '{}' is missing required field 'format_version'", presetName);
            return null;
        }
        int formatVersion = root.get("format_version").getAsInt();
        if (formatVersion != SUPPORTED_FORMAT_VERSION) {
            LOGGER.error("Preset '{}' has unsupported format_version {} (expected {})",
                    presetName, formatVersion, SUPPORTED_FORMAT_VERSION);
            return null;
        }

        // Parse effects array
        if (!root.has("effects") || !root.get("effects").isJsonArray()) {
            LOGGER.error("Preset '{}' is missing required 'effects' array", presetName);
            return null;
        }
        JsonArray effectsArray = root.getAsJsonArray("effects");
        if (effectsArray.isEmpty()) {
            LOGGER.error("Preset '{}' has an empty 'effects' array", presetName);
            return null;
        }

        List<PresetDefinition.EffectEntry> effects = new ArrayList<>();
        for (int i = 0; i < effectsArray.size(); i++) {
            JsonObject effectObj = effectsArray.get(i).getAsJsonObject();
            if (!effectObj.has("type")) {
                LOGGER.error("Preset '{}' effect at index {} is missing 'type'", presetName, i);
                return null;
            }
            String type = effectObj.get("type").getAsString().toLowerCase();

            if (!EffectRegistry.isRegistered(type)) {
                LOGGER.error("Preset '{}' references unknown effect type '{}' at index {}",
                        presetName, type, i);
                return null;
            }

            Map<String, Object> params = new LinkedHashMap<>();
            if (effectObj.has("params") && effectObj.get("params").isJsonObject()) {
                JsonObject paramsObj = effectObj.getAsJsonObject("params");
                for (Map.Entry<String, JsonElement> paramEntry : paramsObj.entrySet()) {
                    params.put(paramEntry.getKey(), jsonElementToValue(paramEntry.getValue()));
                }
            }

            effects.add(new PresetDefinition.EffectEntry(type, params));
        }

        // Parse optional styles
        PresetDefinition.StyleOverrides styles = null;
        if (root.has("styles") && root.get("styles").isJsonObject()) {
            JsonObject stylesObj = root.getAsJsonObject("styles");
            styles = new PresetDefinition.StyleOverrides(
                    getOptionalBoolean(stylesObj, "bold"),
                    getOptionalBoolean(stylesObj, "italic"),
                    getOptionalBoolean(stylesObj, "underline"),
                    getOptionalBoolean(stylesObj, "strikethrough"),
                    getOptionalBoolean(stylesObj, "obfuscated"),
                    getOptionalString(stylesObj, "color"),
                    getOptionalString(stylesObj, "font")
            );
        }

        return new PresetDefinition(presetName, formatVersion, effects, styles);
    }

    private static Object jsonElementToValue(JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive prim = element.getAsJsonPrimitive();
            if (prim.isBoolean()) return prim.getAsBoolean();
            if (prim.isNumber()) return prim.getAsDouble();
            return prim.getAsString();
        }
        return element.toString();
    }

    private static Boolean getOptionalBoolean(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsBoolean() : null;
    }

    private static String getOptionalString(JsonObject obj, String key) {
        return obj.has(key) ? obj.get(key).getAsString() : null;
    }
}
