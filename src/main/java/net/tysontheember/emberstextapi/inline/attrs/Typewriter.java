package net.tysontheember.emberstextapi.inline.attrs;

import net.tysontheember.emberstextapi.inline.TagAttribute;

public record Typewriter(float speed, float delay, String cursor) implements TagAttribute {
    public static Typewriter of(float speed, float delay, String cursor) {
        return new Typewriter(speed, delay, cursor);
    }
}
