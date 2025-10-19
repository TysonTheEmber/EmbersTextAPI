package net.tysontheember.emberstextapi.text.effect;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Converts tag parameter maps into configured {@link Effect} instances.
 */
public final class EffectParameterConverters {

    private static final Map<String, Function<Map<String, Object>, Effect>> CONVERTERS;

    static {
        Map<String, Function<Map<String, Object>, Effect>> converters = new HashMap<>();
        converters.put("typewriter", EffectParameterConverters::createTypewriter);
        converters.put("wave", EffectParameterConverters::createWave);
        converters.put("wiggle", EffectParameterConverters::createWiggle);
        converters.put("shake", EffectParameterConverters::createShake);
        converters.put("grad", EffectParameterConverters::createGradient);
        converters.put("rainb", EffectParameterConverters::createRainbow);
        converters.put("fade", EffectParameterConverters::createFade);
        CONVERTERS = Collections.unmodifiableMap(converters);
    }

    private EffectParameterConverters() {
    }

    /**
     * Converts the supplied tag parameters into an {@link Effect} instance when supported.
     *
     * @param name   effect tag name
     * @param params parameter map
     * @return optional effect instance
     */
    public static Optional<Effect> convert(String name, Map<String, Object> params) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(params, "params");
        Function<Map<String, Object>, Effect> converter = CONVERTERS.get(name.toLowerCase(Locale.ROOT));
        if (converter == null) {
            return Optional.empty();
        }
        return Optional.of(converter.apply(params));
    }

    private static Effect createTypewriter(Map<String, Object> params) {
        TypewriterEffect defaults = TypewriterEffect.builder().build();
        TypewriterEffect.Builder builder = TypewriterEffect.builder();
        builder.speed(number(params, "sp", number(params, "speed", defaults.getSpeed())));
        return builder.build();
    }

    private static Effect createWave(Map<String, Object> params) {
        WaveEffect defaults = WaveEffect.builder().build();
        WaveEffect.Builder builder = WaveEffect.builder();
        builder.amplitude(number(params, "a", defaults.getAmplitude()));
        builder.frequency(number(params, "f", defaults.getFrequency()));
        builder.wavelength(number(params, "w", defaults.getWavelength()));
        return builder.build();
    }

    private static Effect createWiggle(Map<String, Object> params) {
        WiggleEffect.Builder builder = WiggleEffect.builder();
        WiggleEffect defaults = builder.build();
        builder.amplitude(number(params, "a", defaults.getAmplitude()));
        builder.frequency(number(params, "f", defaults.getFrequency()));
        builder.wavelength(number(params, "w", defaults.getWavelength()));
        return builder.build();
    }

    private static Effect createShake(Map<String, Object> params) {
        ShakeEffect.Builder builder = ShakeEffect.builder();
        ShakeEffect defaults = builder.build();
        builder.amplitude(number(params, "a", defaults.getAmplitude()));
        builder.frequency(number(params, "f", defaults.getFrequency()));
        return builder.build();
    }

    private static Effect createGradient(Map<String, Object> params) {
        GradientEffect.Builder builder = GradientEffect.builder();
        if (params.containsKey("from")) {
            builder.from(parseColor(params.get("from")));
        }
        if (params.containsKey("to")) {
            builder.to(parseColor(params.get("to")));
        }
        builder.hsv(booleanValue(params, "hsv", booleanValue(params, "h", false)));
        builder.flow(number(params, "f", number(params, "flow", 0d)));
        builder.span(booleanValue(params, "sp", booleanValue(params, "span", false)));
        builder.uniform(booleanValue(params, "uni", false));
        return builder.build();
    }

    private static Effect createRainbow(Map<String, Object> params) {
        RainbowEffect.Builder builder = RainbowEffect.builder();
        RainbowEffect defaults = builder.build();
        builder.baseHue(number(params, "hue", defaults.getBaseHue()));
        builder.frequency(number(params, "f", defaults.getFrequency()));
        builder.speed(number(params, "sp", defaults.getSpeed()));
        return builder.build();
    }

    private static Effect createFade(Map<String, Object> params) {
        FadeEffect.Builder builder = FadeEffect.builder();
        FadeEffect defaults = builder.build();
        builder.speed(number(params, "sp", defaults.getSpeed()));
        builder.frequency(number(params, "f", defaults.getFrequency()));
        return builder.build();
    }

    private static double number(Map<String, Object> params, String key, double fallback) {
        Object value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return fallback;
    }

    private static boolean booleanValue(Map<String, Object> params, String key, boolean fallback) {
        Object value = params.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return fallback;
    }

    private static Effect.Color parseColor(Object value) {
        if (value instanceof Effect.Color) {
            return (Effect.Color) value;
        }
        if (value instanceof String) {
            return Effect.Color.fromHex((String) value);
        }
        throw new IllegalArgumentException("Unsupported colour value: " + value);
    }
}
