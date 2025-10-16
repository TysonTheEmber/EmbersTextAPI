package net.tysontheember.emberstextapi.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue STRIP_INSERTION_IN_CHAT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("overlay");
        STRIP_INSERTION_IN_CHAT = builder
            .comment("If enabled, chat lines remove Style insertion payloads after overlay collection to keep copy/paste clean.")
            .define("stripInsertionInChat", true);
        builder.pop();
        SPEC = builder.build();
    }

    private ClientConfig() {
    }
}
