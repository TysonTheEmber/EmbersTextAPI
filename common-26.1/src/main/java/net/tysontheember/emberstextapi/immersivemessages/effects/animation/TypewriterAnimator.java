package net.tysontheember.emberstextapi.immersivemessages.effects.animation;

import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;

import java.util.List;

public class TypewriterAnimator {

    public static int calculateTypewriterIndex(float age, float speed, int maxLength) {
        int next = (int) (age * speed);
        return Math.min(next, maxLength);
    }

    public static void updateIndependentSpanTypewriter(float age, List<TextSpan> spans, int[] spanTypewriterIndices) {
        for (int i = 0; i < spans.size(); i++) {
            TextSpan span = spans.get(i);
            if (span.getTypewriterSpeed() != null) {
                int spanNext = (int) (age * span.getTypewriterSpeed());
                spanTypewriterIndices[i] = Math.min(spanNext, span.getContent().length());
            } else {

                spanTypewriterIndices[i] = span.getContent().length();
            }
        }
    }

    public static void updateContainerTypewriter(int totalCharsToShow, List<TextSpan> spans, int[] spanTypewriterIndices) {
        int charsShown = 0;

        for (int i = 0; i < spans.size(); i++) {
            TextSpan span = spans.get(i);
            int spanLength = span.getContent().length();

            if (charsShown + spanLength <= totalCharsToShow) {

                spanTypewriterIndices[i] = spanLength;
                charsShown += spanLength;
            } else if (charsShown < totalCharsToShow) {

                spanTypewriterIndices[i] = totalCharsToShow - charsShown;
                charsShown = totalCharsToShow;
            } else {

                spanTypewriterIndices[i] = 0;
            }
        }
    }

    public static boolean hasIndependentTypewriter(List<TextSpan> spans) {
        return spans.stream().anyMatch(span -> span.getTypewriterSpeed() != null);
    }
}
