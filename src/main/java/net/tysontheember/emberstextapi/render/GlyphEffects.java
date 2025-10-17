package net.tysontheember.emberstextapi.render;

import net.tysontheember.emberstextapi.inline.TagAttribute;

import java.util.List;

/**
 * Placeholder glyph effect calculator. Animations are currently handled elsewhere.
 */
public final class GlyphEffects {
    private GlyphEffects() {
    }

    public static boolean hasAnimatedEffects(List<TagAttribute> attributes) {
        for (TagAttribute attribute : attributes) {
            String name = attribute.getClass().getSimpleName();
            if (name.equals("Typewriter") || name.equals("Wave") || name.equals("Wiggle") || name.equals("Rainbow")) {
                return true;
            }
        }
        return false;
    }
}
