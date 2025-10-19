package net.tysontheember.emberstextapi.text;

import net.minecraft.nbt.CompoundTag;
import net.tysontheember.emberstextapi.text.effect.Effect;
import net.tysontheember.emberstextapi.text.effect.TypewriterEffect;
import net.tysontheember.emberstextapi.text.effect.WaveEffect;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link AttributedTextCodec} ensuring span data round-trips via NBT and JSON.
 */
class AttributedTextCodecTest {

    private AttributedText createSampleText() {
        AttributeSet.Gradient gradient = AttributeSet.Gradient.builder()
                .from("#FF0000")
                .to("#00FF00")
                .hsv(true)
                .flow(0.25d)
                .span(true)
                .uni(false)
                .build();
        AttributeSet.Style style = AttributeSet.Style.builder()
                .bold(true)
                .italic(true)
                .underlined(false)
                .strikethrough(true)
                .obfuscated(false)
                .build();
        AttributeSet.Background background = AttributeSet.Background.builder()
                .on(true)
                .color("#101010")
                .border("#202020")
                .alpha(0.75d)
                .build();
        AttributeSet.Builder attributes = AttributeSet.builder()
                .color("#FFFFFF")
                .gradient(gradient)
                .style(style)
                .background(background)
                .effect("typewriter", TypewriterEffect.builder().speed(2.0d).build())
                .effect("wave", WaveEffect.builder().amplitude(3.0d).frequency(0.5d).wavelength(6.0d).build())
                .effectParam("shadow", Boolean.TRUE)
                .effectParam("data", Map.of("strength", 3.5d));
        Span span = Span.builder()
                .start(0)
                .end(5)
                .attributes(attributes.build())
                .build();
        return AttributedText.builder()
                .text("Hello")
                .addSpan(span)
                .build();
    }

    @Test
    void roundTripsNbt() {
        AttributedText original = createSampleText();
        CompoundTag encoded = AttributedTextCodec.toNbt(original);
        AttributedText decoded = AttributedTextCodec.fromNbt(encoded);

        assertEquals("Hello", decoded.getText());
        assertEquals(1, decoded.getSpans().size());
        Span span = decoded.getSpans().get(0);
        assertEquals(0, span.getStart());
        assertEquals(5, span.getEnd());
        AttributeSet attributes = span.getAttributes();
        assertNotNull(attributes);
        assertEquals("#FFFFFF", attributes.getColor());
        AttributeSet.Gradient gradient = attributes.getGradient();
        assertNotNull(gradient);
        assertEquals("#FF0000", gradient.getFrom());
        assertEquals("#00FF00", gradient.getTo());
        assertTrue(gradient.isHsv());
        assertTrue(gradient.isSpan());
        assertEquals(0.25d, gradient.getFlow());
        AttributeSet.Style style = attributes.getStyle();
        assertTrue(style.isBold());
        assertTrue(style.isItalic());
        assertTrue(style.isStrikethrough());
        AttributeSet.Background background = attributes.getBackground();
        assertTrue(background.isOn());
        assertEquals("#101010", background.getColor());
        assertEquals("#202020", background.getBorder());
        assertEquals(0.75d, background.getAlpha());
        Map<String, Effect> effects = attributes.getEffects();
        assertTrue(effects.get("typewriter") instanceof TypewriterEffect);
        assertTrue(effects.get("wave") instanceof WaveEffect);
        TypewriterEffect typewriter = (TypewriterEffect) effects.get("typewriter");
        assertEquals(2.0d, typewriter.getSpeed());
        WaveEffect wave = (WaveEffect) effects.get("wave");
        assertEquals(3.0d, wave.getAmplitude());
        assertEquals(0.5d, wave.getFrequency());
        assertEquals(6.0d, wave.getWavelength());
        Map<String, Object> params = attributes.getEffectParams();
        assertEquals(Boolean.TRUE, params.get("shadow"));
        assertTrue(params.get("data") instanceof Map);
        Map<?, ?> nested = (Map<?, ?>) params.get("data");
        assertEquals(3.5d, ((Number) nested.get("strength")).doubleValue());
    }

    @Test
    void roundTripsJson() {
        AttributedText original = createSampleText();
        AttributedText decoded = AttributedTextCodec.fromJson(AttributedTextCodec.toJson(original));

        assertEquals("Hello", decoded.getText());
        assertEquals(1, decoded.getSpans().size());
        Span span = decoded.getSpans().get(0);
        AttributeSet attributes = span.getAttributes();
        assertNotNull(attributes.getGradient());
        assertEquals("#FFFFFF", attributes.getColor());
        assertTrue(attributes.getStyle().isBold());
        assertTrue(attributes.getBackground().isOn());
        assertTrue(attributes.getEffects().containsKey("wave"));
        assertTrue(attributes.getEffectParams().containsKey("shadow"));
        Map<?, ?> nested = (Map<?, ?>) attributes.getEffectParams().get("data");
        assertEquals(3.5d, ((Number) nested.get("strength")).doubleValue());
    }
}
