package net.tysontheember.emberstextapi.mixin.client;

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
 * markup tags in the fallback translation string. When markup is found, it parses
 * the text into TextSpan objects with effects and attaches those effects to the
 * Style objects passed to the consumer.
 * </p>
 * <p>
 * Note: This implementation uses a simplified approach that only processes markup
 * in the fallback string. For proper translation key processing, additional work
 * would be needed to intercept the translation resolution process.
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

    @Unique
    private final long emberstextapi$obfInstanceId = java.util.concurrent.ThreadLocalRandom.current().nextLong();

    /**
     * Shadow the fallback field (the raw translation string when no translation is available).
     */
    @Shadow
    @Final
    private String fallback;

    /**
     * Intercept the visit method to detect markup in translatable text.
     * <p>
     * This simplified approach only processes markup in the fallback string.
     * For most use cases, this is sufficient since the fallback is typically
     * the translation key itself or a plain text alternative.
     * </p>
     * <p>
     * This method performs the same steps as LiteralContentsMixin:
     * <ol>
     *   <li>Quick check: If fallback doesn't contain angle brackets, skip markup detection</li>
     *   <li>Parse markup: Use MarkupParser to extract TextSpan objects with effects</li>
     *   <li>Process spans: For each span, clone the style, add effects, and emit characters</li>
     *   <li>Cancel original: Prevent vanilla from processing to avoid duplicate emission</li>
     * </ol>
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

        // Only process if we have a fallback string
        if (fallback == null) {
            return;
        }

        // Quick check: if fallback doesn't contain markup indicators, let vanilla handle it
        if (!fallback.contains("<") || !fallback.contains(">")) {
            return;
        }

        // Parse the markup into spans with effects
        List<TextSpan> spans = MarkupParser.parse(fallback);

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
            boolean hasItem = span.getItemId() != null;

            if (hasEffects || hasFormatting || hasFont || hasItem) {
                hasEffectsOrFormattingOrItems = true;
            }

            // Check for typewriter / obfuscate effects
            if (effects != null) {
                for (Effect effect : effects) {
                    if (effect instanceof TypewriterEffect) {
                        hasTypewriter = true;
                    }
                }
            }
        }
        if (!hasEffectsOrFormattingOrItems) {
            return; // Let vanilla handle plain text
        }

        // Get typewriter track if any span has typewriter effect
        // Use fallback.intern() as key so same text always gets same track
        TypewriterTrack track = null;
        if (hasTypewriter) {
            track = TypewriterTracks.getInstance().get(fallback.intern());

            // Calculate total character count for play completion detection
            int totalChars = 0;
            for (TextSpan s : spans) {
                String c = s.getContent();
                if (c != null && !c.isEmpty()) {
                    totalChars += c.length();
                } else if (s.getItemId() != null) {
                    totalChars += 1; // Space for item
                }
            }
            track.setTotalChars(totalChars);
        }

        // Track global character index for typewriter effect
        int globalCharIndex = 0;
        // Obfuscate base key stable per component (per instance) so tooltips persist
        Object baseObfKey = null;

        // Process each span with its effects
        for (int spanIdx = 0; spanIdx < spans.size(); spanIdx++) {
            TextSpan span = spans.get(spanIdx);
            String content = span.getContent();

            // For item spans with empty content, use a space so the item has something to render on
            boolean isItemSpan = span.getItemId() != null;
            if (content == null || content.isEmpty()) {
                if (isItemSpan) {
                    content = " "; // Emit a space for the item to replace
                } else {
                    continue; // Skip truly empty spans
                }
            }

            // Clone the style and apply this span's formatting and effects
            Style spanStyle = StyleUtil.applyTextSpanFormatting(style, span);

            // Emit each character with the modified style
            // For typewriter, each character needs its own Style with the correct index
            int spanStartIndex = globalCharIndex;
            int spanLength = content.length();

            for (int i = 0; i < content.length(); i++) {
                int codePoint = content.codePointAt(i);

                // For typewriter effect, clone the style and set the index for THIS character
                Style charStyle = spanStyle;
                if (track != null || hasObfuscate) {
                    // Clone the style so each character has its own per-char data.
                    // MC 1.21.1 optimizes withX() to return 'this' when value is unchanged,
                    // so we toggle bold to guarantee a new instance, then restore original value.
                    charStyle = emberstextapi$forceCloneStyle(spanStyle);
                    ETAStyle etaCharStyle = (ETAStyle) charStyle;
                    if (track != null) {
                        etaCharStyle.emberstextapi$setTypewriterTrack(track);
                        etaCharStyle.emberstextapi$setTypewriterIndex(globalCharIndex);
                    }
                    if (hasObfuscate) {
                        etaCharStyle.emberstextapi$setObfuscateKey(new ObfKey(baseObfKey, spanIdx));
                    }
                    etaCharStyle.emberstextapi$setObfuscateSpanStart(spanStartIndex);
                    etaCharStyle.emberstextapi$setObfuscateSpanLength(spanLength);
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
     * Force-clone a Style to guarantee a new instance.
     * MC 1.21.1 optimizes Style.withX() to return 'this' when the value doesn't change,
     * so withClickEvent(getClickEvent()) no longer creates a copy. We toggle bold to
     * guarantee two new instances (toggle + restore), then return the restored one.
     */
    @Unique
    private static Style emberstextapi$forceCloneStyle(Style original) {
        boolean origBold = original.isBold();
        return original.withBold(!origBold).withBold(origBold);
    }
}
