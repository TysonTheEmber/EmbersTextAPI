package net.tysontheember.emberstextapi.core.render;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Provides access to the current render time in ticks and seconds. The values are safe to
 * query even when the client is on the title screen where no world is loaded.
 */
@OnlyIn(Dist.CLIENT)
public final class RenderTime {
    private RenderTime() {
    }

    /**
     * Returns the current client-side time in ticks including partial tick progress.
     */
    public static double getTicks() {
        Minecraft minecraft = Minecraft.getInstance();
        double partial = minecraft.getFrameTime();
        if (minecraft.level != null) {
            return minecraft.level.getGameTime() + partial;
        }
        // Fallback to GUI ticks when no level is present
        return minecraft.gui.getGuiTicks() + partial;
    }

    /**
     * Returns the current client-side time in seconds.
     */
    public static double getSeconds() {
        return getTicks() / 20.0d;
    }

    /**
     * Wraps a floating value into the [0, 1) range while handling negative inputs gracefully.
     */
    public static float wrap01(float value) {
        return Mth.frac(value);
    }
}
