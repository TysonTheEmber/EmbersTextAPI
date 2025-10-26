package net.tysontheember.emberstextapi.core.style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Describes a colour gradient applied across the glyphs of a span.
 */
public final class SpanGradient {
    private final List<Integer> colors;
    private final boolean repeating;
    private final float speed;
    private final float offset;

    public SpanGradient(List<Integer> colors, boolean repeating, float speed, float offset) {
        this.colors = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(colors, "colors")));
        this.repeating = repeating;
        this.speed = speed;
        this.offset = offset;
    }

    public List<Integer> colors() {
        return colors;
    }

    public boolean repeating() {
        return repeating;
    }

    public float speed() {
        return speed;
    }

    public float offset() {
        return offset;
    }

    public SpanGradient copy() {
        return new SpanGradient(colors, repeating, speed, offset);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SpanGradient other)) {
            return false;
        }
        return repeating == other.repeating
                && Float.compare(speed, other.speed) == 0
                && Float.compare(offset, other.offset) == 0
                && colors.equals(other.colors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(colors, repeating, speed, offset);
    }

    @Override
    public String toString() {
        return "SpanGradient{" +
                "colors=" + colors +
                ", repeating=" + repeating +
                ", speed=" + speed +
                ", offset=" + offset +
                '}';
    }
}
