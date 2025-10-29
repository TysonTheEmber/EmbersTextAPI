package net.tysontheember.emberstextapi.debug;

import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Defines the debug configuration model for the Embers Text API. All config keys
 * are created here so that other systems can query {@link DebugFlags} for the
 * resolved values.
 */
public final class DebugConfig {
    public static final String CONFIG_FILE = "emberstextapi-debug.toml";

    public static final ForgeConfigSpec SPEC;

    static final ForgeConfigSpec.BooleanValue DEBUG_ENABLED;
    static final ForgeConfigSpec.BooleanValue TRACE_PARSE;
    static final ForgeConfigSpec.BooleanValue TRACE_LAYOUT;
    static final ForgeConfigSpec.BooleanValue TRACE_RENDER;
    static final ForgeConfigSpec.BooleanValue TRACE_CACHE;
    static final ForgeConfigSpec.BooleanValue TRACE_MIXIN;
    static final ForgeConfigSpec.BooleanValue OVERLAY_ENABLED;
    static final ForgeConfigSpec.IntValue OVERLAY_LEVEL;
    static final ForgeConfigSpec.BooleanValue PERF_ENABLED;
    static final ForgeConfigSpec.BooleanValue FAIL_SAFE_ON_ERROR;
    static final ForgeConfigSpec.BooleanValue SPAN_EVERYWHERE;
    static final ForgeConfigSpec.IntValue EFFECTS_VERSION;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Debug configuration for Embers Text API").push("debug");

        DEBUG_ENABLED = builder
                .comment("Master toggle for the Embers Text API debug tooling.")
                .define("enabled", false);

        builder.comment("Per-system trace toggles").push("trace");
        TRACE_PARSE = builder.comment("Emit parse lifecycle events").define("parse", false);
        TRACE_LAYOUT = builder.comment("Emit layout lifecycle events").define("layout", false);
        TRACE_RENDER = builder.comment("Emit render lifecycle events").define("render", false);
        TRACE_CACHE = builder.comment("Emit cache lifecycle events").define("cache", false);
        TRACE_MIXIN = builder.comment("Emit mixin lifecycle events").define("mixin", false);
        builder.pop();

        builder.comment("Debug overlay configuration").push("overlay");
        OVERLAY_ENABLED = builder.comment("Enable the debug overlay when debug mode is enabled.")
                .define("enabled", false);
        OVERLAY_LEVEL = builder.comment("Overlay verbosity level. 0 disables extra details.")
                .defineInRange("level", 0, 0, 3);
        builder.pop();

        builder.comment("Performance instrumentation").push("perf");
        PERF_ENABLED = builder.comment("Enable debug performance timers.")
                .define("enabled", false);
        builder.pop();

        builder.comment("Rendering safety").push("safety");
        FAIL_SAFE_ON_ERROR = builder
                .comment("Enable fail-safe fallback when a span pipeline error is detected.")
                .define("failSafeOnError", true);
        builder.pop();

        builder.comment("Span system defaults").push("spans");
        SPAN_EVERYWHERE = builder
                .comment("Route vanilla rendering entry points through the span pipeline.")
                .define("spanEverywhere", false);
        EFFECTS_VERSION = builder
                .comment("Version marker for span effect cache keys.")
                .defineInRange("effectsVersion", 2, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.pop();

        SPEC = builder.build();
    }

    private DebugConfig() {
    }

    /**
     * Responds to a Forge config event and updates the in-memory debug flags when the
     * debug specification is modified.
     */
    public static void load(ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (config.getSpec() == SPEC) {
            DebugFlags.reload();
            DebugEnvironment.getEventBus().post(DebugEvents.configReloaded());
        }
    }
}
