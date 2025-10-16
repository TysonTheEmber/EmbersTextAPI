package net.tysontheember.emberstextapi.attributes;

import net.tysontheember.emberstextapi.attributes.impl.BgAttribute;
import net.tysontheember.emberstextapi.attributes.impl.BoldAttribute;
import net.tysontheember.emberstextapi.attributes.impl.ColorAttribute;
import net.tysontheember.emberstextapi.attributes.impl.FadeAttribute;
import net.tysontheember.emberstextapi.attributes.impl.FontAttribute;
import net.tysontheember.emberstextapi.attributes.impl.GradientAttribute;
import net.tysontheember.emberstextapi.attributes.impl.ItalicAttribute;
import net.tysontheember.emberstextapi.attributes.impl.OutlineAttribute;
import net.tysontheember.emberstextapi.attributes.impl.ShakeAttribute;
import net.tysontheember.emberstextapi.attributes.impl.ShadowAttribute;
import net.tysontheember.emberstextapi.attributes.impl.StrikethroughAttribute;
import net.tysontheember.emberstextapi.attributes.impl.TypewriterAttribute;
import net.tysontheember.emberstextapi.attributes.impl.UnderlineAttribute;
import net.tysontheember.emberstextapi.attributes.impl.WaveAttribute;

/**
 * Helper that registers the built-in attribute handlers.
 */
public final class BuiltinAttributes {
    private static boolean INITIALISED = false;

    private BuiltinAttributes() {
    }

    public static synchronized void init() {
        if (INITIALISED) {
            return;
        }
        register(new ColorAttribute());
        register(new BoldAttribute());
        register(new ItalicAttribute());
        register(new UnderlineAttribute());
        register(new StrikethroughAttribute());
        register(new FontAttribute());
        register(new GradientAttribute());
        register(new WaveAttribute());
        register(new ShakeAttribute());
        register(new TypewriterAttribute());
        register(new FadeAttribute());
        register(new ShadowAttribute());
        register(new OutlineAttribute());
        register(new BgAttribute());
        INITIALISED = true;
    }

    private static void register(AttributeHandler handler) {
        TextAttributes.register(handler);
    }
}
