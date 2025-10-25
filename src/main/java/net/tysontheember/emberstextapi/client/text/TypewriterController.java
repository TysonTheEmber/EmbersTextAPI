package net.tysontheember.emberstextapi.client.text;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import net.tysontheember.emberstextapi.duck.ETAStyle;

/**
 * Computes how many glyphs a typewriter track should reveal at a given time.
 */
public final class TypewriterController {
    private static final Map<String, Long> TRACK_STARTS = new ConcurrentHashMap<>();
    private static final Map<Object, Long> ANONYMOUS_TRACK_STARTS = Collections.synchronizedMap(new WeakHashMap<>());

    private TypewriterController() {
    }

    public enum Mode {
        OFF,
        BY_CHAR,
        BY_WORD;
    }

    public static Mode resolve(@Nullable ETAStyle duck) {
        if (duck == null) {
            return Mode.OFF;
        }
        TypewriterTrack track = duck.eta$getTrack();
        if (track != null && track.isActive()) {
            return fromTrackMode(track.mode());
        }
        return Mode.OFF;
    }

    public static int revealCount(@Nullable ETAStyle duck, long timeNanosOrTicks) {
        if (!EffectContext.areAnimationsEnabled()) {
            return Integer.MAX_VALUE;
        }
        if (duck == null) {
            return Integer.MAX_VALUE;
        }

        TypewriterTrack track = duck.eta$getTrack();
        if (track == null || !track.isActive()) {
            return Integer.MAX_VALUE;
        }

        Mode mode = fromTrackMode(track.mode());
        if (mode == Mode.OFF) {
            return Integer.MAX_VALUE;
        }

        float effectiveSpeed = Math.max(0.0001f, track.speedMultiplier());

        long now = Math.max(0L, timeNanosOrTicks);
        long start = resolveStartTime(track, duck, now);
        long elapsed = Math.max(0L, now - start);
        double ticks = elapsed / 50_000_000.0; // 1 tick = 50ms
        long revealed = (long) Math.floor(ticks * effectiveSpeed);
        if (mode == Mode.BY_WORD) {
            // Placeholder: treat BY_WORD the same as BY_CHAR until word logic lands.
        }
        if (revealed > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.max(0L, revealed);
    }

    public static boolean isActive(@Nullable ETAStyle duck) {
        if (!EffectContext.areAnimationsEnabled()) {
            return false;
        }
        if (duck == null) {
            return false;
        }
        TypewriterTrack track = duck.eta$getTrack();
        return track != null && track.isActive() && resolve(duck) != Mode.OFF;
    }

    private static long resolveStartTime(@Nullable TypewriterTrack track, ETAStyle duck, long now) {
        if (track != null && track.isActive()) {
            String id = track.trackId();
            if (id != null && !id.isEmpty()) {
                return TRACK_STARTS.computeIfAbsent(id, key -> now);
            }
        }
        return anonymousStart(duck, now);
    }

    private static long anonymousStart(ETAStyle duck, long now) {
        Objects.requireNonNull(duck, "duck");
        synchronized (ANONYMOUS_TRACK_STARTS) {
            Long start = ANONYMOUS_TRACK_STARTS.get(duck);
            if (start == null) {
                start = now;
                ANONYMOUS_TRACK_STARTS.put(duck, start);
            }
            return start;
        }
    }

    private static Mode fromTrackMode(@Nullable TypewriterTrack.Mode trackMode) {
        if (trackMode == null) {
            return Mode.OFF;
        }
        return switch (trackMode) {
            case CHAR -> Mode.BY_CHAR;
            case WORD -> Mode.BY_WORD;
            case OFF -> Mode.OFF;
        };
    }
}
