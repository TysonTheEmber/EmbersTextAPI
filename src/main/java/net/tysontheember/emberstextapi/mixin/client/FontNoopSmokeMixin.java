package net.tysontheember.emberstextapi.mixin.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Font.class)
public class FontNoopSmokeMixin {
    private static final Logger ETA_LOGGER = LogUtils.getLogger();
    private static boolean eta$logged;

    @Inject(method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I", at = @At("HEAD"))
    private void emberstextapi$logOnce(FormattedCharSequence text, float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource bufferSource, DisplayMode displayMode, int backgroundColor, int packedLight, CallbackInfoReturnable<Integer> cir) {
        if (!eta$logged) {
            eta$logged = true;
            ETA_LOGGER.debug("[ETA] mixin smoke OK");
        }
    }
}
