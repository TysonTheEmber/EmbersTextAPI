package net.tysontheember.emberstextapi.mixin.common;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import net.tysontheember.emberstextapi.client.text.ETAOptions;
import net.tysontheember.emberstextapi.client.text.EffectContext;
import net.tysontheember.emberstextapi.client.text.GlobalTextConfig;
import net.tysontheember.emberstextapi.client.text.MarkupAdapter;
import net.tysontheember.emberstextapi.client.text.TypewriterController;
import net.tysontheember.emberstextapi.client.text.TypewriterTrack;
import net.tysontheember.emberstextapi.duck.ETAStyle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StringDecomposer.class)
public abstract class StringDecomposerMixin {
    @Unique
    private static final ThreadLocal<Boolean> emberstextapi$parsingMarkup = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Inject(method = "iterateFormatted(Ljava/lang/String;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z",
            at = @At("HEAD"), cancellable = true)
    private static void emberstextapi$injectMarkup(String text, Style style, FormattedCharSink sink,
            CallbackInfoReturnable<Boolean> cir) {
        if (Boolean.TRUE.equals(emberstextapi$parsingMarkup.get())) {
            return;
        }
        boolean markupEnabled = GlobalTextConfig.isMarkupEnabled();
        boolean hasMarkup = markupEnabled && MarkupAdapter.hasMarkup(text);
        boolean gatingEnabled = GlobalTextConfig.isTypewriterGatingEnabled();

        if (!hasMarkup && !gatingEnabled) {
            return;
        }

        ETAOptions options = GlobalTextConfig.getOptions();
        TypewriterGateContext gateContext = gatingEnabled ? new TypewriterGateContext(options) : null;
        FormattedCharSink effectiveSink = gateContext == null ? sink
                : (index, styled, codePoint) -> {
                    if (!gateContext.allow(styled)) {
                        return false;
                    }
                    return sink.accept(index, styled, codePoint);
                };

        emberstextapi$parsingMarkup.set(Boolean.TRUE);
        try {
            boolean handled;
            if (hasMarkup) {
                handled = MarkupAdapter.visitFormatted(text, style, effectiveSink);
            } else {
                handled = StringDecomposer.iterateFormatted(text, style, effectiveSink);
            }
            cir.setReturnValue(handled);
        } finally {
            emberstextapi$parsingMarkup.set(Boolean.FALSE);
        }
    }

    @Unique
    private static final class TypewriterGateContext {
        private final long timestamp;
        private final ETAOptions options;
        private final Map<Object, TrackState> tracks = new IdentityHashMap<>();

        private TypewriterGateContext(ETAOptions options) {
            this.timestamp = EffectContext.nowNanos();
            this.options = options;
        }

        private boolean allow(Style style) {
            if (!(style instanceof ETAStyle duck)) {
                return true;
            }
            if (!TypewriterController.isActive(duck, this.options)) {
                return true;
            }

            Object key = resolveKey(duck);
            TrackState state = this.tracks.get(key);
            if (state == null) {
                int reveal = TypewriterController.revealCount(duck, this.timestamp, this.options);
                if (reveal >= Integer.MAX_VALUE) {
                    state = TrackState.unbounded();
                } else {
                    state = new TrackState(reveal);
                }
                this.tracks.put(key, state);
            }

            if (state.unbounded) {
                return true;
            }

            int baseIndex = Math.max(0, duck.eta$getTypewriterIndex());
            int local = state.advanceFor(duck);
            int absoluteIndex = baseIndex + local;
            if (absoluteIndex >= state.revealCount) {
                return false;
            }
            state.commit(duck, local + 1);
            return true;
        }

        private static Object resolveKey(ETAStyle duck) {
            TypewriterTrack track = duck.eta$getTrack();
            if (track != null) {
                String trackId = track.trackId();
                if (trackId != null && !trackId.isEmpty()) {
                    return trackId;
                }
                return track;
            }
            return duck;
        }
    }

    @Unique
    private static final class TrackState {
        private final int revealCount;
        private final boolean unbounded;
        private final Map<ETAStyle, Integer> localProgress;

        private TrackState(int revealCount) {
            this(revealCount, false);
        }

        private TrackState(int revealCount, boolean unbounded) {
            this.revealCount = revealCount;
            this.unbounded = unbounded;
            this.localProgress = unbounded ? null : new IdentityHashMap<>();
        }

        private static TrackState unbounded() {
            return new TrackState(Integer.MAX_VALUE, true);
        }

        private int advanceFor(ETAStyle duck) {
            if (this.unbounded || this.localProgress == null) {
                return 0;
            }
            Integer value = this.localProgress.get(duck);
            return value != null ? value : 0;
        }

        private void commit(ETAStyle duck, int value) {
            if (this.unbounded || this.localProgress == null) {
                return;
            }
            this.localProgress.put(duck, value);
        }
    }
}
