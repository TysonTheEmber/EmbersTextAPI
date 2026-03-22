package net.tysontheember.emberstextapi.sdf;

import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds references to the MSDF text shader instances.
 * <p>
 * Shader registration is handled by platform-specific entry points:
 * <ul>
 *   <li>Forge 1.20.1 / NeoForge 1.21.1: {@code RegisterShadersEvent}</li>
 *   <li>Fabric: {@code CoreShaderRegistrationCallback}</li>
 * </ul>
 * The shaders are loaded from {@code assets/minecraft/shaders/core/rendertype_eta_sdf_text.*}
 * and use the MSDF median-of-three reconstruction kernel in the fragment stage.
 *
 * @see SDFRenderTypes
 */
public final class SDFShaders {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/SDFShaders");

    @Nullable
    private static ShaderInstance sdfTextShader;

    @Nullable
    private static ShaderInstance sdfTextSeeThroughShader;

    private SDFShaders() {}

    public static void setSdfTextShader(ShaderInstance shader) {
        sdfTextShader = shader;
        SDFRenderTypes.clearCache();
        LOGGER.info("SDF text shader registered");
    }

    public static void setSdfTextSeeThroughShader(ShaderInstance shader) {
        sdfTextSeeThroughShader = shader;
        SDFRenderTypes.clearCache();
        LOGGER.info("SDF text see-through shader registered");
    }

    @Nullable
    public static ShaderInstance getSdfTextShader() {
        return sdfTextShader;
    }

    @Nullable
    public static ShaderInstance getSdfTextSeeThroughShader() {
        return sdfTextSeeThroughShader;
    }

    public static boolean isLoaded() {
        return sdfTextShader != null && sdfTextSeeThroughShader != null;
    }
}
