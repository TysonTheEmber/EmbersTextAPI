package net.tysontheember.emberstextapi.mixin.client;

import java.util.List;
import java.util.Optional;

import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.tysontheember.emberstextapi.client.text.SpanStringSplitter;
import net.tysontheember.emberstextapi.client.text.SpanStyleExtras;
import net.tysontheember.emberstextapi.client.text.StringSplitterBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringSplitter.class)
public abstract class StringSplitterMixin implements StringSplitterBridge {
    @Unique
    private boolean emberstextapi$callThrough;

    @Override
    public float emberstextapi$callStringWidthOriginal(FormattedCharSequence sequence) {
        boolean prev = this.emberstextapi$callThrough;
        this.emberstextapi$callThrough = true;
        try {
            return ((StringSplitter) (Object) this).stringWidth(sequence);
        } finally {
            this.emberstextapi$callThrough = prev;
        }
    }

    @Override
    public String emberstextapi$callPlainSubstrOriginal(String text, int maxWidth, Style style) {
        boolean prev = this.emberstextapi$callThrough;
        this.emberstextapi$callThrough = true;
        try {
            return ((StringSplitter) (Object) this).plainHeadByWidth(text, maxWidth, style);
        } finally {
            this.emberstextapi$callThrough = prev;
        }
    }

    @Override
    public List<FormattedCharSequence> emberstextapi$callSplitLinesOriginal(FormattedText text, int maxWidth, Style baseStyle) {
        boolean prev = this.emberstextapi$callThrough;
        this.emberstextapi$callThrough = true;
        try {
            List<?> raw = ((StringSplitter) (Object) this).splitLines(text, maxWidth, baseStyle);
            @SuppressWarnings("unchecked")
            List<FormattedCharSequence> result = (List<FormattedCharSequence>) raw;
            return result;
        } finally {
            this.emberstextapi$callThrough = prev;
        }
    }

    @Inject(method = "plainHeadByWidth", at = @At("HEAD"), cancellable = true)
    private void emberstextapi$sanitizePlainSubstr(String value, int maxWidth, Style style, CallbackInfoReturnable<String> cir) {
        if (this.emberstextapi$callThrough || style == null) {
            return;
        }
        if (!(style instanceof SpanStyleExtras extras) || extras.eta$getSpanGraph() == null) {
            return;
        }
        String sanitized = SpanStringSplitter.sanitized(style, value);
        if (sanitized.equals(value)) {
            return;
        }
        String result = emberstextapi$callPlainSubstrOriginal(sanitized, maxWidth, style);
        cir.setReturnValue(result);
    }

    @Inject(method = "splitLines(Lnet/minecraft/network/chat/FormattedText;ILnet/minecraft/network/chat/Style;)Ljava/util/List;", at = @At("HEAD"), cancellable = true)
    private void emberstextapi$sanitizeSplitLines(FormattedText text, int maxWidth, Style baseStyle, CallbackInfoReturnable<List<FormattedCharSequence>> cir) {
        if (this.emberstextapi$callThrough || text == null || baseStyle == null) {
            return;
        }
        MutableComponent sanitized = Component.empty();
        boolean[] changed = new boolean[1];
        text.visit((style, string) -> {
            String sanitizedSegment = SpanStringSplitter.sanitized(style, string);
            if (!sanitizedSegment.equals(string)) {
                changed[0] = true;
            }
            sanitized.append(SpanStringSplitter.literal(sanitizedSegment, style));
            return Optional.empty();
        }, baseStyle);
        if (!changed[0]) {
            return;
        }
        List<FormattedCharSequence> result = emberstextapi$callSplitLinesOriginal(sanitized, maxWidth, baseStyle);
        cir.setReturnValue(result);
    }

    @Inject(method = "stringWidth(Lnet/minecraft/util/FormattedCharSequence;)F", at = @At("HEAD"), cancellable = true)
    private void emberstextapi$guardStringWidth(FormattedCharSequence sequence, CallbackInfoReturnable<Float> cir) {
        if (this.emberstextapi$callThrough) {
            return;
        }
        this.emberstextapi$callThrough = true;
        try {
            float value = ((StringSplitter) (Object) this).stringWidth(sequence);
            cir.setReturnValue(value);
        } finally {
            this.emberstextapi$callThrough = false;
        }
    }
}
