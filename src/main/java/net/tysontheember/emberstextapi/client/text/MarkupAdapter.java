package net.tysontheember.emberstextapi.client.text;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import net.tysontheember.emberstextapi.duck.ETAStyle;
import net.tysontheember.emberstextapi.immersivemessages.api.MarkupParser;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import net.tysontheember.emberstextapi.immersivemessages.api.ObfuscateMode;
import net.tysontheember.emberstextapi.immersivemessages.api.ShakeType;

/**
 * Bridges legacy Immersive Message markup parsing to vanilla component traversal.
 */
public final class MarkupAdapter {
    private static final Locale LOCALE = Locale.ROOT;

    private MarkupAdapter() {
    }

    public static boolean hasMarkup(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        int start = text.indexOf('<');
        if (start < 0) {
            return false;
        }
        return text.indexOf('>', start + 1) > start;
    }

    public static boolean visitFormatted(String text, Style baseStyle, FormattedCharSink sink) {
        List<TextSpan> spans = MarkupParser.parse(text);
        if (spans.isEmpty()) {
            return StringDecomposer.iterateFormatted(text, baseStyle, sink);
        }

        if (spans.size() == 1) {
            TextSpan span = spans.get(0);
            SpanStylePayload payload = payloadFromSpan(span);
            if (span.getContent().equals(text) && payload.isEmpty()) {
                return StringDecomposer.iterateFormatted(text, baseStyle, sink);
            }
        }

        for (TextSpan span : spans) {
            String chunk = span.getContent();
            if (chunk.isEmpty()) {
                continue;
            }
            SpanStylePayload payload = payloadFromSpan(span);
            Style style = payload.isEmpty() ? baseStyle : applyToStyle(baseStyle, payload);
            if (!StringDecomposer.iterateFormatted(chunk, style, sink)) {
                return false;
            }
        }
        return true;
    }

    public static <T> Optional<T> visitLiteral(String text, Style baseStyle, FormattedText.StyledContentConsumer<T> consumer) {
        List<TextSpan> spans = MarkupParser.parse(text);
        if (spans.isEmpty()) {
            return consumer.accept(baseStyle, text);
        }

        if (spans.size() == 1) {
            TextSpan span = spans.get(0);
            SpanStylePayload payload = payloadFromSpan(span);
            if (span.getContent().equals(text) && payload.isEmpty()) {
                return consumer.accept(baseStyle, text);
            }
        }

        Optional<T> result = Optional.empty();
        for (TextSpan span : spans) {
            String chunk = span.getContent();
            if (chunk.isEmpty()) {
                continue;
            }
            SpanStylePayload payload = payloadFromSpan(span);
            Style style = payload.isEmpty() ? baseStyle : applyToStyle(baseStyle, payload);
            result = consumer.accept(style, chunk);
            if (result.isPresent()) {
                return result;
            }
        }
        return result;
    }

    public static Style applyToStyle(Style baseStyle, SpanStylePayload payload) {
        if (payload.isEmpty()) {
            return baseStyle;
        }

        Style style = ETAStyleOps.copyOf(baseStyle);
        if (payload.color() != null) {
            style = style.withColor(payload.color());
        }
        if (payload.bold() != null) {
            style = style.withBold(payload.bold());
        }
        if (payload.italic() != null) {
            style = style.withItalic(payload.italic());
        }
        if (payload.underline() != null) {
            style = style.withUnderlined(payload.underline());
        }
        if (payload.strikethrough() != null) {
            style = style.withStrikethrough(payload.strikethrough());
        }
        if (payload.obfuscated() != null) {
            style = style.withObfuscated(payload.obfuscated());
        }
        if (payload.font() != null) {
            style = style.withFont(payload.font());
        }

        ETAStyle duck = (ETAStyle) style;
        if (payload.hasEffectOverride()) {
            duck.eta$setEffects(payload.effects());
        }
        if (payload.hasTrackOverride()) {
            duck.eta$setTrack(payload.track());
        }
        if (payload.typewriterIndex() != null) {
            duck.eta$setTypewriterIndex(payload.typewriterIndex());
        }
        if (payload.neonIntensity() != null) {
            duck.eta$setNeonIntensity(payload.neonIntensity());
        }
        if (payload.wobbleAmplitude() != null) {
            duck.eta$setWobbleAmplitude(payload.wobbleAmplitude());
        }
        if (payload.wobbleSpeed() != null) {
            duck.eta$setWobbleSpeed(payload.wobbleSpeed());
        }
        if (payload.gradientFlow() != null) {
            duck.eta$setGradientFlow(payload.gradientFlow());
        }
        return style;
    }

    public static SpanStylePayload payloadFromSpan(TextSpan span) {
        TextColor color = span.getColor();
        Boolean bold = span.getBold();
        Boolean italic = span.getItalic();
        Boolean underline = span.getUnderline();
        Boolean strikethrough = span.getStrikethrough();
        Boolean obfuscated = span.getObfuscated();
        ResourceLocation font = span.getFont();

        List<SpanEffect> effects = collectEffects(span);
        boolean hasEffects = !effects.isEmpty();

        TypewriterTrack track = null;
        boolean hasTrack = false;
        Float typewriterSpeed = span.getTypewriterSpeed();
        if (typewriterSpeed != null) {
            track = new TypewriterTrack(TypewriterTrack.Mode.CHAR, typewriterSpeed, null);
            hasTrack = true;
        }

        Integer typewriterIndex = null;
        Float neonIntensity = null;
        Float wobbleAmplitude = null;
        Float wobbleSpeed = null;
        Float gradientFlow = null;

        return new SpanStylePayload(color, bold, italic, underline, strikethrough, obfuscated, font,
                hasEffects ? List.copyOf(effects) : List.of(), hasEffects, track, hasTrack, typewriterIndex, neonIntensity,
                wobbleAmplitude, wobbleSpeed, gradientFlow);
    }

    private static List<SpanEffect> collectEffects(TextSpan span) {
        List<SpanEffect> effects = new ArrayList<>();
        TextColor[] gradient = span.getGradientColors();
        if (gradient != null && gradient.length > 1) {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("colors", joinColors(gradient));
            effects.add(new SpanEffect("emberstextapi:gradient", params));
        }

        if (span.getShakeType() != null && span.getShakeAmplitude() != null) {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("type", span.getShakeType().name().toLowerCase(LOCALE));
            params.put("amplitude", Float.toString(span.getShakeAmplitude()));
            if (span.getShakeSpeed() != null) {
                params.put("speed", Float.toString(span.getShakeSpeed()));
            }
            if (span.getShakeWavelength() != null) {
                params.put("wavelength", Float.toString(span.getShakeWavelength()));
            }
            effects.add(new SpanEffect("emberstextapi:shake", params));
        }

        if (span.getCharShakeType() != null && span.getCharShakeAmplitude() != null) {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("type", span.getCharShakeType().name().toLowerCase(LOCALE));
            params.put("amplitude", Float.toString(span.getCharShakeAmplitude()));
            if (span.getCharShakeSpeed() != null) {
                params.put("speed", Float.toString(span.getCharShakeSpeed()));
            }
            if (span.getCharShakeWavelength() != null) {
                params.put("wavelength", Float.toString(span.getCharShakeWavelength()));
            }
            effects.add(new SpanEffect("emberstextapi:charshake", params));
        }

        ObfuscateMode obfuscate = span.getObfuscateMode();
        if (obfuscate != null) {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("mode", obfuscate.name().toLowerCase(LOCALE));
            if (span.getObfuscateSpeed() != null) {
                params.put("speed", Float.toString(span.getObfuscateSpeed()));
            }
            effects.add(new SpanEffect("emberstextapi:obfuscate", params));
        }

        return effects;
    }

    private static String joinColors(TextColor[] colors) {
        StringBuilder builder = new StringBuilder(colors.length * 7);
        for (int i = 0; i < colors.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(colorToString(colors[i]));
        }
        return builder.toString();
    }

    private static String colorToString(TextColor color) {
        return String.format(LOCALE, "#%06X", color.getValue());
    }

    public static final class SpanStylePayload {
        private final TextColor color;
        private final Boolean bold;
        private final Boolean italic;
        private final Boolean underline;
        private final Boolean strikethrough;
        private final Boolean obfuscated;
        private final ResourceLocation font;
        private final List<SpanEffect> effects;
        private final boolean hasEffects;
        private final TypewriterTrack track;
        private final boolean hasTrack;
        private final Integer typewriterIndex;
        private final Float neonIntensity;
        private final Float wobbleAmplitude;
        private final Float wobbleSpeed;
        private final Float gradientFlow;

        private SpanStylePayload(TextColor color, Boolean bold, Boolean italic, Boolean underline, Boolean strikethrough,
                Boolean obfuscated, ResourceLocation font, List<SpanEffect> effects, boolean hasEffects, TypewriterTrack track,
                boolean hasTrack, Integer typewriterIndex, Float neonIntensity, Float wobbleAmplitude, Float wobbleSpeed,
                Float gradientFlow) {
            this.color = color;
            this.bold = bold;
            this.italic = italic;
            this.underline = underline;
            this.strikethrough = strikethrough;
            this.obfuscated = obfuscated;
            this.font = font;
            this.effects = hasEffects ? effects : List.of();
            this.hasEffects = hasEffects;
            this.track = track;
            this.hasTrack = hasTrack;
            this.typewriterIndex = typewriterIndex;
            this.neonIntensity = neonIntensity;
            this.wobbleAmplitude = wobbleAmplitude;
            this.wobbleSpeed = wobbleSpeed;
            this.gradientFlow = gradientFlow;
        }

        public TextColor color() {
            return this.color;
        }

        public Boolean bold() {
            return this.bold;
        }

        public Boolean italic() {
            return this.italic;
        }

        public Boolean underline() {
            return this.underline;
        }

        public Boolean strikethrough() {
            return this.strikethrough;
        }

        public Boolean obfuscated() {
            return this.obfuscated;
        }

        public ResourceLocation font() {
            return this.font;
        }

        public List<SpanEffect> effects() {
            return this.effects;
        }

        public boolean hasEffectOverride() {
            return this.hasEffects;
        }

        public TypewriterTrack track() {
            return this.track;
        }

        public boolean hasTrackOverride() {
            return this.hasTrack;
        }

        public Integer typewriterIndex() {
            return this.typewriterIndex;
        }

        public Float neonIntensity() {
            return this.neonIntensity;
        }

        public Float wobbleAmplitude() {
            return this.wobbleAmplitude;
        }

        public Float wobbleSpeed() {
            return this.wobbleSpeed;
        }

        public Float gradientFlow() {
            return this.gradientFlow;
        }

        public boolean isEmpty() {
            return this.color == null && this.bold == null && this.italic == null && this.underline == null
                    && this.strikethrough == null && this.obfuscated == null && this.font == null && !this.hasEffects
                    && !this.hasTrack && this.typewriterIndex == null && this.neonIntensity == null
                    && this.wobbleAmplitude == null && this.wobbleSpeed == null && this.gradientFlow == null;
        }
    }
}
