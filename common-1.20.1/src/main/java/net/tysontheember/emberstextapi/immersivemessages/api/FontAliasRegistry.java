package net.tysontheember.emberstextapi.immersivemessages.api;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class FontAliasRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(FontAliasRegistry.class);

    private static final Map<String, ResourceLocation> ALIASES = new ConcurrentHashMap<>();
    private static final Set<String> BUILT_IN_ALIASES = ConcurrentHashMap.newKeySet();
    private static volatile boolean initialized = false;
    private static volatile boolean locked = false;

    private static final Set<ResourceLocation> NO_BOLD_VARIANT = ConcurrentHashMap.newKeySet();

    private FontAliasRegistry() {}

    @Nullable
    public static ResourceLocation resolve(String nameOrId) {
        if (nameOrId == null || nameOrId.isEmpty()) return null;

        ResourceLocation aliased = ALIASES.get(nameOrId.toLowerCase());
        if (aliased != null) {
            LOGGER.trace("Resolved font alias '{}' -> {}", nameOrId, aliased);
            return aliased;
        }

        ResourceLocation direct = ResourceLocation.tryParse(nameOrId);
        if (direct != null) {
            LOGGER.trace("Resolved font ID directly: {}", direct);
            return direct;
        }

        LOGGER.debug("Unknown font name or invalid ResourceLocation: '{}'", nameOrId);
        return null;
    }

    public static void registerCustom(String alias, ResourceLocation fontId) {
        if (alias == null || alias.isEmpty()) throw new IllegalArgumentException("Font alias must not be empty");
        if (fontId == null) throw new IllegalArgumentException("Font ResourceLocation must not be null");

        String key = alias.toLowerCase();
        if (locked && BUILT_IN_ALIASES.contains(key)) {
            LOGGER.warn("registerCustom: '{}' is a built-in ETA font alias — overwriting is not supported. " +
                    "Choose a different alias name.", key);
            return;
        }

        ResourceLocation prev = ALIASES.put(key, fontId);
        if (prev != null) {
            LOGGER.warn("Font alias '{}' overwritten: {} -> {}", key, prev, fontId);
        } else {
            LOGGER.debug("Registered custom font alias: '{}' -> {}", key, fontId);
        }
    }

    public static Map<String, ResourceLocation> getAliases() {
        return Collections.unmodifiableMap(ALIASES);
    }

    public static boolean isAlias(String alias) {
        return alias != null && ALIASES.containsKey(alias.toLowerCase());
    }

    public static boolean hasBoldVariant(ResourceLocation font) {
        return font != null && !NO_BOLD_VARIANT.contains(font);
    }

    public static synchronized void initBuiltins() {
        if (initialized) {
            LOGGER.debug("FontAliasRegistry already initialized");
            return;
        }

        LOGGER.info("Initializing FontAliasRegistry with built-in font aliases");

        registerBuiltIn("norse",         new ResourceLocation("emberstextapi", "norse"));
        registerBuiltIn("norse_bold",    new ResourceLocation("emberstextapi", "norse_bold"));

        registerBuiltIn("metamorphous",  new ResourceLocation("emberstextapi", "metamorphous"));
        registerBuiltIn("meta",          new ResourceLocation("emberstextapi", "metamorphous"));
        NO_BOLD_VARIANT.add(new ResourceLocation("emberstextapi", "metamorphous"));

        registerBuiltIn("cinzel",        new ResourceLocation("emberstextapi", "cinzel"));
        registerBuiltIn("cinzel_bold",   new ResourceLocation("emberstextapi", "cinzel_bold"));

        registerBuiltIn("almendra",      new ResourceLocation("emberstextapi", "almendra"));
        registerBuiltIn("almendra_bold", new ResourceLocation("emberstextapi", "almendra_bold"));

        registerBuiltIn("cardo",         new ResourceLocation("emberstextapi", "cardo"));
        registerBuiltIn("cardo_bold",    new ResourceLocation("emberstextapi", "cardo_bold"));

        initialized = true;
        locked = true;
        LOGGER.info("FontAliasRegistry initialized with {} aliases (registry locked)", ALIASES.size());
    }

    private static void registerBuiltIn(String alias, ResourceLocation fontId) {
        String key = alias.toLowerCase();
        ALIASES.put(key, fontId);
        BUILT_IN_ALIASES.add(key);
        LOGGER.debug("Registered built-in font alias: '{}' -> {}", key, fontId);
    }

    public static synchronized void clear() {
        ALIASES.clear();
        BUILT_IN_ALIASES.clear();
        initialized = false;
        locked = false;
        LOGGER.debug("FontAliasRegistry cleared");
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static boolean isLocked() {
        return locked;
    }
}
