package net.tysontheember.emberstextapi.client.spans.effects;

import net.tysontheember.emberstextapi.client.spans.SpanAttr;
import net.tysontheember.emberstextapi.immersivemessages.api.ObfuscateMode;

import java.util.Arrays;
import java.util.Random;

/**
 * Handles reveal ordering for obfuscation effects.
 */
public final class ObfuscateEffect {
    private ObfuscateEffect() {
    }

    public static State create(SpanAttr.EffectSpec.Obfuscate spec, int length, long seed) {
        if (spec == null || length <= 0) {
            return null;
        }
        int[] order = buildOrder(length, spec.mode(), seed);
        int[] rank = new int[length];
        Arrays.fill(rank, length);
        for (int i = 0; i < order.length; i++) {
            int idx = order[i];
            if (idx >= 0 && idx < length) {
                rank[idx] = i;
            }
        }
        float speed = spec.speed() != null ? Math.max(0f, spec.speed()) : 1f;
        return new State(rank, speed);
    }

    public static boolean isRevealed(State state, float elapsedTicks, int offset) {
        if (state == null) {
            return true;
        }
        if (offset < 0 || offset >= state.rank.length) {
            return true;
        }
        int revealCount = (int) Math.floor(Math.max(0f, elapsedTicks) * state.speed);
        if (revealCount >= state.rank.length) {
            return true;
        }
        return state.rank[offset] < revealCount;
    }

    private static int[] buildOrder(int length, ObfuscateMode mode, long seed) {
        int[] order = new int[length];
        for (int i = 0; i < length; i++) {
            order[i] = i;
        }
        if (mode == null) {
            return order;
        }
        return switch (mode) {
            case LEFT -> order;
            case RIGHT -> {
                int[] reversed = new int[length];
                for (int i = 0; i < length; i++) {
                    reversed[i] = length - 1 - i;
                }
                yield reversed;
            }
            case CENTER -> buildCenterOrder(length);
            case RANDOM -> shuffle(order, seed);
            default -> order;
        };
    }

    private static int[] buildCenterOrder(int length) {
        int[] order = new int[length];
        int left = (length - 1) / 2;
        int right = length / 2;
        int index = 0;
        while (left >= 0 || right < length) {
            if (left >= 0) {
                order[index++] = left--;
            }
            if (right < length) {
                order[index++] = right++;
            }
        }
        return order;
    }

    private static int[] shuffle(int[] order, long seed) {
        Random random = new Random(seed);
        for (int i = order.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = order[i];
            order[i] = order[j];
            order[j] = tmp;
        }
        return order;
    }

    public record State(int[] rank, float speed) {
        public State {
            rank = rank != null ? Arrays.copyOf(rank, rank.length) : new int[0];
            speed = Math.max(0f, speed);
        }
    }
}
