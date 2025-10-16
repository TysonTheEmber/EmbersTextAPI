package net.tysontheember.emberstextapi.attributes;

import net.tysontheember.emberstextapi.attributes.impl.BoldAttribute;
import net.tysontheember.emberstextapi.attributes.impl.ColorAttribute;
import net.tysontheember.emberstextapi.attributes.impl.FadeAttribute;
import net.tysontheember.emberstextapi.attributes.impl.FontAttribute;
import net.tysontheember.emberstextapi.attributes.impl.GradientAttribute;
import net.tysontheember.emberstextapi.attributes.impl.ItalicAttribute;
import net.tysontheember.emberstextapi.attributes.impl.ObfuscatedAttribute;
import net.tysontheember.emberstextapi.attributes.impl.ShakeAttribute;
import net.tysontheember.emberstextapi.attributes.impl.StrikethroughAttribute;
import net.tysontheember.emberstextapi.attributes.impl.TypewriterAttribute;
import net.tysontheember.emberstextapi.attributes.impl.UnderlineAttribute;
import net.tysontheember.emberstextapi.attributes.impl.WaveAttribute;

/**
 * Registers the built-in attribute handlers.
 */
public final class BuiltinAttributes {
    private BuiltinAttributes() {
    }

    public static void init() {
        register(new ColorAttribute());
        register(new BoldAttribute());
        register(new ItalicAttribute());
        register(new UnderlineAttribute());
        register(new StrikethroughAttribute());
        register(new ObfuscatedAttribute());
        register(new FontAttribute());
        register(new GradientAttribute());
        register(new WaveAttribute());
        register(new ShakeAttribute());
        register(new TypewriterAttribute());
        register(new FadeAttribute());
    }

    private static void register(AttributeHandler handler) {
        TextAttributes.register(handler);
    }
}
