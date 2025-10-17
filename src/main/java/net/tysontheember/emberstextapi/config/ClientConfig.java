package net.tysontheember.emberstextapi.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Client-only configuration values for the Embers text animator. These options
 * allow players to tune rendering behavior without impacting servers.
 */
public final class ClientConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_TAGGED_CHAT;
    public static final ForgeConfigSpec.IntValue MAX_ACTIVE_EFFECTS_PER_LINE;
    public static final ForgeConfigSpec.BooleanValue SHOW_TAG_WARNINGS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Client text animation options").push("client");
        ENABLE_TAGGED_CHAT = builder
                .comment("When true, chat messages that look like they contain text tags will be rendered with Embers effects.")
                .define("enableTaggedChat", true);
        MAX_ACTIVE_EFFECTS_PER_LINE = builder
                .comment("Maximum number of compiled effects allowed per rendered line before the renderer bails out.")
                .defineInRange("maxActiveEffectsPerLine", 16, 0, 1024);
        SHOW_TAG_WARNINGS = builder
                .comment("If enabled, detailed warnings about malformed tags are shown in-game when debug logging is available.")
                .define("dev.showTagWarnings", false);
        builder.pop();

        SPEC = builder.build();
    }

    private ClientConfig() {
    }
}
