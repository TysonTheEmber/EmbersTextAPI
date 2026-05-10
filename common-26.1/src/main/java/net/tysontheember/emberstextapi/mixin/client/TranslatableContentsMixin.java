package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.tysontheember.emberstextapi.accessor.ETAStyle;
import net.tysontheember.emberstextapi.immersivemessages.api.MarkupParser;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.TypewriterEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.TypewriterTrack;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.TypewriterTracks;
import net.tysontheember.emberstextapi.compat.patchouli.PatchouliBypass;
import net.tysontheember.emberstextapi.util.StyleUtil;
import net.tysontheember.emberstextapi.immersivemessages.effects.animation.ObfKey;
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

@Mixin(TranslatableContents.class)
public abstract class TranslatableContentsMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/TranslatableContentsMixin");

    @Unique
    private final long emberstextapi$obfInstanceId = java.util.concurrent.ThreadLocalRandom.current().nextLong();

    @Shadow
    @Final
    private String key;

    @Shadow
    @Final
    private String fallback;

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

        String resolved = emberstextapi$resolveTranslation();
        if (resolved == null || resolved.isEmpty()) {
            return;
        }

        if (!resolved.contains("<") || !resolved.contains(">")) {
            return;
        }

        List<TextSpan> spans = MarkupParser.parse(resolved);

        if (spans == null || spans.isEmpty()) {
            return;
        }

        boolean hasEffectsOrFormattingOrItems = false;
        boolean hasTypewriter = false;
        boolean hasObfuscate = false;
        for (TextSpan span : spans) {
            List<Effect> effects = span.getEffects();
            boolean hasEffects = effects != null && !effects.isEmpty();
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

            if (effects != null) {
                for (Effect effect : effects) {
                    if (effect instanceof TypewriterEffect) {
                        hasTypewriter = true;
                    }
                    if (effect instanceof net.tysontheember.emberstextapi.immersivemessages.effects.animation.ObfuscateEffect) {
                        hasObfuscate = true;
                    }
                }
            }
        }
        if (!hasEffectsOrFormattingOrItems && !MarkupParser.containsLangTag(resolved)) {
            return;
        }

        TypewriterTrack track = null;
        if (hasTypewriter) {
            track = TypewriterTracks.getInstance().get(resolved.intern());

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
        Object stableObfKey = hasObfuscate ? resolved.intern() : null;

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
                    ETAStyle etaCharStyle = (ETAStyle) (Object) charStyle;
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
    private String emberstextapi$resolveTranslation() {
        Language language = Language.getInstance();
        if (language == null) {
            return fallback;
        }

        String translated = language.getOrDefault(key);

        if (translated.equals(key)) {
            return fallback;
        }

        return translated;
    }

    @Unique
    private static Style emberstextapi$forceCloneStyle(Style original) {
        boolean origBold = original.isBold();
        return original.withBold(!origBold).withBold(origBold);
    }
}
