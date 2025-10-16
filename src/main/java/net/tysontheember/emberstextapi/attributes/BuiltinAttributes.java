package net.tysontheember.emberstextapi.attributes;

import net.tysontheember.emberstextapi.attributes.impl.BoldAttribute;
import net.tysontheember.emberstextapi.attributes.impl.BgAttribute;
import net.tysontheember.emberstextapi.attributes.impl.ColorAttribute;
import net.tysontheember.emberstextapi.attributes.impl.FadeAttribute;
import net.tysontheember.emberstextapi.attributes.impl.FontAttribute;
import net.tysontheember.emberstextapi.attributes.impl.GradientAttribute;
import net.tysontheember.emberstextapi.attributes.impl.ItalicAttribute;
import net.tysontheember.emberstextapi.attributes.impl.ObfuscatedAttribute;
import net.tysontheember.emberstextapi.attributes.impl.ShadowAttribute;
import net.tysontheember.emberstextapi.attributes.impl.ShakeAttribute;
import net.tysontheember.emberstextapi.attributes.impl.OutlineAttribute;
import net.tysontheember.emberstextapi.attributes.impl.StrikethroughAttribute;
import net.tysontheember.emberstextapi.attributes.impl.TypewriterAttribute;
import net.tysontheember.emberstextapi.attributes.impl.UnderlineAttribute;
import net.tysontheember.emberstextapi.attributes.impl.WaveAttribute;

/**
 * Registers all built-in attribute handlers.
 */
public final class BuiltinAttributes {
    private static boolean initialized;

    private BuiltinAttributes() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

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
        register(new ShadowAttribute());
        register(new OutlineAttribute());
        register(new BgAttribute());
    }

    private static void register(AttributeHandler handler) {
        TextAttributes.register(handler);
    }
}
