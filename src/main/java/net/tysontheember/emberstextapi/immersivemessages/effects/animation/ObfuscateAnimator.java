package net.tysontheember.emberstextapi.immersivemessages.effects.animation;

import net.tysontheember.emberstextapi.immersivemessages.api.ObfuscateMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Handles obfuscation/deobfuscation animation logic.
 * Manages progressive reveal of characters from an obfuscated state.
 */
public class ObfuscateAnimator {

    /**
     * Initializes the reveal order for obfuscation based on the specified mode.
     *
     * @param mode The obfuscation mode (LEFT, RIGHT, CENTER, RANDOM)
     * @param textLength The length of the text to obfuscate
     * @param random Random instance for RANDOM mode
     * @return A list of character indices in the order they should be revealed
     */
    public static List<Integer> createRevealOrder(ObfuscateMode mode, int textLength, Random random) {
        List<Integer> revealOrder = new ArrayList<>(textLength);
        for (int i = 0; i < textLength; i++) {
            revealOrder.add(i);
        }

        switch (mode) {
            case RIGHT -> Collections.reverse(revealOrder);
            case CENTER -> {
                revealOrder.clear();
                int left = (textLength - 1) / 2;
                int right = textLength / 2;
                while (left >= 0 || right < textLength) {
                    if (left >= 0) revealOrder.add(left--);
                    if (right < textLength) revealOrder.add(right++);
                }
            }
            case RANDOM -> Collections.shuffle(revealOrder, random);
            // LEFT is the default (no modification needed)
        }

        return revealOrder;
    }

    /**
     * Updates the reveal mask by revealing characters based on progress.
     *
     * @param revealMask The boolean array tracking which characters are revealed
     * @param revealOrder The order in which to reveal characters
     * @param revealIndex Current index in the reveal order (will be modified)
     * @param progress Current progress value (accumulates over time, will be modified)
     * @param delta Frame delta for this tick
     * @param speed Characters per tick reveal speed
     * @param typewriterEnabled Whether typewriter is active
     * @param typewriterIndex Current typewriter index (limits reveal)
     * @return The number of characters revealed this tick
     */
    public static int updateRevealMask(boolean[] revealMask, List<Integer> revealOrder,
                                        int[] revealIndexRef, float[] progressRef,
                                        float delta, float speed,
                                        boolean typewriterEnabled, int typewriterIndex) {

        if (revealOrder == null || revealIndexRef[0] >= revealOrder.size()) {
            return 0;
        }

        progressRef[0] += speed * delta;
        int revealCount = Math.min((int) progressRef[0], revealOrder.size() - revealIndexRef[0]);
        if (revealCount <= 0) {
            return 0;
        }

        int revealed = 0;
        for (int i = 0; i < revealCount; i++) {
            int idx = revealOrder.get(revealIndexRef[0]);
            if (typewriterEnabled && idx >= typewriterIndex) {
                break;
            }
            revealMask[idx] = true;
            revealIndexRef[0]++;
            revealed++;
        }

        if (revealed > 0) {
            progressRef[0] -= revealed;
        }

        return revealed;
    }

    /**
     * Data holder for obfuscation state.
     * Used to manage the mutable state needed for obfuscation animation.
     */
    public static class ObfuscateState {
        public boolean[] revealMask;
        public List<Integer> revealOrder;
        public int revealIndex;
        public float progress;

        public ObfuscateState(int textLength) {
            this.revealMask = new boolean[textLength];
            this.revealIndex = 0;
            this.progress = 0f;
        }
    }
}
