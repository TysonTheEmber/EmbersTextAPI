package net.tysontheember.emberstextapi.text;

import net.tysontheember.emberstextapi.text.effect.Effect;
import net.tysontheember.emberstextapi.text.effect.EffectParameterConverters;
import net.tysontheember.emberstextapi.text.effect.FadeEffect;
import net.tysontheember.emberstextapi.text.effect.GradientEffect;
import net.tysontheember.emberstextapi.text.effect.RainbowEffect;
import net.tysontheember.emberstextapi.text.effect.ShakeEffect;
import net.tysontheember.emberstextapi.text.effect.TypewriterEffect;
import net.tysontheember.emberstextapi.text.effect.WaveEffect;
import net.tysontheember.emberstextapi.text.effect.WiggleEffect;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link EffectParameterConverters}.
 */
class EffectParameterConvertersTest {

    @Test
    void convertsTypewriterParameters() {
        Effect effect = EffectParameterConverters.convert("typewriter", Map.of("sp", 2.5d)).orElseThrow();
        TypewriterEffect typewriter = assertInstanceOf(TypewriterEffect.class, effect);
        assertEquals(2.5d, typewriter.getSpeed());
    }

    @Test
    void convertsWaveParameters() {
        Effect effect = EffectParameterConverters.convert("wave", Map.of(
                "a", 3.0d,
                "f", 0.4d,
                "w", 7.5d
        )).orElseThrow();
        WaveEffect wave = assertInstanceOf(WaveEffect.class, effect);
        assertEquals(3.0d, wave.getAmplitude());
        assertEquals(0.4d, wave.getFrequency());
        assertEquals(7.5d, wave.getWavelength());
    }

    @Test
    void convertsWiggleParameters() {
        Effect effect = EffectParameterConverters.convert("wiggle", Map.of(
                "a", 1.2d,
                "f", 0.8d,
                "w", 5.0d
        )).orElseThrow();
        WiggleEffect wiggle = assertInstanceOf(WiggleEffect.class, effect);
        assertEquals(1.2d, wiggle.getAmplitude());
        assertEquals(0.8d, wiggle.getFrequency());
        assertEquals(5.0d, wiggle.getWavelength());
    }

    @Test
    void convertsShakeParameters() {
        Effect effect = EffectParameterConverters.convert("shake", Map.of(
                "a", 1.6d,
                "f", 0.75d
        )).orElseThrow();
        ShakeEffect shake = assertInstanceOf(ShakeEffect.class, effect);
        assertEquals(1.6d, shake.getAmplitude());
        assertEquals(0.75d, shake.getFrequency());
    }

    @Test
    void convertsGradientParameters() {
        Effect effect = EffectParameterConverters.convert("grad", Map.of(
                "from", "#FF0000",
                "to", "#00FF00",
                "hsv", true,
                "f", 0.25d,
                "sp", true,
                "uni", true
        )).orElseThrow();
        GradientEffect gradient = assertInstanceOf(GradientEffect.class, effect);
        assertEquals(Effect.Color.fromHex("#FF0000"), gradient.getFrom());
        assertEquals(Effect.Color.fromHex("#00FF00"), gradient.getTo());
        assertTrue(gradient.isHsv());
        assertEquals(0.25d, gradient.getFlow());
        assertTrue(gradient.isSpan());
        assertTrue(gradient.isUniform());
    }

    @Test
    void convertsRainbowParameters() {
        Effect effect = EffectParameterConverters.convert("rainb", Map.of(
                "hue", 180d,
                "f", 0.5d,
                "sp", 1.5d
        )).orElseThrow();
        RainbowEffect rainbow = assertInstanceOf(RainbowEffect.class, effect);
        assertEquals(180d, rainbow.getBaseHue());
        assertEquals(0.5d, rainbow.getFrequency());
        assertEquals(1.5d, rainbow.getSpeed());
    }

    @Test
    void convertsFadeParameters() {
        Effect effect = EffectParameterConverters.convert("fade", Map.of(
                "sp", 1.2d,
                "f", 0.9d
        )).orElseThrow();
        FadeEffect fade = assertInstanceOf(FadeEffect.class, effect);
        assertEquals(1.2d, fade.getSpeed());
        assertEquals(0.9d, fade.getFrequency());
    }
}
