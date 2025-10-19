package net.tysontheember.emberstextapi.text.effect;

import net.minecraft.world.phys.Vec2;

import java.util.Objects;

/**
 * Applies a dynamic gradient between two colours.
 */
public final class GradientEffect implements Effect {

    private final Color from;
    private final Color to;
    private final boolean hsv;
    private final double flow;
    private final boolean span;
    private final boolean uniform;
    private int ticks;

    private GradientEffect(Builder builder) {
        this.from = builder.from;
        this.to = builder.to;
        this.hsv = builder.hsv;
        this.flow = builder.flow;
        this.span = builder.span;
        this.uniform = builder.uniform;
    }

    /**
     * Creates a new builder instance.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void beginFrame(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public void applyGlyph(int index, Vec2 basePos, Color base, Out out) {
        out.reset(basePos, base);
        Color start = from != null ? from : base;
        Color end = to != null ? to : base;
        if (start.equals(end)) {
            return;
        }
        double phase = ticks * flow;
        if (!span) {
            phase += index * flow;
        }
        float t = (float) ((Math.sin(phase) + 1d) * 0.5d);
        if (uniform) {
            t = (float) ((ticks * flow) % 1.0d);
        }
        Color colour = hsv ? start.lerpHsv(end, t) : start.lerp(end, t);
        out.setColor(colour);
    }

    public Color getFrom() {
        return from;
    }

    public Color getTo() {
        return to;
    }

    public boolean isHsv() {
        return hsv;
    }

    public double getFlow() {
        return flow;
    }

    public boolean isSpan() {
        return span;
    }

    public boolean isUniform() {
        return uniform;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GradientEffect)) {
            return false;
        }
        GradientEffect that = (GradientEffect) o;
        return hsv == that.hsv
                && Double.compare(that.flow, flow) == 0
                && span == that.span
                && uniform == that.uniform
                && Objects.equals(from, that.from)
                && Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, hsv, flow, span, uniform);
    }

    /**
     * Builder for {@link GradientEffect}.
     */
    public static final class Builder {

        private Color from;
        private Color to;
        private boolean hsv;
        private double flow;
        private boolean span;
        private boolean uniform;

        private Builder() {
        }

        public Builder from(Color from) {
            this.from = from;
            return this;
        }

        public Builder to(Color to) {
            this.to = to;
            return this;
        }

        public Builder hsv(boolean hsv) {
            this.hsv = hsv;
            return this;
        }

        public Builder flow(double flow) {
            this.flow = flow;
            return this;
        }

        public Builder span(boolean span) {
            this.span = span;
            return this;
        }

        public Builder uniform(boolean uniform) {
            this.uniform = uniform;
            return this;
        }

        public GradientEffect build() {
            return new GradientEffect(this);
        }
    }
}
