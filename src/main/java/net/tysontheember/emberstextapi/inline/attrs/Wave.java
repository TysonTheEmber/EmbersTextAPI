package net.tysontheember.emberstextapi.inline.attrs;

import net.tysontheember.emberstextapi.inline.TagAttribute;

public record Wave(float amplitude, float frequency) implements TagAttribute {
    public static Wave of(float amplitude, float frequency) {
        return new Wave(amplitude, frequency);
    }

    public static Wave defaults() {
        return new Wave(1.0f, 1.0f);
    }
}
