package net.tysontheember.emberstextapi.sdf;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.font.GlyphProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Parses SDF font provider definitions from font JSON files.
 * <p>
 * This class intentionally does NOT import any {@code org.lwjgl.util.freetype} classes
 * at the class level. FreeType-dependent code is isolated in {@link FreeTypeManager} and
 * {@link SDFGlyphProvider}, which are only loaded after a runtime availability check.
 * This prevents {@link NoClassDefFoundError} on platforms where LWJGL FreeType is not
 * on the classpath (e.g., Forge 1.20.1 which ships LWJGL 3.3.1 without the freetype module).
 */
public final class SDFGlyphProviderDefinition {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/SDFGlyphProviderDef");

    /** Cached result of FreeType availability check */
    private static Boolean freeTypeAvailable;

    public static final String TYPE_ID = "emberstextapi:sdf";

    private SDFGlyphProviderDefinition() {}

    /**
     * Check if a JSON provider entry is an SDF provider.
     */
    public static boolean isSdfProvider(JsonObject json) {
        return json.has("type") && TYPE_ID.equals(json.get("type").getAsString());
    }

    /**
     * Check if the LWJGL FreeType module is available at runtime.
     * Uses Class.forName() to avoid importing FreeType classes directly.
     */
    public static boolean isFreeTypeAvailable() {
        if (freeTypeAvailable == null) {
            try {
                Class.forName("org.lwjgl.util.freetype.FreeType");
                freeTypeAvailable = true;
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                LOGGER.warn("LWJGL FreeType module not available — SDF font providers disabled. " +
                        "This is expected on Forge 1.20.1 (LWJGL 3.3.1). " +
                        "SDF fonts require NeoForge 1.21.1+ or Fabric with LWJGL 3.3.3+.");
                freeTypeAvailable = false;
            }
        }
        return freeTypeAvailable;
    }

    /**
     * Parse an SDF provider definition from JSON and load the font.
     *
     * @param json            The provider JSON object
     * @param resourceManager MC resource manager for loading font files
     * @return The GlyphProvider, or null if loading fails
     */
    @Nullable
    public static GlyphProvider load(JsonObject json, ResourceManager resourceManager) {
        if (!isFreeTypeAvailable()) {
            return null;
        }

        if (!FreeTypeManager.getInstance().isAvailable()) {
            LOGGER.warn("FreeType library initialization failed — SDF font provider disabled");
            return null;
        }

        try {
            // Parse configuration
            String fileStr = json.get("file").getAsString();
            ResourceLocation fontFile = ResourceLocation.parse(fileStr);

            float size = getFloat(json, "size", 16.0f);
            int sdfResolution = getInt(json, "sdf_resolution", 48);
            int padding = getInt(json, "padding", 4);
            float spread = getFloat(json, "spread", 4.0f);
            float oversample = getFloat(json, "oversample", 1.0f);
            String skip = getString(json, "skip", "");

            // New MSDF-specific fields with backward compat
            float pxRange;
            if (json.has("px_range")) {
                pxRange = getFloat(json, "px_range", 8.0f);
            } else {
                // Backward compat: convert old spread to pxRange
                pxRange = spread * 2.0f;
            }
            float angleThreshold = getFloat(json, "angle_threshold", 3.0f);

            float[] shift = new float[]{0, 0};
            if (json.has("shift") && json.get("shift").isJsonArray()) {
                var arr = json.getAsJsonArray("shift");
                if (arr.size() >= 2) {
                    shift[0] = arr.get(0).getAsFloat();
                    shift[1] = arr.get(1).getAsFloat();
                }
            }

            SDFConfig config = new SDFConfig(sdfResolution, padding, spread, size, oversample,
                    shift, skip, pxRange, angleThreshold);

            // Load font file from resources
            ResourceLocation fontResourceLoc = ResourceLocation.fromNamespaceAndPath(
                    fontFile.getNamespace(), "font/" + fontFile.getPath());

            ByteBuffer fontData = loadFontData(resourceManager, fontResourceLoc);
            if (fontData == null) {
                LOGGER.error("Failed to load font file: {}", fontResourceLoc);
                return null;
            }

            // Delegate to FreeType-dependent code (safe because isFreeTypeAvailable() passed)
            return SDFGlyphProviderFactory.create(fontData, config);

        } catch (Exception e) {
            LOGGER.error("Failed to create SDF glyph provider", e);
            return null;
        }
    }

    @Nullable
    private static ByteBuffer loadFontData(ResourceManager resourceManager, ResourceLocation location) {
        try {
            var resource = resourceManager.getResource(location);
            if (resource.isEmpty()) {
                return null;
            }
            try (InputStream is = resource.get().open()) {
                byte[] bytes = is.readAllBytes();
                ByteBuffer buf = MemoryUtil.memAlloc(bytes.length);
                buf.put(bytes);
                buf.flip();
                return buf;
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read font file: {}", location, e);
            return null;
        }
    }

    private static float getFloat(JsonObject json, String key, float defaultValue) {
        return json.has(key) ? json.get(key).getAsFloat() : defaultValue;
    }

    private static int getInt(JsonObject json, String key, int defaultValue) {
        return json.has(key) ? json.get(key).getAsInt() : defaultValue;
    }

    private static String getString(JsonObject json, String key, String defaultValue) {
        return json.has(key) ? json.get(key).getAsString() : defaultValue;
    }
}
