package net.tysontheember.emberstextapi.immersivemessages.effects.animation;

import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;

import java.util.List;

/**
 * Utility class for ImmersiveMessage typewriter animation calculations.
 * <p>
 * This provides helper methods for the message-level typewriter feature
 * (when {@code typewriter=true} is set on an ImmersiveMessage). This is
 * separate from the {@code <typewriter>} effect tag which works through
 * the effect system.
 * </p>
 */
public class TypewriterAnimator {

    /**
     * Calculates the typewriter index (number of visible characters) for a simple text string.
     *
     * @param age Current message age in ticks
     * @param speed Characters per tick reveal speed
     * @param maxLength Maximum text length (prevents overflow)
     * @return The number of characters that should be visible
     */
    public static int calculateTypewriterIndex(float age, float speed, int maxLength) {
        int next = (int) (age * speed);
        return Math.min(next, maxLength);
    }

    /**
     * Updates per-span typewriter indices for independent span animations.
     * Each span with its own typewriter speed animates independently.
     *
     * @param age Current message age in ticks
     * @param spans List of text spans
     * @param spanTypewriterIndices Array to update with visible character counts per span
     */
    public static void updateIndependentSpanTypewriter(float age, List<TextSpan> spans, int[] spanTypewriterIndices) {
        for (int i = 0; i < spans.size(); i++) {
            TextSpan span = spans.get(i);
            if (span.getTypewriterSpeed() != null) {
                int spanNext = (int) (age * span.getTypewriterSpeed());
                spanTypewriterIndices[i] = Math.min(spanNext, span.getContent().length());
            } else {
                // Show all characters if no typewriter speed set
                spanTypewriterIndices[i] = span.getContent().length();
            }
        }
    }

    /**
     * Updates per-span typewriter indices for container-based animation.
     * Characters are revealed sequentially across all spans using a global speed.
     *
     * @param totalCharsToShow Total number of characters to show across all spans
     * @param spans List of text spans
     * @param spanTypewriterIndices Array to update with visible character counts per span
     */
    public static void updateContainerTypewriter(int totalCharsToShow, List<TextSpan> spans, int[] spanTypewriterIndices) {
        int charsShown = 0;

        for (int i = 0; i < spans.size(); i++) {
            TextSpan span = spans.get(i);
            int spanLength = span.getContent().length();

            if (charsShown + spanLength <= totalCharsToShow) {
                // Show entire span
                spanTypewriterIndices[i] = spanLength;
                charsShown += spanLength;
            } else if (charsShown < totalCharsToShow) {
                // Partially show this span
                spanTypewriterIndices[i] = totalCharsToShow - charsShown;
                charsShown = totalCharsToShow;
            } else {
                // Don't show this span yet
                spanTypewriterIndices[i] = 0;
            }
        }
    }

    /**
     * Determines if any span has an independent typewriter speed.
     *
     * @param spans List of text spans to check
     * @return true if at least one span has its own typewriter speed
     */
    public static boolean hasIndependentTypewriter(List<TextSpan> spans) {
        return spans.stream().anyMatch(span -> span.getTypewriterSpeed() != null);
    }
}
