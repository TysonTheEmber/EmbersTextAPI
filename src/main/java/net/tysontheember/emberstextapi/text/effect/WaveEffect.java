package net.tysontheember.emberstextapi.text.effect;

import net.minecraft.world.phys.Vec2;

import java.util.Objects;

/**
 * Applies a sinusoidal vertical offset to glyphs.
 */
public final class WaveEffect implements Effect {

    private final double amplitude;
    private final double frequency;
    private final double wavelength;
    private int ticks;

    private WaveEffect(Builder builder) {
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
        float y = (float) (Math.sin(angle) * amplitude);
        out.offset(0f, y);
    }

    /**
     * Wave amplitude.
     *
     * @return amplitude
     */
    public double getAmplitude() {
        return amplitude;
    }

    /**
     * Oscillation frequency.
     *
     * @return frequency
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * Wavelength divisor used for phase spacing.
     *
     * @return wavelength value
     */
    public double getWavelength() {
        return wavelength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WaveEffect)) {
            return false;
        }
        WaveEffect that = (WaveEffect) o;
        return Double.compare(that.amplitude, amplitude) == 0
                && Double.compare(that.frequency, frequency) == 0
                && Double.compare(that.wavelength, wavelength) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amplitude, frequency, wavelength);
    }

    /**
     * Builder for {@link WaveEffect}.
     */
    public static final class Builder {

        private double amplitude = 1.0d;
        private double frequency = 0.2d;
        private double wavelength = 4.0d;

        private Builder() {
        }

        /**
         * Sets the wave amplitude.
         *
         * @param amplitude amplitude value
         * @return this builder for chaining
         */
        public Builder amplitude(double amplitude) {
            this.amplitude = amplitude;
            return this;
        }

        /**
         * Sets the oscillation frequency.
         *
         * @param frequency frequency value
         * @return this builder for chaining
         */
        public Builder frequency(double frequency) {
            this.frequency = frequency;
            return this;
        }

        /**
         * Sets the wavelength divisor.
         *
         * @param wavelength wavelength value
         * @return this builder for chaining
         */
        public Builder wavelength(double wavelength) {
            this.wavelength = wavelength;
            return this;
        }

        /**
         * Builds the effect instance.
         *
         * @return immutable effect
         */
        public WaveEffect build() {
            return new WaveEffect(this);
        }
    }
}
