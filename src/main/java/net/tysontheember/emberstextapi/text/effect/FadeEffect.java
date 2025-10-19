package net.tysontheember.emberstextapi.text.effect;

import net.minecraft.world.phys.Vec2;

import java.util.Objects;

/**
 * Modulates glyph alpha using a sinusoidal fade.
 */
public final class FadeEffect implements Effect {

    private final double speed;
    private final double frequency;
    private int ticks;

    private FadeEffect(Builder builder) {
        this.speed = builder.speed;
        this.frequency = builder.frequency;
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
        double angle = (ticks * speed) + index * frequency;
        float alpha = (float) ((Math.sin(angle) + 1d) * 0.5d);
        out.setColor(base.withAlpha(base.getAlpha() * alpha));
    }

    public double getSpeed() {
        return speed;
    }

    public double getFrequency() {
        return frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FadeEffect)) {
            return false;
        }
        FadeEffect that = (FadeEffect) o;
        return Double.compare(that.speed, speed) == 0
                && Double.compare(that.frequency, frequency) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(speed, frequency);
    }

    /**
     * Builder for {@link FadeEffect}.
     */
    public static final class Builder {

        private double speed = 0.5d;
        private double frequency = 0.2d;

        private Builder() {
        }

        public Builder speed(double speed) {
            this.speed = speed;
            return this;
        }

        public Builder frequency(double frequency) {
            this.frequency = frequency;
            return this;
        }

        public FadeEffect build() {
            return new FadeEffect(this);
        }
    }
}
