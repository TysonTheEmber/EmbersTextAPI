package net.tysontheember.emberstextapi.inline.attrs;

import net.tysontheember.emberstextapi.inline.TagAttribute;

public record Gradient(int from, int to) implements TagAttribute {
    public static Gradient of(int from, int to) {
        return new Gradient(from, to);
    }
}
