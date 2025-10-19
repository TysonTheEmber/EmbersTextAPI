package net.tysontheember.emberstextapi.text.effect;

import net.minecraft.world.phys.Vec2;

import java.util.Objects;

/**
 * Produces a two-dimensional wiggle motion.
 */
public final class WiggleEffect implements Effect {

    private final double amplitude;
    private final double frequency;
    private final double wavelength;
    private int ticks;

    private WiggleEffect(Builder builder) {
        this.amplitude = builder.amplitude;
        this.frequency = builder.frequency;
        this.wavelength = builder.wavelength;
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
        double angle = (index / Math.max(1d, wavelength)) + ticks * frequency;
        float x = (float) (Math.sin(angle) * amplitude);
        float y = (float) (Math.cos(angle) * amplitude);
        out.offset(x, y);
    }

    public double getAmplitude() {
        return amplitude;
    }

    public double getFrequency() {
        return frequency;
    }

    public double getWavelength() {
        return wavelength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WiggleEffect)) {
            return false;
        }
        WiggleEffect that = (WiggleEffect) o;
        return Double.compare(that.amplitude, amplitude) == 0
                && Double.compare(that.frequency, frequency) == 0
                && Double.compare(that.wavelength, wavelength) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amplitude, frequency, wavelength);
    }

    /**
     * Builder for {@link WiggleEffect}.
     */
    public static final class Builder {

        private double amplitude = 1.0d;
        private double frequency = 0.25d;
        private double wavelength = 4.0d;

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

        public Builder wavelength(double wavelength) {
            this.wavelength = wavelength;
            return this;
        }

        public WiggleEffect build() {
            return new WiggleEffect(this);
        }
    }
}
