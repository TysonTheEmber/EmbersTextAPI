package net.tysontheember.emberstextapi.client.text;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.duck.ETAStyle;

/**
 * Maintains per-track reveal accounting when typewriter gating is active.
 */
public final class TypewriterGate {
    private final long timestamp;
    private final Map<Object, TrackState> tracks = new IdentityHashMap<>();

    public TypewriterGate() {
        this.timestamp = EffectContext.nowNanos();
    }

    public boolean allow(Style style) {
        if (!(style instanceof ETAStyle duck)) {
            return true;
        }
        if (!TypewriterController.isActive(duck)) {
            return true;
        }

        Object key = resolveKey(duck);
        TrackState state = this.tracks.get(key);
        if (state == null) {
            int reveal = TypewriterController.revealCount(duck, this.timestamp);
            state = reveal >= Integer.MAX_VALUE ? TrackState.unbounded() : new TrackState(reveal);
            this.tracks.put(key, state);
        }

        if (state.unbounded) {
            return true;
        }

        int baseIndex = Math.max(0, duck.eta$getTypewriterIndex());
        int local = state.advanceFor(duck);
        int absoluteIndex = baseIndex + local;
        if (absoluteIndex >= state.revealCount) {
            return false;
        }
        state.commit(duck, local + 1);
        return true;
    }

    private static Object resolveKey(ETAStyle duck) {
        TypewriterTrack track = duck.eta$getTrack();
        if (track != null) {
            String trackId = track.trackId();
            if (trackId != null && !trackId.isEmpty()) {
                return trackId;
            }
            return track;
        }
        return duck;
    }

    private static final class TrackState {
        private final int revealCount;
        private final boolean unbounded;
        private final Map<ETAStyle, Integer> localProgress;

        private TrackState(int revealCount) {
            this(revealCount, false);
        }

        private TrackState(int revealCount, boolean unbounded) {
            this.revealCount = revealCount;
            this.unbounded = unbounded;
            this.localProgress = unbounded ? null : new IdentityHashMap<>();
        }

        private static TrackState unbounded() {
            return new TrackState(Integer.MAX_VALUE, true);
        }

        private int advanceFor(ETAStyle duck) {
            if (this.unbounded || this.localProgress == null) {
                return 0;
            }
            Integer value = this.localProgress.get(duck);
            return value != null ? value : 0;
        }

        private void commit(ETAStyle duck, int value) {
            if (this.unbounded || this.localProgress == null) {
                return;
            }
            this.localProgress.put(duck, value);
        }
    }
}
