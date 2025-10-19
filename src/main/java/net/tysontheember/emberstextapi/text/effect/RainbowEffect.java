package net.tysontheember.emberstextapi.text.effect;

import net.minecraft.world.phys.Vec2;

import java.util.Objects;

/**
 * Cycles glyph colours through the HSV colour wheel.
 */
public final class RainbowEffect implements Effect {

    private final double baseHue;
    private final double frequency;
    private final double speed;
    private int ticks;

    private RainbowEffect(Builder builder) {
        this.baseHue = builder.baseHue;
        this.frequency = builder.frequency;
        this.speed = builder.speed;
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
        double hue = baseHue + (ticks * speed) + index * frequency;
        Color colour = Color.fromHsv((float) hue, 1.0f, 1.0f).withAlpha(base.getAlpha());
        out.setColor(colour);
    }

    public double getBaseHue() {
        return baseHue;
    }

    public double getFrequency() {
        return frequency;
    }

    public double getSpeed() {
        return speed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RainbowEffect)) {
            return false;
        }
        RainbowEffect that = (RainbowEffect) o;
        return Double.compare(that.baseHue, baseHue) == 0
                && Double.compare(that.frequency, frequency) == 0
                && Double.compare(that.speed, speed) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseHue, frequency, speed);
    }

    /**
     * Builder for {@link RainbowEffect}.
     */
    public static final class Builder {

        private double baseHue = 0d;
        private double frequency = 3d;
        private double speed = 0.5d;

        private Builder() {
        }

        public Builder baseHue(double baseHue) {
            this.baseHue = baseHue;
            return this;
        }

        public Builder frequency(double frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder speed(double speed) {
            this.speed = speed;
            return this;
        }

        public RainbowEffect build() {
            return new RainbowEffect(this);
        }
    }
}
