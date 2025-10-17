package net.tysontheember.emberstextapi.text;

import net.minecraft.client.Minecraft;

/**
 * Provides environment info while compiling effects.
 */
public record CompileContext(Minecraft minecraft) {
    public static CompileContext ofClient() {
        return new CompileContext(Minecraft.getInstance());
    }
}
