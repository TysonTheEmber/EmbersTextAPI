package net.tysontheember.emberstextapi.inline.attrs;

import net.tysontheember.emberstextapi.inline.TagAttribute;

public record Wiggle(float amplitude, float frequency) implements TagAttribute {
    public static Wiggle of(float amplitude, float frequency) {
        return new Wiggle(amplitude, frequency);
    }
}
