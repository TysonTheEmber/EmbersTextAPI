package net.tysontheember.emberstextapi.mixin.client;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.tysontheember.emberstextapi.duck.ETAStyle;
import net.tysontheember.emberstextapi.immersivemessages.api.MarkupParser;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.immersivemessages.effects.visual.TypewriterEffect;
import net.tysontheember.emberstextapi.typewriter.TypewriterTrack;
import net.tysontheember.emberstextapi.typewriter.TypewriterTracks;
import net.tysontheember.emberstextapi.util.StyleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

/**
 * Mixin for detecting inline markup in literal text components.
 * <p>
 * This mixin intercepts the visit() method of LiteralContents and checks for markup
 * tags like {@code <rainbow>}, {@code <wave>}, {@code <shake>}, etc. When markup is
 * detected, it parses the text into TextSpan objects with effects and attaches those
 * effects to the Style objects passed to the consumer.
 * </p>
 * <p>
 * This is Phase 3 of the Global Text Styling implementation plan. The actual rendering
 * of these effects will be handled in Phase 4.
 * </p>
 *
 * @see MarkupParser
 * @see TextSpan
 * @see StyleUtil
 */
@Mixin(LiteralContents.class)
public abstract class LiteralContentsMixin {

    private static final Logger LOGGER = LoggerFactory.getLogger("EmbersTextAPI/LiteralContentsMixin");

    /**
     * Shadow the text field to access the literal text content.
     */
    @Shadow
    @Final
    private String text;

    /**
     * Intercept the visit method to detect and parse markup in literal text.
     * <p>
     * This method performs the following steps:
     * <ol>
     *   <li>Quick check: If text doesn't contain angle brackets, skip markup detection</li>
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

        // Quick check: if text doesn't contain markup indicators, let vanilla handle it
        if (!text.contains("<") || !text.contains(">")) {
            return;
        }

        // Parse the markup into spans with effects
        List<TextSpan> spans = MarkupParser.parse(text);

        // Debug logging (only when debug level is enabled)
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

        // If no valid markup was found (empty list or parsing failed), let vanilla handle it
        if (spans == null || spans.isEmpty()) {
            return;
        }

        // Check if any span has effects, formatting, or item data - if not, let vanilla handle it
        boolean hasEffectsOrFormattingOrItems = false;
        boolean hasTypewriter = false;
        for (TextSpan span : spans) {
            boolean hasEffects = span.getEffects() != null && !span.getEffects().isEmpty();
            boolean hasFormatting = (span.getBold() != null && span.getBold()) ||
                                   (span.getItalic() != null && span.getItalic()) ||
                                   (span.getUnderline() != null && span.getUnderline()) ||
                                   (span.getStrikethrough() != null && span.getStrikethrough()) ||
                                   (span.getObfuscated() != null && span.getObfuscated());
            boolean hasItem = span.getItemId() != null;

            if (hasEffects || hasFormatting || hasItem) {
                hasEffectsOrFormattingOrItems = true;
            }

            // Check for typewriter effect
            if (hasEffects) {
                for (Effect effect : span.getEffects()) {
                    if (effect instanceof TypewriterEffect) {
                        hasTypewriter = true;
                        break;
                    }
                }
            }
        }
        if (!hasEffectsOrFormattingOrItems) {
            return; // Let vanilla handle plain text
        }

        // Get typewriter track if any span has typewriter effect
        // Use text.intern() as key so same text always gets same track
        // This prevents tooltip hover from resetting chat typewriters
        TypewriterTrack track = null;
        if (hasTypewriter) {
            track = TypewriterTracks.getInstance().get(text.intern());

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

        // Process each span with its effects
        for (TextSpan span : spans) {
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
            for (int i = 0; i < content.length(); i++) {
                int codePoint = content.codePointAt(i);

                // For typewriter effect, clone the style and set the index for THIS character
                Style charStyle = spanStyle;
                if (track != null) {
                    // Clone the style so each character has its own index
                    charStyle = spanStyle.withClickEvent(spanStyle.getClickEvent()); // Force clone
                    ETAStyle etaCharStyle = (ETAStyle) charStyle;
                    etaCharStyle.emberstextapi$setTypewriterTrack(track);
                    etaCharStyle.emberstextapi$setTypewriterIndex(globalCharIndex);
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
}
