package net.tysontheember.emberstextapi.core.style;

import java.util.Objects;

import net.tysontheember.emberstextapi.immersivemessages.api.ShakeType;

/**
 * Describes shake parameters that should be applied to glyphs.
 */
public final class ShakeState {
    private final ShakeType type;
    private final float amplitude;
    private final float speed;
    private final float wavelength;
    private final boolean perGlyph;

    public ShakeState(ShakeType type, float amplitude, float speed, float wavelength, boolean perGlyph) {
        this.type = Objects.requireNonNull(type, "type");
        this.amplitude = amplitude;
        this.speed = speed;
        this.wavelength = wavelength;
        this.perGlyph = perGlyph;
    }

    public ShakeType type() {
        return type;
    }

    public float amplitude() {
        return amplitude;
    }

    public float speed() {
        return speed;
    }

    public float wavelength() {
        return wavelength;
    }

    public boolean perGlyph() {
        return perGlyph;
    }

    public ShakeState copy(boolean perGlyphOverride) {
        return new ShakeState(type, amplitude, speed, wavelength, perGlyphOverride);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ShakeState that)) {
            return false;
        }
        return Float.compare(amplitude, that.amplitude) == 0
                && Float.compare(speed, that.speed) == 0
                && Float.compare(wavelength, that.wavelength) == 0
                && perGlyph == that.perGlyph
                && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, amplitude, speed, wavelength, perGlyph);
    }

    @Override
    public String toString() {
        return "ShakeState{" +
                "type=" + type +
                ", amplitude=" + amplitude +
                ", speed=" + speed +
                ", wavelength=" + wavelength +
                ", perGlyph=" + perGlyph +
                '}';
    }
}
