package net.tysontheember.emberstextapi.client.hook;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

/**
 * Ensures the mixin configuration is registered when the client environment starts.
 */
public final class MixinBootstrapper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile boolean initialised;

    private MixinBootstrapper() {
    }

    public static void init() {
        if (initialised || !FMLEnvironment.dist.isClient()) {
            return;
        }

        synchronized (MixinBootstrapper.class) {
            if (initialised) {
                return;
            }
            MixinBootstrap.init();
            try {
                Mixins.addConfiguration("mixins.emberstextapi.json");
                LOGGER.debug("Registered mixin config mixins.emberstextapi.json");
            } catch (IllegalArgumentException alreadyRegistered) {
                LOGGER.debug("Mixin config mixins.emberstextapi.json already registered");
            }
            initialised = true;
        }
    }
}
