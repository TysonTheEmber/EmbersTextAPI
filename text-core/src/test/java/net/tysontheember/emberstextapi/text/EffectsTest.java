package net.tysontheember.emberstextapi.text;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EffectsTest {
    @Test
    void gradientInterpolatesBetweenColors() {
        AttributedText text = AttributedText.of("AB");
        Attribute attr = new Attribute(EmbersKey.of("embers", "grad"), Params.of(Map.of(
                "from", "#000000",
                "to", "#FFFFFF"
        )));
        Span span = new Span(0, text.length(), List.of(attr));
        TextEffect.EffectEnvironment env = new SimpleEnv(0xFFFFFFFF, 0f, 0L);
        TextEffect effect = Effects.compile(attr, new TextEffect.CompileContext(text, span, env, null));

        TextEffect.GlyphState first = new TextEffect.GlyphState(0xFFFFFFFF, false);
        effect.apply(new TextEffect.GlyphContext(0, 0, 0, 2, 'A', 0f, 0L, text, span), first);
        TextEffect.GlyphState second = new TextEffect.GlyphState(0xFFFFFFFF, false);
        effect.apply(new TextEffect.GlyphContext(1, 0, 1, 2, 'B', 0f, 0L, text, span), second);

        assertTrue(first.resolvedColor() != second.resolvedColor(), "Gradient should change colors across span");
    }

    @Test
    void typewriterHidesCharactersUntilSpeedAllows() {
        AttributedText text = AttributedText.of("Hello");
        Attribute attr = new Attribute(EmbersKey.of("embers", "typewriter"), Params.of(Map.of(
                "speed", 2
        )));
        Span span = new Span(0, text.length(), List.of(attr));
        TextEffect.EffectEnvironment env = new SimpleEnv(0xFFFFFFFF, 0f, 0L);
        TextEffect effect = Effects.compile(attr, new TextEffect.CompileContext(text, span, env, null));

        TextEffect.GlyphState first = new TextEffect.GlyphState(0xFFFFFFFF, false);
        effect.apply(new TextEffect.GlyphContext(0, 0, 0, 5, 'H', 0f, 0L, text, span), first);
        assertTrue(first.visible(), "First character should be visible immediately");

        TextEffect.GlyphState second = new TextEffect.GlyphState(0xFFFFFFFF, false);
        effect.apply(new TextEffect.GlyphContext(1, 0, 1, 5, 'e', 0f, 0L, text, span), second);
        assertFalse(second.visible(), "Second character should be hidden until enough time passes");

        TextEffect.GlyphState secondLater = new TextEffect.GlyphState(0xFFFFFFFF, false);
        effect.apply(new TextEffect.GlyphContext(1, 0, 1, 5, 'e', 1f, 0L, text, span), secondLater);
        assertTrue(secondLater.visible(), "Second character should appear after elapsed time");
    }

    @Test
    void shakeProducesDeterministicOffsets() {
        AttributedText text = AttributedText.of("ABCD");
        Attribute attr = new Attribute(EmbersKey.of("embers", "shake"), Params.of(Map.of(
                "a", 2f,
                "f", 1f
        )));
        Span span = new Span(0, text.length(), List.of(attr));
        TextEffect.EffectEnvironment env = new SimpleEnv(0xFFFFFFFF, 0f, 1234L);
        TextEffect effect = Effects.compile(attr, new TextEffect.CompileContext(text, span, env, null));

        TextEffect.GlyphState first = new TextEffect.GlyphState(0xFFFFFFFF, false);
        effect.apply(new TextEffect.GlyphContext(0, 0, 0, 4, 'A', 0.5f, 0L, text, span), first);
        TextEffect.GlyphState repeat = new TextEffect.GlyphState(0xFFFFFFFF, false);
        effect.apply(new TextEffect.GlyphContext(0, 0, 0, 4, 'A', 0.5f, 0L, text, span), repeat);

        assertEquals(first.offsetX(), repeat.offsetX(), 0.0001f);
        assertEquals(first.offsetY(), repeat.offsetY(), 0.0001f);
    }
    private record SimpleEnv(int baseColor, float animationStartTime, long seed) implements TextEffect.EffectEnvironment {
    }
}
