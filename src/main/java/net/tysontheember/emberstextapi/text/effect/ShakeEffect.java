package net.tysontheember.emberstextapi.text.effect;

import net.minecraft.world.phys.Vec2;

import java.util.Objects;

/**
 * Adds a jittering shake motion to glyph positions.
 */
public final class ShakeEffect implements Effect {

    private final double amplitude;
    private final double frequency;
    private int ticks;

    private ShakeEffect(Builder builder) {
        this.amplitude = builder.amplitude;
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
        double angle = (ticks + index) * frequency;
        float x = (float) (Math.sin(angle * 13.0d) * amplitude);
        float y = (float) (Math.cos(angle * 17.0d) * amplitude);
        out.offset(x, y);
    }

    public double getAmplitude() {
        return amplitude;
    }

    public double getFrequency() {
        return frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShakeEffect)) {
            return false;
        }
        ShakeEffect that = (ShakeEffect) o;
        return Double.compare(that.amplitude, amplitude) == 0
                && Double.compare(that.frequency, frequency) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amplitude, frequency);
    }

    /**
     * Builder for {@link ShakeEffect}.
     */
    public static final class Builder {

        private double amplitude = 0.75d;
        private double frequency = 0.35d;

        private Builder() {
        }

        public Builder amplitude(double amplitude) {
            this.amplitude = amplitude;
            return this;
        }

        public Builder frequency(double frequency) {
            this.frequency = frequency;
            return this;
        }

        public ShakeEffect build() {
            return new ShakeEffect(this);
        }
    }
}
