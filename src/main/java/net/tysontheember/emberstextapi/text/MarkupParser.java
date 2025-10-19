package net.tysontheember.emberstextapi.text;

import com.mojang.logging.LogUtils;
import net.tysontheember.emberstextapi.text.effect.Effect;
import net.tysontheember.emberstextapi.text.effect.EffectParameterConverters;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses lightweight markup tags into {@link AttributedText} instances.
 */
public final class MarkupParser {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern HEX_COLOR = Pattern.compile("#(?:[0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(?:\\.\\d+)?");
    private static final Set<String> KNOWN_TAGS = Set.of(
            "typewriter",
            "wave",
            "wiggle",
            "shake",
            "grad",
            "rainb",
            "fade",
            "pulse",
            "neon",
            "pend",
            "swing",
            "turb",
            "shadow",
            "bold",
            "italic",
            "underlined",
            "strikethrough",
            "obfuscated",
            "background"
    );
    private static final TagRegistry TAG_REGISTRY = createRegistry();

    private MarkupParser() {
    }

    /**
     * Parses the supplied markup string into an {@link AttributedText} structure.
     *
     * @param input markup text
     * @return parsed attributed text
     */
    public static AttributedText parse(String input) {
        Objects.requireNonNull(input, "input");

        StringBuilder plainText = new StringBuilder();
        List<Segment> segments = new ArrayList<>();
        Deque<ActiveTag> stack = new ArrayDeque<>();
        int segmentStart = 0;

        int index = 0;
        while (index < input.length()) {
            char current = input.charAt(index);
            if (current == '<') {
                int closing = input.indexOf('>', index + 1);
                if (closing == -1) {
                    plainText.append(current);
                    index++;
                    continue;
                }

                String tagContent = input.substring(index + 1, closing).trim();
                if (tagContent.isEmpty()) {
                    plainText.append(current);
                    index++;
                    continue;
                }

                if (tagContent.startsWith("/")) {
                    String tagName = tagContent.substring(1).trim().toLowerCase(Locale.ROOT);
                    segmentStart = appendSegmentIfNeeded(plainText, segments, stack, segmentStart);
                    if (!removeTag(stack, tagName)) {
                        LOGGER.warn("Encountered unmatched closing tag </{}>.", tagName);
                    }
                } else {
                    TagToken token = parseStartTag(tagContent);
                    if (token == null) {
                        plainText.append(current);
                        index++;
                        continue;
                    }

                    segmentStart = appendSegmentIfNeeded(plainText, segments, stack, segmentStart);
                    Optional<ActiveTag> activeTag = createActiveTag(token);
                    activeTag.ifPresent(stack::push);
                }
                index = closing + 1;
            } else {
                plainText.append(current);
                index++;
            }
        }

        appendSegmentIfNeeded(plainText, segments, stack, segmentStart);

        AttributedText.Builder builder = AttributedText.builder().text(plainText.toString());
        for (Segment segment : segments) {
            if (segment.start >= segment.end) {
                continue;
            }
            builder.addSpan(segment.toSpan());
        }
        return builder.build();
    }

    private static Optional<ActiveTag> createActiveTag(TagToken token) {
        String name = token.name.toLowerCase(Locale.ROOT);
        if (!KNOWN_TAGS.contains(name)) {
            LOGGER.warn("Unknown tag <{}> encountered; skipping.", name);
            return Optional.empty();
        }

        Map<String, Object> parameters = token.parameters;
        try {
            TAG_REGISTRY.validate(name, parameters);
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("Invalid parameters for <{}>: {}", name, ex.getMessage());
            return Optional.empty();
        }

        TagApplier applier = createApplier(name);
        if (applier == null) {
            return Optional.empty();
        }

        return Optional.of(new ActiveTag(name, parameters, applier));
    }

    private static TagApplier createApplier(String tagName) {
        switch (tagName) {
            case "bold":
                return (state, params) -> state.bold = true;
            case "italic":
                return (state, params) -> state.italic = true;
            case "underlined":
                return (state, params) -> state.underlined = true;
            case "strikethrough":
                return (state, params) -> state.strikethrough = true;
            case "obfuscated":
                return (state, params) -> state.obfuscated = true;
            case "background":
                return (state, params) -> applyBackground(state, params);
            case "grad":
                return (state, params) -> applyGradient(state, params);
            case "typewriter":
            case "wave":
            case "wiggle":
            case "shake":
            case "rainb":
            case "fade":
            case "pulse":
            case "neon":
            case "pend":
            case "swing":
            case "turb":
            case "shadow":
                return (state, params) -> applyEffect(state, tagName, params);
            default:
                return null;
        }
    }

    private static void applyEffect(AttributeState state, String name, Map<String, Object> params) {
        Optional<Effect> effect = EffectParameterConverters.convert(name, params);
        if (effect.isPresent()) {
            state.effects.put(name, effect.get());
            return;
        }
        Object value;
        if (params.isEmpty()) {
            value = Boolean.TRUE;
        } else {
            value = copyParameters(params);
        }
        state.effectParams.put(name, value);
    }

    private static void applyGradient(AttributeState state, Map<String, Object> params) {
        GradientState gradientState = new GradientState();
        gradientState.from = (String) params.get("from");
        gradientState.to = (String) params.get("to");
        gradientState.hsv = getBoolean(params, "hsv");
        gradientState.span = getBoolean(params, "span");
        gradientState.uni = getBoolean(params, "uni");
        gradientState.flow = getDouble(params, "flow", 0.0);
        state.gradient = gradientState;
    }

    private static void applyBackground(AttributeState state, Map<String, Object> params) {
        if (params.containsKey("on")) {
            state.backgroundOn = Boolean.TRUE.equals(params.get("on"));
        } else {
            state.backgroundOn = true;
        }
        if (params.containsKey("color")) {
            state.backgroundColor = (String) params.get("color");
        }
        if (params.containsKey("border")) {
            state.backgroundBorder = (String) params.get("border");
        }
        if (params.containsKey("alpha")) {
            state.backgroundAlpha = ((Number) params.get("alpha")).doubleValue();
        }
    }

    private static boolean getBoolean(Map<String, Object> params, String key) {
        Object value = params.get(key);
        return value instanceof Boolean && (Boolean) value;
    }

    private static double getDouble(Map<String, Object> params, String key, double defaultValue) {
        Object value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    private static Map<String, Object> copyParameters(Map<String, Object> params) {
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) value;
                copy.put(entry.getKey(), copyParameters(nested));
            } else {
                copy.put(entry.getKey(), value);
            }
        }
        return copy;
    }

    private static boolean removeTag(Deque<ActiveTag> stack, String tagName) {
        ActiveTag top = stack.peek();
        if (top != null && top.name.equals(tagName)) {
            stack.pop();
            return true;
        }
        Iterator<ActiveTag> iterator = stack.iterator();
        while (iterator.hasNext()) {
            ActiveTag tag = iterator.next();
            if (tag.name.equals(tagName)) {
                iterator.remove();
                LOGGER.warn("Markup for </{}> closed out of order; continuing.", tagName);
                return true;
            }
        }
        return false;
    }

    private static int appendSegmentIfNeeded(StringBuilder text, List<Segment> segments, Deque<ActiveTag> stack, int segmentStart) {
        if (text.length() <= segmentStart) {
            return segmentStart;
        }
        AttributeState state = buildState(stack);
        Segment last = segments.isEmpty() ? null : segments.get(segments.size() - 1);
        if (last != null && last.state.equals(state) && last.end == segmentStart) {
            last.end = text.length();
        } else {
            segments.add(new Segment(segmentStart, text.length(), state));
        }
        return text.length();
    }

    private static AttributeState buildState(Deque<ActiveTag> stack) {
        AttributeState state = new AttributeState();
        Iterator<ActiveTag> iterator = stack.descendingIterator();
        while (iterator.hasNext()) {
            ActiveTag tag = iterator.next();
            tag.apply(state);
        }
        return state;
    }

    private static TagToken parseStartTag(String content) {
        int length = content.length();
        int index = 0;
        while (index < length && Character.isWhitespace(content.charAt(index))) {
            index++;
        }
        if (index >= length) {
            return null;
        }

        int nameStart = index;
        while (index < length && !Character.isWhitespace(content.charAt(index))) {
            char ch = content.charAt(index);
            if (ch == '=') {
                break;
            }
            index++;
        }
        String name = content.substring(nameStart, index).toLowerCase(Locale.ROOT);
        Map<String, Object> parameters = new LinkedHashMap<>();

        while (index < length) {
            while (index < length && Character.isWhitespace(content.charAt(index))) {
                index++;
            }
            if (index >= length) {
                break;
            }

            int keyStart = index;
            while (index < length && !Character.isWhitespace(content.charAt(index)) && content.charAt(index) != '=') {
                index++;
            }
            String key = content.substring(keyStart, index).toLowerCase(Locale.ROOT);

            while (index < length && Character.isWhitespace(content.charAt(index))) {
                index++;
            }

            String value;
            if (index < length && content.charAt(index) == '=') {
                index++;
                while (index < length && Character.isWhitespace(content.charAt(index))) {
                    index++;
                }
                if (index < length && (content.charAt(index) == '"' || content.charAt(index) == '\'')) {
                    char quote = content.charAt(index++);
                    int valueStart = index;
                    while (index < length && content.charAt(index) != quote) {
                        index++;
                    }
                    value = content.substring(valueStart, Math.min(index, length));
                    if (index < length && content.charAt(index) == quote) {
                        index++;
                    }
                } else {
                    int valueStart = index;
                    while (index < length && !Character.isWhitespace(content.charAt(index))) {
                        index++;
                    }
                    value = content.substring(valueStart, index);
                }
            } else {
                value = "true";
            }

            if (!key.isEmpty()) {
                parameters.put(key, convertValue(value));
            }
        }

        return new TagToken(name, Collections.unmodifiableMap(parameters));
    }

    private static Object convertValue(String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        String lower = trimmed.toLowerCase(Locale.ROOT);
        if ("true".equals(lower)) {
            return Boolean.TRUE;
        }
        if ("false".equals(lower)) {
            return Boolean.FALSE;
        }

        Matcher hexMatcher = HEX_COLOR.matcher(trimmed);
        if (hexMatcher.matches()) {
            return trimmed.toUpperCase(Locale.ROOT);
        }

        Matcher numberMatcher = NUMBER_PATTERN.matcher(trimmed);
        if (numberMatcher.matches()) {
            if (trimmed.contains(".")) {
                return Double.parseDouble(trimmed);
            }
            return Long.parseLong(trimmed);
        }

        return trimmed;
    }

    private static TagRegistry createRegistry() {
        TagRegistry registry = new TagRegistry();
        registerNoParams(registry, "bold");
        registerNoParams(registry, "italic");
        registerNoParams(registry, "underlined");
        registerNoParams(registry, "strikethrough");
        registerNoParams(registry, "obfuscated");
        registerOpenEffect(registry, "typewriter");
        registerOpenEffect(registry, "wave");
        registerOpenEffect(registry, "wiggle");
        registerOpenEffect(registry, "shake");
        registerOpenEffect(registry, "rainb");
        registerOpenEffect(registry, "fade");
        registerOpenEffect(registry, "pulse");
        registerOpenEffect(registry, "neon");
        registerOpenEffect(registry, "pend");
        registerOpenEffect(registry, "swing");
        registerOpenEffect(registry, "turb");
        registerOpenEffect(registry, "shadow");
        registry.register("grad", MarkupParser::validateGradient);
        registry.register("background", MarkupParser::validateBackground);
        return registry;
    }

    private static void registerNoParams(TagRegistry registry, String tag) {
        registry.register(tag, params -> {
            if (!params.isEmpty()) {
                throw new IllegalArgumentException("tag does not accept parameters");
            }
        });
    }

    private static void registerOpenEffect(TagRegistry registry, String tag) {
        registry.register(tag, params -> {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                Object value = entry.getValue();
                if (!(value instanceof Boolean || value instanceof Number || value instanceof String)) {
                    throw new IllegalArgumentException("unsupported parameter type for " + entry.getKey());
                }
            }
        });
    }

    private static void validateGradient(Map<String, Object> params) {
        Object from = params.get("from");
        Object to = params.get("to");
        if (!(from instanceof String) || !(to instanceof String)) {
            throw new IllegalArgumentException("grad requires from and to colors");
        }
        validateOptionalBoolean(params, "hsv");
        validateOptionalBoolean(params, "span");
        validateOptionalBoolean(params, "uni");
        validateOptionalNumber(params, "flow");
    }

    private static void validateBackground(Map<String, Object> params) {
        validateOptionalBoolean(params, "on");
        validateOptionalNumber(params, "alpha");
        validateOptionalString(params, "color");
        validateOptionalString(params, "border");
    }

    private static void validateOptionalBoolean(Map<String, Object> params, String key) {
        if (params.containsKey(key) && !(params.get(key) instanceof Boolean)) {
            throw new IllegalArgumentException(key + " must be a boolean");
        }
    }

    private static void validateOptionalNumber(Map<String, Object> params, String key) {
        if (params.containsKey(key) && !(params.get(key) instanceof Number)) {
            throw new IllegalArgumentException(key + " must be numeric");
        }
    }

    private static void validateOptionalString(Map<String, Object> params, String key) {
        if (params.containsKey(key) && !(params.get(key) instanceof String)) {
            throw new IllegalArgumentException(key + " must be text");
        }
    }

    private interface TagApplier {
        void apply(AttributeState state, Map<String, Object> parameters);
    }

    private static final class ActiveTag {
        private final String name;
        private final Map<String, Object> parameters;
        private final TagApplier applier;

        private ActiveTag(String name, Map<String, Object> parameters, TagApplier applier) {
            this.name = name;
            this.parameters = parameters;
            this.applier = applier;
        }

        private void apply(AttributeState state) {
            applier.apply(state, parameters);
        }
    }

    private static final class Segment {
        private final int start;
        private int end;
        private final AttributeState state;

        private Segment(int start, int end, AttributeState state) {
            this.start = start;
            this.end = end;
            this.state = state;
        }

        private Span toSpan() {
            AttributeSet.Builder attributeBuilder = AttributeSet.builder();
            if (state.color != null) {
                attributeBuilder.color(state.color);
            }
            if (state.gradient != null) {
                attributeBuilder.gradient(state.gradient.toGradient());
            }
            AttributeSet.Style style = AttributeSet.Style.builder()
                    .bold(state.bold)
                    .italic(state.italic)
                    .underlined(state.underlined)
                    .strikethrough(state.strikethrough)
                    .obfuscated(state.obfuscated)
                    .build();
            AttributeSet.Background.Builder backgroundBuilder = AttributeSet.Background.builder()
                    .on(state.backgroundOn);
            if (state.backgroundColor != null) {
                backgroundBuilder.color(state.backgroundColor);
            }
            if (state.backgroundBorder != null) {
                backgroundBuilder.border(state.backgroundBorder);
            }
            if (state.backgroundAlpha != null) {
                backgroundBuilder.alpha(state.backgroundAlpha);
            }
            attributeBuilder.style(style);
            attributeBuilder.background(backgroundBuilder.build());
            if (!state.effects.isEmpty()) {
                attributeBuilder.effects(state.effects);
            }
            if (!state.effectParams.isEmpty()) {
                attributeBuilder.effectParams(state.effectParams);
            }
            return Span.builder()
                    .start(start)
                    .end(end)
                    .attributes(attributeBuilder.build())
                    .build();
        }
    }

    private static final class AttributeState {
        private String color;
        private GradientState gradient;
        private boolean bold;
        private boolean italic;
        private boolean underlined;
        private boolean strikethrough;
        private boolean obfuscated;
        private boolean backgroundOn;
        private String backgroundColor;
        private String backgroundBorder;
        private Double backgroundAlpha;
        private final Map<String, Object> effectParams = new LinkedHashMap<>();
        private final Map<String, Effect> effects = new LinkedHashMap<>();

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof AttributeState)) {
                return false;
            }
            AttributeState that = (AttributeState) o;
            return bold == that.bold
                    && italic == that.italic
                    && underlined == that.underlined
                    && strikethrough == that.strikethrough
                    && obfuscated == that.obfuscated
                    && backgroundOn == that.backgroundOn
                    && Objects.equals(color, that.color)
                    && Objects.equals(gradient, that.gradient)
                    && Objects.equals(backgroundColor, that.backgroundColor)
                    && Objects.equals(backgroundBorder, that.backgroundBorder)
                    && Objects.equals(backgroundAlpha, that.backgroundAlpha)
                    && Objects.equals(effectParams, that.effectParams)
                    && Objects.equals(effects, that.effects);
        }

        @Override
        public int hashCode() {
            return Objects.hash(color, gradient, bold, italic, underlined, strikethrough, obfuscated, backgroundOn, backgroundColor, backgroundBorder, backgroundAlpha, effectParams, effects);
        }
    }

    private static final class GradientState {
        private String from;
        private String to;
        private boolean hsv;
        private boolean span;
        private boolean uni;
        private double flow;

        private AttributeSet.Gradient toGradient() {
            return AttributeSet.Gradient.builder()
                    .from(from)
                    .to(to)
                    .hsv(hsv)
                    .span(span)
                    .uni(uni)
                    .flow(flow)
                    .build();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof GradientState)) {
                return false;
            }
            GradientState that = (GradientState) o;
            return hsv == that.hsv
                    && span == that.span
                    && uni == that.uni
                    && Double.compare(that.flow, flow) == 0
                    && Objects.equals(from, that.from)
                    && Objects.equals(to, that.to);
        }

        @Override
        public int hashCode() {
            return Objects.hash(from, to, hsv, span, uni, flow);
        }
    }

    private static final class TagToken {
        private final String name;
        private final Map<String, Object> parameters;

        private TagToken(String name, Map<String, Object> parameters) {
            this.name = name;
            this.parameters = parameters;
        }
    }
}

