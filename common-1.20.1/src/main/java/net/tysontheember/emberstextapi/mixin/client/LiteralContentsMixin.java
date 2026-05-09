package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.tysontheember.emberstextapi.accessor.ETAStyle;
import net.tysontheember.emberstextapi.compat.patchouli.PatchouliBypass;
import net.tysontheember.emberstextapi.immersivemessages.api.MarkupParser;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.TypewriterEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.ObfKey;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.TypewriterTrack;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.TypewriterTracks;
import net.tysontheember.emberstextapi.util.StyleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(LiteralContents.class)
public abstract class LiteralContentsMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/LiteralContentsMixin");

    @Unique
    private final long emberstextapi$obfInstanceId = java.util.concurrent.ThreadLocalRandom.current().nextLong();

    @Shadow
    @Final
    private String text;

    @Inject(
            method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true)
    private <T> void emberstextapi$visit(
            FormattedText.StyledContentConsumer<T> consumer,
            Style style,
            CallbackInfoReturnable<Optional<T>> cir) {

        if (PatchouliBypass.active()) {
            return;
        }

        if (!text.contains("<") || !text.contains(">")) {
            return;
        }

        List<TextSpan> spans = MarkupParser.parse(text);

        if (LOGGER.isDebugEnabled() && text.contains("item")) {
            LOGGER.debug("Detected item markup: {}", text);
            LOGGER.debug("Parsed {} spans", spans != null ? spans.size() : 0);
            if (spans != null) {
                for (int i = 0; i < spans.size(); i++) {
                    TextSpan span = spans.get(i);
                    LOGGER.debug("  Span {}: content='{}', itemId={}", i, span.getContent(), span.getItemId());
                }
            }
        }

        if (spans == null || spans.isEmpty()) {
            return;
        }

        boolean hasEffectsOrFormattingOrItems = false;
        boolean hasTypewriter = false;
        boolean hasObfuscate = false;
        for (TextSpan span : spans) {
            boolean hasEffects = span.getEffects() != null && !span.getEffects().isEmpty();
            boolean hasFormatting = (span.getBold() != null && span.getBold()) ||
                                   (span.getItalic() != null && span.getItalic()) ||
                                   (span.getUnderline() != null && span.getUnderline()) ||
                                   (span.getStrikethrough() != null && span.getStrikethrough()) ||
                                   (span.getObfuscated() != null && span.getObfuscated());
            boolean hasFont = span.getFont() != null;
            boolean hasColor = span.getColor() != null;
            boolean hasItem = span.getItemId() != null;
            boolean hasEntity = span.getEntityId() != null;

            if (hasEffects || hasFormatting || hasFont || hasColor || hasItem || hasEntity) {
                hasEffectsOrFormattingOrItems = true;
            }

            if (hasEffects) {
                for (Effect effect : span.getEffects()) {
                    if (effect instanceof TypewriterEffect) {
                        hasTypewriter = true;
                    }
                    if (effect instanceof net.tysontheember.emberstextapi.immersivemessages.effects.animation.ObfuscateEffect) {
                        hasObfuscate = true;
                    }
                }
            }
        }
        if (!hasEffectsOrFormattingOrItems) {
            return;
        }

        TypewriterTrack track = null;
        if (hasTypewriter) {
            track = TypewriterTracks.getInstance().get(text.intern());

            int totalChars = 0;
            for (TextSpan s : spans) {
                String c = s.getContent();
                if (c != null && !c.isEmpty()) {
                    totalChars += c.length();
                } else if (s.getItemId() != null || s.getEntityId() != null) {
                    totalChars += 1;
                }
            }
            track.setTotalChars(totalChars);
        }

        int globalCharIndex = 0;

        Object baseObfKey = hasObfuscate ? this.emberstextapi$obfInstanceId : null;
        Object stableObfKey = hasObfuscate ? text.intern() : null;

        for (int spanIdx = 0; spanIdx < spans.size(); spanIdx++) {
            TextSpan span = spans.get(spanIdx);
            String content = span.getContent();

            boolean isItemSpan = span.getItemId() != null;
            boolean isEntitySpan = span.getEntityId() != null;
            if (content == null || content.isEmpty()) {
                if (isItemSpan || isEntitySpan) {
                    content = " ";
                } else {
                    continue;
                }
            }

            Style spanStyle = StyleUtil.applyTextSpanFormatting(style, span);
            int spanStartIndex = globalCharIndex;
            int spanLength = content.length();

            for (int i = 0; i < content.length(); i++) {
                int codePoint = content.codePointAt(i);

                Style charStyle = spanStyle;
                if (track != null || hasObfuscate) {

                    charStyle = emberstextapi$forceCloneStyle(spanStyle);
                    ETAStyle etaCharStyle = (ETAStyle) charStyle;
                    if (track != null) {
                        etaCharStyle.emberstextapi$setTypewriterTrack(track);
                        etaCharStyle.emberstextapi$setTypewriterIndex(globalCharIndex);
                    }
                    if (hasObfuscate) {
                        etaCharStyle.emberstextapi$setObfuscateKey(new ObfKey(baseObfKey, spanIdx));
                        etaCharStyle.emberstextapi$setObfuscateStableKey(new ObfKey(stableObfKey, spanIdx));
                        etaCharStyle.emberstextapi$setObfuscateSpanStart(spanStartIndex);
                        etaCharStyle.emberstextapi$setObfuscateSpanLength(spanLength);
                    }
                }

                Optional<T> result = consumer.accept(charStyle, Character.toString(codePoint));
                globalCharIndex++;

                if (result.isPresent()) {
                    cir.setReturnValue(result);
                    return;
                }

                if (Character.charCount(codePoint) > 1) {
                    i++;
                }
            }
        }

        cir.setReturnValue(Optional.empty());
    }

    @Unique
    private static Style emberstextapi$forceCloneStyle(Style original) {
        boolean origBold = original.isBold();
        return original.withBold(!origBold).withBold(origBold);
    }
}
