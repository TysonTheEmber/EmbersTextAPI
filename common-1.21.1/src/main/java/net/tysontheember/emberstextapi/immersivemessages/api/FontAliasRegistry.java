package net.tysontheember.emberstextapi.immersivemessages.api;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for font short-name aliases, mapping user-friendly names to full ResourceLocations.
 *
 * <p>This registry allows markup authors to write {@code <font name=cinzel>} instead of
 * {@code <font id=emberstextapi:cinzel>}. The alias system falls back to direct
 * {@link ResourceLocation#tryParse} for unrecognised names, so full IDs always work.</p>
 *
 * <p>Built-in aliases are registered once during mod initialization and cannot be overwritten
 * after the registry is locked. Third-party mods may register additional aliases via
 * {@link #registerCustom(String, ResourceLocation)}.</p>
 *
 * <h3>Usage (markup):</h3>
 * <pre>{@code
 * <font name=cinzel>Text in Cinzel</font>
 * <font name=norse>Text in Norse</font>
 * <font id=emberstextapi:cinzel>Full ID still works</font>
 * }</pre>
 *
 * <h3>Usage (Java API):</h3>
 * <pre>{@code
 * ResourceLocation font = FontAliasRegistry.resolve("cinzel");
 * span.font(font);
 * }</pre>
 */
public final class FontAliasRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(FontAliasRegistry.class);

    /** Map of lowercase alias → ResourceLocation. */
    private static final Map<String, ResourceLocation> ALIASES = new ConcurrentHashMap<>();

    /** Set of alias names that are built-in and protected from overwrite after lock. */
    private static final Set<String> BUILT_IN_ALIASES = ConcurrentHashMap.newKeySet();

    /** Whether built-in aliases have been registered. Idempotent guard. */
    private static volatile boolean initialized = false;

    /** When true, built-in aliases cannot be overwritten. */
    private static volatile boolean locked = false;

    /**
     * Set of ResourceLocation paths for fonts that have no {@code _bold} variant.
     * The bold auto-switch in {@link MarkupParser} checks this before constructing
     * the {@code _bold} resource name, preventing a silent fallback to the default font.
     */
    private static final Set<ResourceLocation> NO_BOLD_VARIANT = ConcurrentHashMap.newKeySet();

    private FontAliasRegistry() {}

    /**
     * Resolve a short name or full ResourceLocation string to a ResourceLocation.
     *
     * <p>Resolution order:
     * <ol>
     *   <li>Check the alias table (case-insensitive)</li>
     *   <li>Fall through to {@link ResourceLocation#tryParse(String)} for namespaced IDs</li>
     * </ol>
     * </p>
     *
     * @param nameOrId Short alias (e.g., {@code "cinzel"}) or full ID (e.g., {@code "emberstextapi:cinzel"})
     * @return Resolved ResourceLocation, or {@code null} if unresolvable
     */
    @Nullable
    public static ResourceLocation resolve(String nameOrId) {
        if (nameOrId == null || nameOrId.isEmpty()) return null;

        // 1. Alias lookup (case-insensitive)
        ResourceLocation aliased = ALIASES.get(nameOrId.toLowerCase());
        if (aliased != null) {
            LOGGER.trace("Resolved font alias '{}' -> {}", nameOrId, aliased);
            return aliased;
        }

        // 2. Direct ResourceLocation parse (handles "namespace:path" forms)
        ResourceLocation direct = ResourceLocation.tryParse(nameOrId);
        if (direct != null) {
            LOGGER.trace("Resolved font ID directly: {}", direct);
            return direct;
        }

        LOGGER.debug("Unknown font name or invalid ResourceLocation: '{}'", nameOrId);
        return null;
    }

    /**
     * Register a custom alias for use with the {@code <font>} markup tag.
     *
     * <p>Unlike built-in aliases, custom aliases can be registered at any time and
     * are never locked. A warning is logged if the alias would overwrite a built-in name.</p>
     *
     * @param alias   Short name (case-insensitive; will be stored lowercase)
     * @param fontId  Full ResourceLocation of the font
     */
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

    /**
     * Get an unmodifiable view of all registered aliases.
     *
     * @return Unmodifiable alias map (lowercase alias → ResourceLocation)
     */
    public static Map<String, ResourceLocation> getAliases() {
        return Collections.unmodifiableMap(ALIASES);
    }

    /**
     * Check whether a short name is a registered alias.
     *
     * @param alias Short name to check (case-insensitive)
     * @return {@code true} if the alias is registered
     */
    public static boolean isAlias(String alias) {
        return alias != null && ALIASES.containsKey(alias.toLowerCase());
    }

    /**
     * Return {@code true} if the given font has a {@code _bold} variant registered.
     * Fonts explicitly listed in {@code NO_BOLD_VARIANT} return {@code false} so that
     * {@link MarkupParser} skips the bold auto-switch and keeps the regular face instead
     * of falling back to Minecraft's default font.
     *
     * @param font ResourceLocation of the font (must not be the {@code _bold} variant itself)
     * @return {@code true} if a bold variant should be attempted
     */
    public static boolean hasBoldVariant(ResourceLocation font) {
        return font != null && !NO_BOLD_VARIANT.contains(font);
    }

    /**
     * Initialize built-in font aliases. Called once from each loader's client entry point
     * alongside {@link net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry#initializeDefaultEffects()}.
     *
     * <p>This method is idempotent — subsequent calls are ignored.</p>
     */
    public static synchronized void initBuiltins() {
        if (initialized) {
            LOGGER.debug("FontAliasRegistry already initialized");
            return;
        }

        LOGGER.info("Initializing FontAliasRegistry with built-in font aliases");

        // Norse
        registerBuiltIn("norse",         ResourceLocation.fromNamespaceAndPath("emberstextapi", "norse"));
        registerBuiltIn("norse_bold",    ResourceLocation.fromNamespaceAndPath("emberstextapi", "norse_bold"));

        // Metamorphous (no bold variant — suppress auto bold-switch)
        registerBuiltIn("metamorphous",  ResourceLocation.fromNamespaceAndPath("emberstextapi", "metamorphous"));
        registerBuiltIn("meta",          ResourceLocation.fromNamespaceAndPath("emberstextapi", "metamorphous"));
        NO_BOLD_VARIANT.add(ResourceLocation.fromNamespaceAndPath("emberstextapi", "metamorphous"));

        // Cinzel
        registerBuiltIn("cinzel",        ResourceLocation.fromNamespaceAndPath("emberstextapi", "cinzel"));
        registerBuiltIn("cinzel_bold",   ResourceLocation.fromNamespaceAndPath("emberstextapi", "cinzel_bold"));

        // Almendra
        registerBuiltIn("almendra",      ResourceLocation.fromNamespaceAndPath("emberstextapi", "almendra"));
        registerBuiltIn("almendra_bold", ResourceLocation.fromNamespaceAndPath("emberstextapi", "almendra_bold"));

        // Cardo
        registerBuiltIn("cardo",         ResourceLocation.fromNamespaceAndPath("emberstextapi", "cardo"));
        registerBuiltIn("cardo_bold",    ResourceLocation.fromNamespaceAndPath("emberstextapi", "cardo_bold"));

        initialized = true;
        locked = true;
        LOGGER.info("FontAliasRegistry initialized with {} aliases (registry locked)", ALIASES.size());
    }

    /**
     * Register a built-in alias (internal use only — called during {@link #initBuiltins()}).
     */
    private static void registerBuiltIn(String alias, ResourceLocation fontId) {
        String key = alias.toLowerCase();
        ALIASES.put(key, fontId);
        BUILT_IN_ALIASES.add(key);
        LOGGER.debug("Registered built-in font alias: '{}' -> {}", key, fontId);
    }

    // -----------------------------------------------------------------------
    // Test support
    // -----------------------------------------------------------------------

    /**
     * Clear all registrations and reset initialization state.
     * <strong>For testing only — do not call in production code.</strong>
     */
    public static synchronized void clear() {
        ALIASES.clear();
        BUILT_IN_ALIASES.clear();
        initialized = false;
        locked = false;
        LOGGER.debug("FontAliasRegistry cleared");
    }

    /** @return {@code true} if {@link #initBuiltins()} has been called. */
    public static boolean isInitialized() {
        return initialized;
    }

    /** @return {@code true} if the built-in aliases are locked against overwrite. */
    public static boolean isLocked() {
        return locked;
    }
}
