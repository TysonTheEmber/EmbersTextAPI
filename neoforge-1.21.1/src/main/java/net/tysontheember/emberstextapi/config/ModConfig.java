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

        CLIENT_BUILDER.pop();

        CLIENT_BUILDER.comment("Limits").push("limits");

        MAX_MESSAGE_DURATION = CLIENT_BUILDER
            .comment("Maximum immersive message duration in ticks. 0 = unlimited.")
            .defineInRange("maxMessageDuration", 0, 0, Integer.MAX_VALUE);

        MAX_ACTIVE_MESSAGES = CLIENT_BUILDER
            .comment("Maximum simultaneous active messages per player. 0 = unlimited.")
            .defineInRange("maxActiveMessages", 0, 0, Integer.MAX_VALUE);

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
}
