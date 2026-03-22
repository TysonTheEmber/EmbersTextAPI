package net.tysontheember.emberstextapi.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration manager for EmbersTextAPI (NeoForge 1.21.1).
 * Uses NeoForge's config system to create TOML config files.
 * <p>
 * Server-side options (welcome message, markup permissions) go in COMMON config.
 * Client-side options (immersive messages, effects, limits) go in CLIENT config.
 */
public class ModConfig {

    // === COMMON config (server-side) ===
    private static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec COMMON_SPEC;

    public static final ModConfigSpec.BooleanValue WELCOME_MESSAGE_ENABLED;

    // Player Markup Access
    public static final ModConfigSpec.ConfigValue<String> MARKUP_PERMISSION_MODE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> MARKUP_PLAYER_LIST;

    // === CLIENT config ===
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec CLIENT_SPEC;

    public static final ModConfigSpec.BooleanValue IMMERSIVE_MESSAGES_ENABLED;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_EFFECTS;
    public static final ModConfigSpec.IntValue MAX_MESSAGE_DURATION;
    public static final ModConfigSpec.IntValue MAX_ACTIVE_MESSAGES;

    // Server-side message limits
    public static final ModConfigSpec.IntValue MAX_SERVER_MESSAGE_DURATION;
    public static final ModConfigSpec.IntValue MAX_SERVER_ACTIVE_MESSAGES;
    public static final ModConfigSpec.IntValue MAX_QUEUE_SIZE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ALLOWED_EFFECTS;

    // Markup
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DISALLOWED_MARKUP_TAGS;

    // === CLIENT extras ===
    // Accessibility
    public static final ModConfigSpec.BooleanValue REDUCE_MOTION;
    public static final ModConfigSpec.IntValue MAX_NEON_QUALITY;

    // Performance
    public static final ModConfigSpec.IntValue TEXT_LAYOUT_CACHE_SIZE;
    public static final ModConfigSpec.BooleanValue SDF_ENABLED;

    // === COMMON (anvil) ===
    public static final ModConfigSpec.IntValue ANVIL_NAME_MAX_LENGTH;

    static {
        // --- COMMON ---
        COMMON_BUILDER.comment("EmbersTextAPI Server Configuration").push("general");

        WELCOME_MESSAGE_ENABLED = COMMON_BUILDER
            .comment("Enable the welcome message shown to players on first join")
            .define("welcomeMessageEnabled", true);

        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Player Markup Access", "Controls which players can use markup tags like <rainbow> in chat").push("markup");

        MARKUP_PERMISSION_MODE = COMMON_BUILDER
            .comment("Permission mode: NONE (no restrictions), WHITELIST (only listed players), BLACKLIST (listed players blocked)")
            .define("markupPermissionMode", "NONE");

        MARKUP_PLAYER_LIST = COMMON_BUILDER
            .comment("Player UUIDs for the whitelist/blacklist")
            .defineListAllowEmpty("markupPlayerList", ArrayList::new, e -> e instanceof String);

        DISALLOWED_MARKUP_TAGS = COMMON_BUILDER
            .comment("Tags stripped from player-authored chat markup (e.g. [\"glitch\", \"neon\"]). Does not affect API-sent messages.")
            .defineListAllowEmpty("disallowedMarkupTags", ArrayList::new, e -> e instanceof String);

        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Server-side Message Limits").push("messages");

        MAX_SERVER_MESSAGE_DURATION = COMMON_BUILDER
            .comment("Server-enforced max message duration in ticks. 0 = unlimited. 1200 = 60 seconds.")
            .defineInRange("maxServerMessageDuration", 1200, 0, 72000);

        MAX_SERVER_ACTIVE_MESSAGES = COMMON_BUILDER
            .comment("Server-enforced max simultaneous messages per player. 0 = unlimited.")
            .defineInRange("maxServerActiveMessages", 10, 0, 100);

        MAX_QUEUE_SIZE = COMMON_BUILDER
            .comment("Max pending steps per queue channel. 0 = unlimited. Prevents memory exhaustion.")
            .defineInRange("maxQueueSize", 50, 0, 1000);

        ALLOWED_EFFECTS = COMMON_BUILDER
            .comment("If non-empty, only listed effects are allowed in server-sent messages. Empty = all allowed.")
            .defineListAllowEmpty("allowedEffects", ArrayList::new, e -> e instanceof String);

        COMMON_BUILDER.pop();

        COMMON_BUILDER.comment("Anvil").push("anvil");

        ANVIL_NAME_MAX_LENGTH = COMMON_BUILDER
            .comment("Maximum number of characters allowed when renaming an item in an anvil. Vanilla default is 50.")
            .defineInRange("anvilNameMaxLength", 50, 1, Integer.MAX_VALUE);

        COMMON_BUILDER.pop();
        COMMON_SPEC = COMMON_BUILDER.build();

        // --- CLIENT ---
        CLIENT_BUILDER.comment("EmbersTextAPI Client Configuration").push("general");

        IMMERSIVE_MESSAGES_ENABLED = CLIENT_BUILDER
            .comment("Master toggle for immersive messages (sent via commands/API)")
            .define("immersiveMessagesEnabled", true);

        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.comment("Effect Settings").push("effects");

        DISABLED_EFFECTS = CLIENT_BUILDER
            .comment("List of effect names to disable globally (e.g. [\"glitch\", \"shake\"]). Disabled effects render as plain text.")
            .defineListAllowEmpty("disabledEffects", ArrayList::new, e -> e instanceof String);

        REDUCE_MOTION = CLIENT_BUILDER
            .comment("Disables all positional/motion effects (wave, bounce, swing, shake, etc.). Color effects still work. Accessibility feature.")
            .define("reduceMotion", false);

        MAX_NEON_QUALITY = CLIENT_BUILDER
            .comment("Caps neon/glow quality. 1=fast(6 samples), 2=balanced(12), 3=quality(20). Lower for better performance.")
            .defineInRange("maxNeonQuality", 3, 1, 3);

        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.comment("Limits").push("limits");

        MAX_MESSAGE_DURATION = CLIENT_BUILDER
            .comment("Maximum immersive message duration in ticks. 0 = unlimited.")
            .defineInRange("maxMessageDuration", 0, 0, Integer.MAX_VALUE);

        MAX_ACTIVE_MESSAGES = CLIENT_BUILDER
            .comment("Maximum simultaneous active messages per player. 0 = unlimited.")
            .defineInRange("maxActiveMessages", 0, 0, Integer.MAX_VALUE);

        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.comment("Performance").push("performance");

        TEXT_LAYOUT_CACHE_SIZE = CLIENT_BUILDER
            .comment("Max entries in TextLayoutCache LRU. Higher = more memory, fewer recomputations.")
            .defineInRange("textLayoutCacheSize", 256, 64, 2048);

        SDF_ENABLED = CLIENT_BUILDER
            .comment("Master toggle for SDF font rendering. When false, SDF fonts fall back to vanilla bitmap.")
            .define("sdfEnabled", true);

        CLIENT_BUILDER.pop();

        CLIENT_SPEC = CLIENT_BUILDER.build();
    }

    // --- COMMON accessors ---

    public static boolean isWelcomeMessageEnabled() {
        return WELCOME_MESSAGE_ENABLED.get();
    }

    public static void setWelcomeMessageEnabled(boolean enabled) {
        WELCOME_MESSAGE_ENABLED.set(enabled);
        COMMON_SPEC.save();
    }

    public static String getMarkupPermissionMode() {
        return MARKUP_PERMISSION_MODE.get();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getMarkupPlayerList() {
        return (List<String>) (List<?>) MARKUP_PLAYER_LIST.get();
    }

    // --- CLIENT accessors (return defaults if client config not loaded) ---

    public static boolean isImmersiveMessagesEnabled() {
        try {
            return IMMERSIVE_MESSAGES_ENABLED.get();
        } catch (Exception e) {
            return true;
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> getDisabledEffects() {
        try {
            return (List<String>) (List<?>) DISABLED_EFFECTS.get();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static int getMaxMessageDuration() {
        try {
            return MAX_MESSAGE_DURATION.get();
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getMaxActiveMessages() {
        try {
            return MAX_ACTIVE_MESSAGES.get();
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getAnvilNameMaxLength() {
        return ANVIL_NAME_MAX_LENGTH.get();
    }

    // --- New COMMON accessors ---

    public static int getMaxServerMessageDuration() {
        return MAX_SERVER_MESSAGE_DURATION.get();
    }

    public static int getMaxServerActiveMessages() {
        return MAX_SERVER_ACTIVE_MESSAGES.get();
    }

    public static int getMaxQueueSize() {
        return MAX_QUEUE_SIZE.get();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getAllowedEffects() {
        return (List<String>) (List<?>) ALLOWED_EFFECTS.get();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getDisallowedMarkupTags() {
        return (List<String>) (List<?>) DISALLOWED_MARKUP_TAGS.get();
    }

    // --- New CLIENT accessors ---

    public static boolean isReduceMotionEnabled() {
        try {
            return REDUCE_MOTION.get();
        } catch (Exception e) {
            return false;
        }
    }

    public static int getMaxNeonQuality() {
        try {
            return MAX_NEON_QUALITY.get();
        } catch (Exception e) {
            return 3;
        }
    }

    public static int getTextLayoutCacheSize() {
        try {
            return TEXT_LAYOUT_CACHE_SIZE.get();
        } catch (Exception e) {
            return 256;
        }
    }

    public static boolean isSdfEnabled() {
        try {
            return SDF_ENABLED.get();
        } catch (Exception e) {
            return true;
        }
    }
}
