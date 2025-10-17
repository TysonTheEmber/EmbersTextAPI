package net.tysontheember.emberstextapi.inline.attrs;

import net.tysontheember.emberstextapi.inline.TagAttribute;

public record Color(int argb) implements TagAttribute {
    public static Color of(int argb) {
        return new Color(argb);
    }
}
