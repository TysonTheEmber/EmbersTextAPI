package net.tysontheember.emberstextapi.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration manager for EmbersTextAPI (NeoForge 1.21.1).
 * Uses NeoForge's config system to create a TOML config file.
 */
public class ModConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue WELCOME_MESSAGE_ENABLED;

    static {
        BUILDER.comment("EmbersTextAPI Configuration").push("general");

        WELCOME_MESSAGE_ENABLED = BUILDER
            .comment("Enable the welcome message shown to players on first join")
            .define("welcomeMessageEnabled", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    /**
     * Check if welcome message is enabled.
     */
    public static boolean isWelcomeMessageEnabled() {
        return WELCOME_MESSAGE_ENABLED.get();
    }

    /**
     * Set welcome message enabled state.
     */
    public static void setWelcomeMessageEnabled(boolean enabled) {
        WELCOME_MESSAGE_ENABLED.set(enabled);
        SPEC.save();
    }
}
