package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.tysontheember.emberstextapi.accessor.ETAStyle;
import net.tysontheember.emberstextapi.immersivemessages.api.MarkupParser;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import net.tysontheember.emberstextapi.util.MarkupStripper;
import net.tysontheember.emberstextapi.util.StyleUtil;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(StringSplitter.class)
public abstract class StringSplitterMixin {

    @Shadow @Final
    private StringSplitter.WidthProvider widthProvider;

    @Inject(
            method = "stringWidth(Lnet/minecraft/util/FormattedCharSequence;)F",
            at = @At("HEAD"),
            cancellable = true
    )
    private void emberstextapi$adjustWidthForItems(FormattedCharSequence seq, CallbackInfoReturnable<Float> cir) {
        MutableFloat width = new MutableFloat();
        seq.accept((index, style, codePoint) -> {
            ETAStyle etaStyle = (ETAStyle) (Object) style;
            if (etaStyle.emberstextapi$getItemId() != null) {
                float offsetX = etaStyle.emberstextapi$getItemOffsetX() != null ? etaStyle.emberstextapi$getItemOffsetX() : -4.0f;
                width.add(offsetX + 16.0f);
            } else {
                width.add(this.widthProvider.getWidth(codePoint, style));
            }
            return true;
        });
        cir.setReturnValue(width.floatValue());
    }

    @Inject(
            method = "stringWidth(Lnet/minecraft/network/chat/FormattedText;)F",
            at = @At("HEAD"),
            cancellable = true
    )
    private void emberstextapi$markupAwareWidthFormattedText(FormattedText text, CallbackInfoReturnable<Float> cir) {
        Float w = emberstextapi$measureMarkup(text.getString());
        if (w != null) cir.setReturnValue(w);
    }

    @Inject(
            method = "stringWidth(Ljava/lang/String;)F",
            at = @At("HEAD"),
            cancellable = true
    )
    private void emberstextapi$markupAwareWidthString(String text, CallbackInfoReturnable<Float> cir) {
        Float w = emberstextapi$measureMarkup(text);
        if (w != null) cir.setReturnValue(w);
    }

    private Float emberstextapi$measureMarkup(String raw) {
        if (raw == null || raw.isEmpty() || !MarkupStripper.containsMarkup(raw)) return null;

        List<TextSpan> spans = MarkupParser.parse(raw);
        if (spans == null || spans.isEmpty()) return null;

        float total = 0f;
        for (TextSpan span : spans) {
            if (span.getItemId() != null || span.getEntityId() != null) {
                float offsetX = span.getItemOffsetX() != null ? span.getItemOffsetX() : -4.0f;
                total += offsetX + 16.0f;
                continue;
            }
            String content = span.getContent();
            if (content == null || content.isEmpty()) continue;

            Style spanStyle = StyleUtil.applyTextSpanFormatting(Style.EMPTY, span);
            for (int i = 0; i < content.length(); i++) {
                int cp = content.codePointAt(i);
                total += this.widthProvider.getWidth(cp, spanStyle);
                if (Character.charCount(cp) > 1) i++;
            }
        }
        return total;
    }
}
