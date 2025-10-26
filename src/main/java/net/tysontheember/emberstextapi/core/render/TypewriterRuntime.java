package net.tysontheember.emberstextapi.core.render;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.tysontheember.emberstextapi.core.style.TypewriterState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Tracks the reveal progress for active typewriter tracks.
 */
@OnlyIn(Dist.CLIENT)
public final class TypewriterRuntime {
    private static final Map<Integer, TrackState> TRACKS = new ConcurrentHashMap<>();

    private TypewriterRuntime() {
    }

    public static boolean isGlyphVisible(TypewriterState state) {
        if (state == null) {
            return true;
        }
        float speed = Math.max(0.0f, state.speed());
        if (speed <= 0.0f) {
            return true;
        }

        TrackState track = TRACKS.computeIfAbsent(state.track(), key -> TrackState.start(RenderTime.getTicks(), speed));
        double now = RenderTime.getTicks();
        track.update(now, speed, state.index());
        double visible = track.visibleCharacters(now);
        // Characters are zero-indexed; require full character budget to show the glyph.
        return visible + 1e-4d >= state.index() + 1;
    }

    private static final class TrackState {
        private double startTick;
        private float speed;
        private int lastIndex;

        private TrackState(double startTick, float speed) {
            this.startTick = startTick;
            this.speed = speed;
            this.lastIndex = -1;
        }

        static TrackState start(double now, float speed) {
            return new TrackState(now, speed);
        }

        void update(double now, float newSpeed, int index) {
            if (index == 0 && lastIndex > 0) {
                // Restart when the sequence loops back to the first glyph.
                startTick = now;
            }
            if (newSpeed != speed) {
                double visible = visibleCharacters(now);
                speed = newSpeed;
                if (speed > 0.0f) {
                    startTick = now - visible / speed;
                }
            }
            speed = newSpeed;
            lastIndex = Math.max(lastIndex, index);
        }

        double visibleCharacters(double now) {
            double elapsed = Math.max(0.0d, now - startTick);
            return elapsed * speed;
        }
    }
}
