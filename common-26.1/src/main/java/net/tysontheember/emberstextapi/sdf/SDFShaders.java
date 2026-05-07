package net.tysontheember.emberstextapi.sdf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SDFShaders {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/SDFShaders");

    private static boolean loaded = false;

    private SDFShaders() {}

    public static void markLoaded() {
        loaded = true;
        SDFRenderTypes.clearCache();
        LOGGER.info("SDF text shaders marked as available");
    }

    public static boolean isLoaded() {

        return false;
    }
}
