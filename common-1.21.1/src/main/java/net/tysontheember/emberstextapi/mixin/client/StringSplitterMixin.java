package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.tysontheember.emberstextapi.accessor.ETAStyle;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ensures inline item components report an appropriate width during layout.
 * <p>
 * Our item tag renders a full 16px icon but vanilla width calculations only
 * see the placeholder space character and treat it as one glyph. That causes
 * tooltips and wrapped text to underestimate line width and overflow. We
 * mirror the x-advance used by {@link net.tysontheember.emberstextapi.mixin.client.StringRenderOutputMixin}
 * so that layout and rendering stay in sync.
 */
@Mixin(StringSplitter.class)
public abstract class StringSplitterMixin {

    @Shadow @Final
    private StringSplitter.WidthProvider widthProvider;

    /**
     * Make width calculations account for inline item icons produced by the <item> tag.
     * Vanilla only sees the placeholder space character and underestimates the width,
     * which lets tooltips/wrapping clip. We mirror the x‑advance used during rendering
     * so layout and render stay aligned.
     */
    @Inject(
            method = "stringWidth(Lnet/minecraft/util/FormattedCharSequence;)F",
            at = @At("HEAD"),
            cancellable = true
    )
    private void emberstextapi$adjustWidthForItems(FormattedCharSequence seq, CallbackInfoReturnable<Float> cir) {
        MutableFloat width = new MutableFloat();

        seq.accept((index, style, codePoint) -> {
            if (style instanceof ETAStyle etaStyle && etaStyle.emberstextapi$getItemId() != null) {
                float offsetX = etaStyle.emberstextapi$getItemOffsetX() != null ? etaStyle.emberstextapi$getItemOffsetX() : -4.0f;
                width.add(offsetX + 16.0f); // match render advance (offset + icon width)
            } else {
                width.add(this.widthProvider.getWidth(codePoint, style));
            }
            return true;
        });

        cir.setReturnValue(width.floatValue());
    }
}
