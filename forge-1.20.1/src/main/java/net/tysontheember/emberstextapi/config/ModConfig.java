package net.tysontheember.emberstextapi.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;

/**
 * Configuration manager for EmbersTextAPI.
 * Uses Forge's config system to create a TOML config file.
 */
public class ModConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue WELCOME_MESSAGE_ENABLED;

    static {
        BUILDER.comment("EmbersTextAPI Configuration").push("general");

        WELCOME_MESSAGE_ENABLED = BUILDER
            .comment("Enable the welcome message shown to players on first join")
            .define("welcomeMessageEnabled", true);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    /**
     * Register the config file.
     */
    @SuppressWarnings("deprecation")
    public static void register() {
        ModLoadingContext.get().registerConfig(Type.COMMON, SPEC, "emberstextapi-common.toml");
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
