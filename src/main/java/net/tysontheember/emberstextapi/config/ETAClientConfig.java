package net.tysontheember.emberstextapi.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.tysontheember.emberstextapi.client.text.GlobalTextConfig;

public final class ETAClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ANIMATIONS_ENABLED;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Client settings for Embers Text API").push("client");
        ANIMATIONS_ENABLED = builder
                .comment("Enable animated span effects (shake, obfuscate, typewriter, gradients)")
                .define("animationsEnabled", true);
        builder.pop();
        SPEC = builder.build();
    }

    private ETAClientConfig() {
    }

    public static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            apply();
        }
    }

    public static void onReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            apply();
        }
    }

    private static void apply() {
        GlobalTextConfig.setAnimationsEnabled(ANIMATIONS_ENABLED.get());
    }
}
