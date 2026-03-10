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
import net.tysontheember.emberstextapi.typewriter.TypewriterTrack;
import net.tysontheember.emberstextapi.typewriter.TypewriterTracks;
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

/**
 * Mixin for detecting inline markup in translatable text components.
 * <p>
 * This mixin intercepts the visit() method of TranslatableContents to detect
 * markup tags in resolved translation values. When markup is found, it parses
 * the text into TextSpan objects with effects and attaches those effects to the
 * Style objects passed to the consumer.
 * </p>
 * <p>
 * The mixin resolves the translation key via {@link Language#getInstance()} to
 * obtain the actual translated string, then checks it for markup. This allows
 * markup tags like {@code <rainbow>} to be used directly in lang file values
 * (e.g. {@code "item.mymod.sword": "<rainbow>Epic Sword</rainbow>"}).
 * </p>
 * <p>
 * This is Phase 3 of the Global Text Styling implementation plan.
 * </p>
 *
 * @see MarkupParser
 * @see TextSpan
 * @see StyleUtil
 */
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

    /**
     * Intercept the visit method to detect markup in translatable text.
     * <p>
     * Resolves the translation key to get the actual translated string, then
     * checks it for markup tags. Falls back to the fallback string if present.
     * </p>
     *
     * @param consumer The consumer that accepts styled character data
     * @param style The base style to apply
     * @param cir Callback info for returning the result
     * @param <T> The type parameter for the consumer
     */
    @Inject(
            method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
            at = @At("HEAD"),
            cancellable = true)
    private <T> void emberstextapi$visit(
            FormattedText.StyledContentConsumer<T> consumer,
            Style style,
            CallbackInfoReturnable<Optional<T>> cir) {

        // Resolve the translated string from the language system
        String resolved = emberstextapi$resolveTranslation();
        if (resolved == null || resolved.isEmpty()) {
            return;
        }

        // Quick check: if resolved text doesn't contain markup indicators, let vanilla handle it
        if (!resolved.contains("<") || !resolved.contains(">")) {
            return;
        }

        LOGGER.debug("[DIAG] TranslatableContentsMixin.visit() HIT — key: {}, resolved: {}", key,
                resolved.length() > 120 ? resolved.substring(0, 120) + "..." : resolved);

        // Parse the markup into spans with effects
        List<TextSpan> spans = MarkupParser.parse(resolved);

        // If no valid markup was found, let vanilla handle it
        if (spans == null || spans.isEmpty()) {
            return;
        }

        // Check if any span has effects, formatting, or item data - if not, let vanilla handle it
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
            boolean hasGlobal = span.hasGlobalAttributes();

            if (hasEffects || hasFormatting || hasFont || hasColor || hasItem || hasEntity || hasGlobal) {
                hasEffectsOrFormattingOrItems = true;
            }

            // Check for typewriter / obfuscate effects
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
        if (!hasEffectsOrFormattingOrItems) {
            return; // Let vanilla handle plain text
        }

        LOGGER.debug("[DIAG] hasTypewriter={}, hasObfuscate={}, hasEffectsOrFormatting={}", hasTypewriter, hasObfuscate, hasEffectsOrFormattingOrItems);

        // Get typewriter track if any span has typewriter effect
        // Use resolved.intern() as key so same text always gets same track
        TypewriterTrack track = null;
        if (hasTypewriter) {
            track = TypewriterTracks.getInstance().get(resolved.intern());

            // Calculate total character count for play completion detection
            int totalChars = 0;
            for (TextSpan s : spans) {
                String c = s.getContent();
                if (c != null && !c.isEmpty()) {
                    totalChars += c.length();
                } else if (s.getItemId() != null || s.getEntityId() != null) {
                    totalChars += 1; // Space for item/entity
                }
            }
            track.setTotalChars(totalChars);
        }

        // Track global character index for typewriter effect
        int globalCharIndex = 0;
        // Obfuscate keys: per-component stable id to persist across frames, per span index
        Object baseObfKey = hasObfuscate ? this.emberstextapi$obfInstanceId : null;
        Object stableObfKey = hasObfuscate ? resolved.intern() : null;

        // Process each span with its effects
        for (int spanIdx = 0; spanIdx < spans.size(); spanIdx++) {
            TextSpan span = spans.get(spanIdx);
            String content = span.getContent();

            // For item/entity spans with empty content, use a space so they have something to render on
            boolean isItemSpan = span.getItemId() != null;
            boolean isEntitySpan = span.getEntityId() != null;
            if (content == null || content.isEmpty()) {
                if (isItemSpan || isEntitySpan) {
                    content = " "; // Emit a space for the item/entity to replace
                } else {
                    continue; // Skip truly empty spans
                }
            }

            // Clone the style and apply this span's formatting and effects
            Style spanStyle = StyleUtil.applyTextSpanFormatting(style, span);

            // Emit each character with the modified style
            int spanStartIndex = globalCharIndex;
            int spanLength = content.length();

            for (int i = 0; i < content.length(); i++) {
                int codePoint = content.codePointAt(i);

                // For typewriter/obfuscate, clone the style and set per-character data
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

                // Accept the character with the styled content consumer
                Optional<T> result = consumer.accept(charStyle, Character.toString(codePoint));

                // Increment global char index for typewriter
                globalCharIndex++;

                // If the consumer returns a non-empty result, it wants to stop processing
                if (result.isPresent()) {
                    cir.setReturnValue(result);
                    return;
                }

                // Handle surrogate pairs (characters outside the BMP)
                if (Character.charCount(codePoint) > 1) {
                    i++; // Skip the low surrogate
                }
            }
        }

        // We've processed all characters, return empty to indicate completion
        cir.setReturnValue(Optional.empty());
    }

    /**
     * Resolve the translation key to get the actual translated string.
     * Uses the current language to look up the key, falling back to the
     * fallback string if the key is not found in the language file.
     *
     * @return the resolved translation string, or null if unavailable
     */
    @Unique
    private String emberstextapi$resolveTranslation() {
        Language language = Language.getInstance();
        if (language == null) {
            return fallback;
        }

        // Try to get the translated value; if not found, Language returns the key itself
        String translated = language.getOrDefault(key);

        // If Language returned the key back (meaning no translation found), use fallback
        if (translated.equals(key)) {
            return fallback;
        }

        return translated;
    }

    /**
     * Force-clone a Style to guarantee a new instance.
     * MC 1.21.1 optimizes Style.withX() to return 'this' when the value doesn't change,
     * so withClickEvent(getClickEvent()) no longer creates a copy. We toggle bold to
     * guarantee two new instances (toggle + restore), then return the restored one.
     * StyleMixin propagation hooks ensure effects and typewriter data are copied through.
     */
    @Unique
    private static Style emberstextapi$forceCloneStyle(Style original) {
        boolean origBold = original.isBold();
        return original.withBold(!origBold).withBold(origBold);
    }
}
