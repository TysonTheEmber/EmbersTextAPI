package net.tysontheember.emberstextapi.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.tysontheember.emberstextapi.EmbersTextAPI;
import net.tysontheember.emberstextapi.inline.InlineConfig;

@Mod.EventBusSubscriber(modid = EmbersTextAPI.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLE_INLINE_TAGS;
    public static final ForgeConfigSpec.BooleanValue LOG_UNKNOWN_TAGS;
    public static final ForgeConfigSpec.IntValue MAX_NESTING_DEPTH;
    public static final ForgeConfigSpec.IntValue MAX_PARSED_LENGTH;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("inlineTags");
        ENABLE_INLINE_TAGS = builder.comment("Enable parsing of inline markup tags.").define("enableInlineTags", true);
        LOG_UNKNOWN_TAGS = builder.comment("Log unknown tags for debugging.").define("logUnknownTags", false);
        MAX_NESTING_DEPTH = builder.comment("Maximum allowed nesting depth for tags.").defineInRange("maxNestingDepth", 16, 1, 64);
        MAX_PARSED_LENGTH = builder.comment("Maximum characters processed when parsing inline markup.").defineInRange("maxParsedLength", 16_384, 128, 1_000_000);
        builder.pop();
        SPEC = builder.build();
    }

    private ClientConfig() {
    }

    public static void bake() {
        InlineConfig.apply(ENABLE_INLINE_TAGS.get(), LOG_UNKNOWN_TAGS.get(), MAX_NESTING_DEPTH.get(), MAX_PARSED_LENGTH.get());
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            bake();
        }
    }
}
