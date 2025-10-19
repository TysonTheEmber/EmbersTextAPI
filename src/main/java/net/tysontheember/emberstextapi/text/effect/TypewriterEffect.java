package net.tysontheember.emberstextapi.text.effect;

import net.minecraft.world.phys.Vec2;

import java.util.Objects;

/**
 * Reveals glyphs progressively over time, emulating a typewriter.
 */
public final class TypewriterEffect implements Effect {

    private final double speed;
    private int ticks;

    private TypewriterEffect(Builder builder) {
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
        double visible = Math.max(0d, ticks * speed);
        if (index > visible) {
            out.multiplyAlpha(0f);
        }
    }

    /**
     * Current reveal speed in glyphs per tick.
     *
     * @return speed value
     */
    public double getSpeed() {
        return speed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TypewriterEffect)) {
            return false;
        }
        TypewriterEffect that = (TypewriterEffect) o;
        return Double.compare(that.speed, speed) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(speed);
    }

    /**
     * Builder for {@link TypewriterEffect}.
     */
    public static final class Builder {

        private double speed = 1.0d;

        private Builder() {
        }

        /**
         * Sets the glyph reveal speed.
         *
         * @param speed glyphs per tick
         * @return this builder for chaining
         */
        public Builder speed(double speed) {
            this.speed = speed;
            return this;
        }

        /**
         * Builds the effect instance.
         *
         * @return immutable effect
         */
        public TypewriterEffect build() {
            return new TypewriterEffect(this);
        }
    }
}
