package net.tysontheember.emberstextapi.client.markup;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.client.spans.SpanAttr;
import net.tysontheember.emberstextapi.client.spans.SpanBundle;
import net.tysontheember.emberstextapi.client.spans.TextSpanView;
import net.tysontheember.emberstextapi.immersivemessages.api.ObfuscateMode;
import net.tysontheember.emberstextapi.immersivemessages.api.ShakeType;
import net.tysontheember.emberstextapi.immersivemessages.api.TextAnchor;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared service that parses markup strings into span bundles.
 */
public final class MarkupService {
    private static final Pattern TAG_PATTERN = Pattern.compile("<(/?)([a-zA-Z][a-zA-Z0-9]*)((?:\\s+[a-zA-Z][a-zA-Z0-9]*[=:](?:[\\\"'][^\\\"']*[\\\"']|[^\\s>]+))*)>");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("([a-zA-Z][a-zA-Z0-9]*)[=:](?:([\\\"])([^\\\"]*)\\2|([^\\s>]+))");
    private static final int CACHE_LIMIT = 256;
    private static final char ESCAPED_LT = '\uE000';
    private static final char ESCAPED_GT = '\uE001';
    private static final char ESCAPED_BSLASH = '\uE002';

    private static final MarkupService INSTANCE = new MarkupService();

    private final Map<CacheKey, SpanBundle> cache = Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<CacheKey, SpanBundle> eldest) {
            return size() > CACHE_LIMIT;
        }
    });

    private MarkupService() {
    }

    public static MarkupService getInstance() {
        return INSTANCE;
    }

    public Optional<SpanBundle> parse(String rawText, Locale locale, boolean allowCache) {
        if (rawText == null || rawText.isEmpty()) {
            return Optional.empty();
        }

        Locale effectiveLocale = locale != null ? locale : Locale.ROOT;
        CacheKey key = new CacheKey(rawText, effectiveLocale);
        if (allowCache) {
            SpanBundle cached = cache.get(key);
            if (cached != null) {
                return Optional.of(cached);
            }
        }

        ParseResult result = parseInternal(preprocessEscapes(rawText));
        SpanBundle bundle = new SpanBundle(result.plainText(), result.views(), result.legacySpans(), result.warnings(), result.errors());
        if (allowCache && !bundle.hasErrors()) {
            cache.put(key, bundle);
        }
        return Optional.of(bundle);
    }

    private ParseResult parseInternal(String markup) {
        List<SpanSegment> segments = new ArrayList<>();
        List<TextSpan> styleStack = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Matcher matcher = TAG_PATTERN.matcher(markup);
        int lastEnd = 0;
        StringBuilder plain = new StringBuilder();

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String rawContent = markup.substring(lastEnd, matcher.start());
                if (!rawContent.isEmpty()) {
                    String content = restoreEscapes(rawContent);
                    int startIndex = plain.length();
                    plain.append(content);
                    TextSpan span = createSpanWithCurrentStyles(content, styleStack);
                    int endIndex = plain.length();
                    segments.add(new SpanSegment(startIndex, endIndex, span));
                }
            }

            boolean closing = "/".equals(matcher.group(1));
            String tagName = matcher.group(2).toLowerCase(Locale.ROOT);
            String attributes = matcher.group(3);

            if (closing) {
                if ("item".equals(tagName) && !styleStack.isEmpty()) {
                    TextSpan itemStyle = styleStack.get(styleStack.size() - 1);
                    if (itemStyle.getItemId() != null) {
                        TextSpan itemSpan = new TextSpan("");
                        itemSpan.item(itemStyle.getItemId(), itemStyle.getItemCount() != null ? itemStyle.getItemCount() : 1);
                        if (itemStyle.getItemOffsetX() != null || itemStyle.getItemOffsetY() != null) {
                            itemSpan.itemOffset(
                                itemStyle.getItemOffsetX() != null ? itemStyle.getItemOffsetX() : 0f,
                                itemStyle.getItemOffsetY() != null ? itemStyle.getItemOffsetY() : 0f
                            );
                        }
                        int index = plain.length();
                        segments.add(new SpanSegment(index, index, itemSpan));
                    }
                } else if ("entity".equals(tagName) && !styleStack.isEmpty()) {
                    TextSpan entityStyle = styleStack.get(styleStack.size() - 1);
                    if (entityStyle.getEntityId() != null) {
                        TextSpan entitySpan = new TextSpan("");
                        entitySpan.entity(entityStyle.getEntityId(), entityStyle.getEntityScale() != null ? entityStyle.getEntityScale() : 1.0f);
                        if (entityStyle.getEntityOffsetX() != null || entityStyle.getEntityOffsetY() != null) {
                            entitySpan.entityOffset(
                                entityStyle.getEntityOffsetX() != null ? entityStyle.getEntityOffsetX() : 0f,
                                entityStyle.getEntityOffsetY() != null ? entityStyle.getEntityOffsetY() : 0f
                            );
                        }
                        if (entityStyle.getEntityYaw() != null || entityStyle.getEntityPitch() != null) {
                            entitySpan.entityRotation(
                                entityStyle.getEntityYaw() != null ? entityStyle.getEntityYaw() : 45f,
                                entityStyle.getEntityPitch() != null ? entityStyle.getEntityPitch() : 0f
                            );
                        }
                        if (entityStyle.getEntityAnimation() != null) {
                            entitySpan.entityAnimation(entityStyle.getEntityAnimation());
                        }
                        int index = plain.length();
                        segments.add(new SpanSegment(index, index, entitySpan));
                    }
                }

                if (!styleStack.isEmpty()) {
                    styleStack.remove(styleStack.size() - 1);
                } else {
                    warnings.add("Unmatched closing tag: " + tagName);
                }
            } else {
                TextSpan currentStyle = styleStack.isEmpty() ? new TextSpan("") : styleStack.get(styleStack.size() - 1).inherit();
                applyTagToSpan(currentStyle, tagName, attributes, warnings, errors);
                styleStack.add(currentStyle);
            }

            lastEnd = matcher.end();
        }

        if (lastEnd < markup.length()) {
            String rawContent = markup.substring(lastEnd);
            if (!rawContent.isEmpty()) {
                String content = restoreEscapes(rawContent);
                int startIndex = plain.length();
                plain.append(content);
                TextSpan span = createSpanWithCurrentStyles(content, styleStack);
                int endIndex = plain.length();
                segments.add(new SpanSegment(startIndex, endIndex, span));
            }
        }

        if (!styleStack.isEmpty()) {
            warnings.add("Unclosed tag(s) detected");
        }

        List<TextSpan> legacy = new ArrayList<>(segments.size());
        List<TextSpanView> views = new ArrayList<>(segments.size());
        for (SpanSegment segment : segments) {
            TextSpan span = segment.span();
            legacy.add(span);
            views.add(new TextSpanView(segment.start(), segment.end(), SpanAttr.fromTextSpan(span)));
        }

        return new ParseResult(views, legacy, plain.toString(), warnings, errors);
    }

    private TextSpan createSpanWithCurrentStyles(String content, List<TextSpan> styleStack) {
        TextSpan span = new TextSpan(content);
        for (TextSpan style : styleStack) {
            inheritStyles(span, style);
        }
        return span;
    }

    private static void inheritStyles(TextSpan target, TextSpan source) {
        if (source.getColor() != null) target.color(source.getColor());
        if (source.getBold() != null) target.bold(source.getBold());
        if (source.getItalic() != null) target.italic(source.getItalic());
        if (source.getUnderline() != null) target.underline(source.getUnderline());
        if (source.getStrikethrough() != null) target.strikethrough(source.getStrikethrough());
        if (source.getObfuscated() != null) target.obfuscated(source.getObfuscated());
        if (source.getFont() != null) target.font(source.getFont());
        if (source.getGradientColors() != null) target.gradient(source.getGradientColors());
        if (source.getShakeType() != null && source.getShakeAmplitude() != null) {
            if (source.getShakeSpeed() != null && source.getShakeWavelength() != null) {
                target.shake(source.getShakeType(), source.getShakeAmplitude(), source.getShakeSpeed(), source.getShakeWavelength());
            } else if (source.getShakeSpeed() != null) {
                target.shake(source.getShakeType(), source.getShakeAmplitude(), source.getShakeSpeed());
            } else {
                target.shake(source.getShakeType(), source.getShakeAmplitude());
            }
        }
        if (source.getCharShakeType() != null && source.getCharShakeAmplitude() != null) {
            if (source.getCharShakeSpeed() != null && source.getCharShakeWavelength() != null) {
                target.charShake(source.getCharShakeType(), source.getCharShakeAmplitude(), source.getCharShakeSpeed(), source.getCharShakeWavelength());
            } else if (source.getCharShakeSpeed() != null) {
                target.charShake(source.getCharShakeType(), source.getCharShakeAmplitude(), source.getCharShakeSpeed());
            } else {
                target.charShake(source.getCharShakeType(), source.getCharShakeAmplitude());
            }
        }
        if (source.getObfuscateMode() != null) {
            target.obfuscate(source.getObfuscateMode(), source.getObfuscateSpeed() != null ? source.getObfuscateSpeed() : 1.0f);
        }
        if (source.getBackgroundColor() != null) {
            target.background(source.getBackgroundColor());
        }
        if (source.getBackgroundGradient() != null) {
            target.backgroundGradient(source.getBackgroundGradient());
        }
        if (source.getItemId() != null) {
            target.item(source.getItemId(), source.getItemCount() != null ? source.getItemCount() : 1);
            if (source.getItemOffsetX() != null || source.getItemOffsetY() != null) {
                target.itemOffset(
                    source.getItemOffsetX() != null ? source.getItemOffsetX() : 0f,
                    source.getItemOffsetY() != null ? source.getItemOffsetY() : 0f
                );
            }
        }
        if (source.getGlobalBackground() != null) {
            target.globalBackground(source.getGlobalBackground());
        }
        if (source.getGlobalBackgroundColor() != null) {
            target.globalBackgroundColor(source.getGlobalBackgroundColor());
        }
        if (source.getGlobalBackgroundGradient() != null) {
            target.globalBackgroundGradient(source.getGlobalBackgroundGradient());
        }
        if (source.getGlobalBorderStart() != null) {
            target.globalBorder(source.getGlobalBorderStart(), source.getGlobalBorderEnd());
        }
        if (source.getGlobalXOffset() != null || source.getGlobalYOffset() != null) {
            target.globalOffset(
                source.getGlobalXOffset() != null ? source.getGlobalXOffset() : 0f,
                source.getGlobalYOffset() != null ? source.getGlobalYOffset() : 0f
            );
        }
        if (source.getGlobalAnchor() != null) {
            target.globalAnchor(source.getGlobalAnchor());
        }
        if (source.getGlobalAlign() != null) {
            target.globalAlign(source.getGlobalAlign());
        }
        if (source.getGlobalScale() != null) {
            target.globalScale(source.getGlobalScale());
        }
        if (source.getGlobalShadow() != null) {
            target.globalShadow(source.getGlobalShadow());
        }
        if (source.getGlobalFadeInTicks() != null) {
            target.globalFadeIn(source.getGlobalFadeInTicks());
        }
        if (source.getGlobalFadeOutTicks() != null) {
            target.globalFadeOut(source.getGlobalFadeOutTicks());
        }
        if (source.getGlobalTypewriterSpeed() != null) {
            target.globalTypewriter(source.getGlobalTypewriterSpeed(), source.getGlobalTypewriterCenter() != null ? source.getGlobalTypewriterCenter() : false);
        }
    }

    private void applyTagToSpan(TextSpan span, String tagName, String attributes, List<String> warnings, List<String> errors) {
        Map<String, String> attrs = parseAttributes(attributes);
        switch (tagName) {
            case "bold", "b" -> span.bold(true);
            case "italic", "i" -> span.italic(true);
            case "underline", "u" -> span.underline(true);
            case "strikethrough", "s" -> span.strikethrough(true);
            case "obfuscated", "obf" -> span.obfuscated(true);
            case "color", "c" -> {
                String colorValue = attrs.getOrDefault("value", attrs.get("color"));
                if (colorValue != null) span.color(colorValue);
            }
            case "font" -> {
                String fontValue = attrs.getOrDefault("value", attrs.get("font"));
                if (fontValue != null) {
                    ResourceLocation font = ResourceLocation.tryParse(fontValue);
                    if (font != null) span.font(font);
                }
            }
            case "grad", "gradient" -> {
                String values = attrs.get("values");
                if (values != null) {
                    String[] colorStrs = values.split(",");
                    if (colorStrs.length >= 2) {
                        for (int i = 0; i < colorStrs.length; i++) {
                            colorStrs[i] = colorStrs[i].trim();
                        }
                        span.gradient(colorStrs);
                    } else {
                        warnings.add("Gradient tag requires at least two colors");
                    }
                } else {
                    String from = attrs.get("from");
                    String to = attrs.get("to");
                    if (from != null && to != null) {
                        span.gradient(from, to);
                    } else {
                        warnings.add("Gradient tag missing color definition");
                    }
                }
            }
            case "typewriter", "type" -> {
                String speedStr = attrs.getOrDefault("speed", "1.0");
                String centerStr = attrs.get("center");
                try {
                    float speed = Float.parseFloat(speedStr);
                    boolean center = "true".equalsIgnoreCase(centerStr);
                    span.globalTypewriter(speed, center);
                } catch (NumberFormatException ex) {
                    errors.add("Invalid typewriter speed: " + speedStr);
                }
            }
            case "shake" -> {
                String typeStr = attrs.getOrDefault("type", "random");
                String amplitudeStr = attrs.getOrDefault("amplitude", "1.0");
                String speedStr = attrs.get("speed");
                String wavelengthStr = attrs.get("wavelength");
                try {
                    ShakeType type = ShakeType.valueOf(typeStr.toUpperCase(Locale.ROOT));
                    float amplitude = Float.parseFloat(amplitudeStr);
                    if (speedStr != null && wavelengthStr != null) {
                        float speed = Float.parseFloat(speedStr);
                        float wavelength = Float.parseFloat(wavelengthStr);
                        span.shake(type, amplitude, speed, wavelength);
                    } else if (speedStr != null) {
                        float speed = Float.parseFloat(speedStr);
                        span.shake(type, amplitude, speed);
                    } else {
                        span.shake(type, amplitude);
                    }
                } catch (Exception ex) {
                    errors.add("Invalid shake tag attributes");
                }
            }
            case "wiggle", "charshake" -> {
                String typeStr = attrs.getOrDefault("type", "random");
                String amplitudeStr = attrs.getOrDefault("amplitude", "1.0");
                String speedStr = attrs.get("speed");
                String wavelengthStr = attrs.get("wavelength");
                try {
                    ShakeType type = ShakeType.valueOf(typeStr.toUpperCase(Locale.ROOT));
                    float amplitude = Float.parseFloat(amplitudeStr);
                    if (speedStr != null && wavelengthStr != null) {
                        float speed = Float.parseFloat(speedStr);
                        float wavelength = Float.parseFloat(wavelengthStr);
                        span.charShake(type, amplitude, speed, wavelength);
                    } else if (speedStr != null) {
                        float speed = Float.parseFloat(speedStr);
                        span.charShake(type, amplitude, speed);
                    } else {
                        span.charShake(type, amplitude);
                    }
                } catch (Exception ex) {
                    errors.add("Invalid charshake tag attributes");
                }
            }
            case "wave" -> {
                String amplitudeStr = attrs.getOrDefault("amplitude", "1.0");
                String speedStr = attrs.get("speed");
                String wavelengthStr = attrs.get("wavelength");
                try {
                    float amplitude = Float.parseFloat(amplitudeStr);
                    if (speedStr != null && wavelengthStr != null) {
                        float speed = Float.parseFloat(speedStr);
                        float wavelength = Float.parseFloat(wavelengthStr);
                        span.shake(ShakeType.WAVE, amplitude, speed, wavelength);
                    } else if (speedStr != null) {
                        float speed = Float.parseFloat(speedStr);
                        span.shake(ShakeType.WAVE, amplitude, speed);
                    } else {
                        span.shake(ShakeType.WAVE, amplitude);
                    }
                } catch (NumberFormatException ex) {
                    errors.add("Invalid wave tag attributes");
                }
            }
            case "obfuscate", "scramble" -> {
                String modeStr = attrs.getOrDefault("mode", "random");
                String speedStr = attrs.getOrDefault("speed", "1.0");
                try {
                    ObfuscateMode mode = ObfuscateMode.valueOf(modeStr.toUpperCase(Locale.ROOT));
                    float speed = Float.parseFloat(speedStr);
                    span.obfuscate(mode, speed);
                } catch (Exception ex) {
                    errors.add("Invalid obfuscate tag attributes");
                }
            }
            case "background", "bg" -> {
                String colorStr = attrs.get("color");
                String borderColorStr = attrs.get("bordercolor");
                String borderStartStr = attrs.get("borderstart");
                String borderEndStr = attrs.get("borderend");

                if (colorStr != null) {
                    ImmersiveColor bgColor = parseImmersiveColor(colorStr);
                    if (bgColor != null) {
                        span.globalBackgroundColor(bgColor);
                    } else {
                        errors.add("Invalid background color: " + colorStr);
                    }
                } else {
                    span.globalBackground(true);
                }

                if (borderColorStr != null) {
                    ImmersiveColor borderColor = parseImmersiveColor(borderColorStr);
                    if (borderColor != null) {
                        span.globalBorder(borderColor, borderColor);
                    } else {
                        errors.add("Invalid background border color: " + borderColorStr);
                    }
                }

                if (borderStartStr != null && borderEndStr != null) {
                    ImmersiveColor borderStart = parseImmersiveColor(borderStartStr);
                    ImmersiveColor borderEnd = parseImmersiveColor(borderEndStr);
                    if (borderStart != null && borderEnd != null) {
                        span.globalBorder(borderStart, borderEnd);
                    } else {
                        errors.add("Invalid background border gradient");
                    }
                }
            }
            case "backgroundgradient", "bggradient" -> {
                String fromStr = attrs.get("from");
                String toStr = attrs.get("to");
                if (fromStr != null && toStr != null) {
                    ImmersiveColor from = parseImmersiveColor(fromStr);
                    ImmersiveColor to = parseImmersiveColor(toStr);
                    if (from != null && to != null) {
                        span.globalBackgroundGradient(from, to);
                    } else {
                        errors.add("Invalid background gradient");
                    }
                } else {
                    warnings.add("backgroundgradient requires from and to attributes");
                }
            }
            case "scale" -> {
                String scaleStr = attrs.getOrDefault("value", "1.0");
                try {
                    float scale = Float.parseFloat(scaleStr);
                    span.globalScale(scale);
                } catch (NumberFormatException ex) {
                    errors.add("Invalid scale value: " + scaleStr);
                }
            }
            case "offset" -> {
                String xStr = attrs.getOrDefault("x", "0.0");
                String yStr = attrs.getOrDefault("y", "0.0");
                try {
                    float x = Float.parseFloat(xStr);
                    float y = Float.parseFloat(yStr);
                    span.globalOffset(x, y);
                } catch (NumberFormatException ex) {
                    errors.add("Invalid offset values");
                }
            }
            case "anchor" -> {
                String anchorStr = attrs.getOrDefault("value", "TOP_CENTER");
                try {
                    TextAnchor anchor = TextAnchor.valueOf(anchorStr.toUpperCase(Locale.ROOT));
                    span.globalAnchor(anchor);
                } catch (IllegalArgumentException ex) {
                    errors.add("Invalid anchor value: " + anchorStr);
                }
            }
            case "align" -> {
                String alignStr = attrs.getOrDefault("value", "TOP_CENTER");
                try {
                    TextAnchor align = TextAnchor.valueOf(alignStr.toUpperCase(Locale.ROOT));
                    span.globalAlign(align);
                } catch (IllegalArgumentException ex) {
                    errors.add("Invalid align value: " + alignStr);
                }
            }
            case "shadow" -> {
                String shadowStr = attrs.getOrDefault("value", "true");
                boolean shadow = "true".equalsIgnoreCase(shadowStr);
                span.globalShadow(shadow);
            }
            case "fade" -> {
                String inStr = attrs.get("in");
                String outStr = attrs.get("out");

                if (inStr != null) {
                    try {
                        int inTicks = Integer.parseInt(inStr);
                        span.globalFadeIn(inTicks);
                    } catch (NumberFormatException ex) {
                        errors.add("Invalid fade in value: " + inStr);
                    }
                }

                if (outStr != null) {
                    try {
                        int outTicks = Integer.parseInt(outStr);
                        span.globalFadeOut(outTicks);
                    } catch (NumberFormatException ex) {
                        errors.add("Invalid fade out value: " + outStr);
                    }
                }
            }
            case "item" -> {
                String itemId = attrs.getOrDefault("value", attrs.get("id"));
                String sizeStr = attrs.getOrDefault("size", attrs.getOrDefault("count", "1"));
                String offsetXStr = attrs.getOrDefault("offsetx", attrs.getOrDefault("x", "0"));
                String offsetYStr = attrs.getOrDefault("offsety", attrs.getOrDefault("y", "0"));

                if (itemId != null) {
                    try {
                        int size = Integer.parseInt(sizeStr);
                        span.item(itemId, size);
                    } catch (NumberFormatException ex) {
                        span.item(itemId);
                        warnings.add("Invalid item size, defaulting to 1");
                    }

                    try {
                        float offsetX = Float.parseFloat(offsetXStr);
                        float offsetY = Float.parseFloat(offsetYStr);
                        if (offsetX != 0 || offsetY != 0) {
                            span.itemOffset(offsetX, offsetY);
                        }
                    } catch (NumberFormatException ex) {
                        warnings.add("Invalid item offset ignored");
                    }
                } else {
                    warnings.add("Item tag missing id");
                }
            }
            case "entity" -> {
                String entityId = attrs.getOrDefault("value", attrs.get("id"));
                String scaleStr = attrs.getOrDefault("scale", "1.0");
                String offsetXStr = attrs.getOrDefault("offsetx", attrs.getOrDefault("x", "0"));
                String offsetYStr = attrs.getOrDefault("offsety", attrs.getOrDefault("y", "0"));
                String yawStr = attrs.getOrDefault("yaw", "45");
                String pitchStr = attrs.getOrDefault("pitch", "0");
                String animation = attrs.getOrDefault("animation", attrs.getOrDefault("anim", "idle"));

                if (entityId != null) {
                    try {
                        float scale = Float.parseFloat(scaleStr);
                        span.entity(entityId, scale);
                    } catch (NumberFormatException ex) {
                        span.entity(entityId);
                        warnings.add("Invalid entity scale, defaulting to 1.0");
                    }

                    try {
                        float offsetX = Float.parseFloat(offsetXStr);
                        float offsetY = Float.parseFloat(offsetYStr);
                        if (offsetX != 0 || offsetY != 0) {
                            span.entityOffset(offsetX, offsetY);
                        }
                    } catch (NumberFormatException ex) {
                        warnings.add("Invalid entity offset ignored");
                    }

                    try {
                        float yaw = Float.parseFloat(yawStr);
                        float pitch = Float.parseFloat(pitchStr);
                        span.entityRotation(yaw, pitch);
                    } catch (NumberFormatException ex) {
                        warnings.add("Invalid entity rotation ignored");
                    }

                    if (animation != null && !animation.isEmpty()) {
                        span.entityAnimation(animation);
                    }
                } else {
                    warnings.add("Entity tag missing id");
                }
            }
            default -> warnings.add("Unknown tag: " + tagName);
        }
    }

    private Map<String, String> parseAttributes(String attributeString) {
        Map<String, String> attributes = new java.util.HashMap<>();
        if (attributeString == null || attributeString.trim().isEmpty()) {
            return attributes;
        }

        Matcher matcher = ATTRIBUTE_PATTERN.matcher(attributeString);
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
            attributes.put(key.toLowerCase(Locale.ROOT), value);
        }
        return attributes;
    }

    private ImmersiveColor parseImmersiveColor(String value) {
        if (value == null || value.trim().isEmpty()) return null;

        String v = value.trim();
        try {
            if (v.startsWith("#")) v = v.substring(1);
            if (v.startsWith("0x")) v = v.substring(2);
            if (v.length() == 8) {
                return new ImmersiveColor((int) Long.parseLong(v, 16));
            } else if (v.length() == 6) {
                return new ImmersiveColor(0xFF000000 | Integer.parseInt(v, 16));
            }
        } catch (NumberFormatException ignored) {
        }

        ChatFormatting fmt = ChatFormatting.getByName(value);
        if (fmt != null && fmt.getColor() != null) {
            return new ImmersiveColor(0xFF000000 | fmt.getColor());
        }

        TextColor parsed = TextColor.parseColor(value);
        if (parsed != null) {
            int c = parsed.getValue();
            if ((c & 0xFF000000) == 0) c |= 0xFF000000;
            return new ImmersiveColor(c);
        }

        return null;
    }

    private String preprocessEscapes(String input) {
        StringBuilder builder = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\\' && i + 1 < input.length()) {
                char next = input.charAt(i + 1);
                if (next == '<') {
                    builder.append(ESCAPED_LT);
                    i++;
                    continue;
                } else if (next == '>') {
                    builder.append(ESCAPED_GT);
                    i++;
                    continue;
                } else if (next == '\\') {
                    builder.append(ESCAPED_BSLASH);
                    i++;
                    continue;
                }
            }
            builder.append(c);
        }
        return builder.toString();
    }

    private String restoreEscapes(String input) {
        StringBuilder builder = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == ESCAPED_LT) {
                builder.append('<');
            } else if (c == ESCAPED_GT) {
                builder.append('>');
            } else if (c == ESCAPED_BSLASH) {
                builder.append('\\');
            } else {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    private record ParseResult(List<TextSpanView> views,
                               List<TextSpan> legacySpans,
                               String plainText,
                               List<String> warnings,
                               List<String> errors) {}

    private record SpanSegment(int start, int end, TextSpan span) {}

    private record CacheKey(String rawText, Locale locale) {
        private CacheKey {
            rawText = Objects.requireNonNull(rawText, "rawText");
            locale = Objects.requireNonNull(locale, "locale");
        }
    }
}
