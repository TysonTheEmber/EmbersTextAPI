package net.tysontheember.emberstextapi.immersivemessages.effects.animation;

import net.tysontheember.emberstextapi.immersivemessages.api.ObfuscateMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ObfuscateAnimator {

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
            case EDGES -> {
                revealOrder.clear();
                int left = 0;
                int right = textLength - 1;
                while (left <= right) {
                    if (left <= right) revealOrder.add(left++);
                    if (left <= right) revealOrder.add(right--);
                }
            }
            case RANDOM -> Collections.shuffle(revealOrder, random);

        }

        return revealOrder;
    }

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
