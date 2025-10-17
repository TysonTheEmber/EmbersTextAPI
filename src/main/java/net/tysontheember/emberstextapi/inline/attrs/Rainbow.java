package net.tysontheember.emberstextapi.inline.attrs;

import net.tysontheember.emberstextapi.inline.TagAttribute;

public record Rainbow(float speed) implements TagAttribute {
    public static Rainbow withSpeed(float speed) {
        return new Rainbow(speed);
    }
}
