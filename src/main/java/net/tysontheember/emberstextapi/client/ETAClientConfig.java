package net.tysontheember.emberstextapi.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.tysontheember.emberstextapi.EmbersTextAPI;

/**
 * Client configuration bindings for EmbersTextAPI.
 */
@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ETAClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.BooleanValue ENABLE_GLOBAL_SPANS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_TYPEWRITER;
    public static final ForgeConfigSpec SPEC;

    private static ModConfig clientConfig;

    static {
        BUILDER.push("general");
        ENABLE_GLOBAL_SPANS = BUILDER.comment("Enable EmbersTextAPI global span rendering.")
            .define("enableGlobalSpans", true);
        ENABLE_TYPEWRITER = BUILDER.comment("Enable typewriter playback for tagged text.")
            .define("enableTypewriter", true);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }

    private ETAClientConfig() {
    }

    @SuppressWarnings("removal")
    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC, "emberstextapi-client.toml");
    }

    public static void save() {
        if (clientConfig != null) {
            clientConfig.save();
        }
    }

    @SubscribeEvent
    public static void onConfigEvent(ModConfigEvent event) {
        ModConfig config = event.getConfig();
        if (config.getSpec() == SPEC) {
            clientConfig = config;
        }
    }
}
