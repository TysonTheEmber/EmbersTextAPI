package net.tysontheember.emberstextapi.immersivemessages.api;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.MessageEffect;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.MessageEffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.MessageAttribute;
import net.tysontheember.emberstextapi.immersivemessages.effects.message.attr.MessageAttributeRegistry;
import net.tysontheember.emberstextapi.immersivemessages.effects.preset.PresetDefinition;
import net.tysontheember.emberstextapi.immersivemessages.effects.preset.PresetRegistry;
import net.tysontheember.emberstextapi.immersivemessages.util.ColorParser;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkupParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkupParser.class);

    private static final Pattern TAG_PATTERN = Pattern.compile("<(/?)([a-zA-Z0-9_][a-zA-Z0-9_]*)((?:\\s+[a-zA-Z][a-zA-Z0-9]*(?:[=:](?:[\"'][^\"']*[\"']|[^\\s>/]+))?)*)(/?)>");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("([a-zA-Z][a-zA-Z0-9]*)(?:[=:](?:([\"'])([^\"']*)\\2|([^\\s>/]+)))?");
    private static final Pattern MESSAGE_TAG_PATTERN = Pattern.compile(
        "\\[([a-zA-Z0-9_]+)((?:\\s+(?:[a-zA-Z][a-zA-Z0-9]*(?:[=:](?:[\"'][^\"']*[\"']|[^\\s\\]]+))?|[^\\s\\]]+))*)\\s*\\]"
    );

    public static ParseResult parseFull(String markup) {
        if (markup == null || markup.isEmpty()) {
            return ParseResult.empty();
        }

        List<int[]> angleRegions = collectAngleRegions(markup);
        List<MessageEffect> messageEffects = new ArrayList<>();
        List<MessageAttribute> messageAttributes = new ArrayList<>();
        List<int[]> extractedRegions = new ArrayList<>();

        Matcher m = MESSAGE_TAG_PATTERN.matcher(markup);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            if (overlapsAngleRegion(start, end, angleRegions)) {
                LOGGER.warn("[{}] inside <...> region — bracket tags must appear outside <...> tags; rendering as literal text",
                        m.group(1));
                continue;
            }

            String tagName = m.group(1).toLowerCase();
            String attributes = m.group(2);
            String tagContent = (attributes == null || attributes.trim().isEmpty())
                    ? tagName : tagName + attributes;

            if (MessageEffectRegistry.isRegistered(tagName)) {
                try {
                    messageEffects.add(MessageEffectRegistry.parseTag(tagContent));
                    extractedRegions.add(new int[]{start, end});
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Failed to construct message effect '{}': {}", tagName, e.getMessage());
                }
            } else if (MessageAttributeRegistry.isRegistered(tagName)) {
                try {
                    messageAttributes.add(MessageAttributeRegistry.parseTag(tagContent));
                    extractedRegions.add(new int[]{start, end});
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Failed to construct message attribute '{}': {}", tagName, e.getMessage());
                }
            }
        }

        String stripped = stripRanges(markup, extractedRegions);
        List<TextSpan> spans = parse(stripped);
        return new ParseResult(spans, messageEffects, messageAttributes, stripped);
    }

    private static List<int[]> collectAngleRegions(String markup) {
        List<int[]> regions = new ArrayList<>();
        Matcher m = TAG_PATTERN.matcher(markup);
        while (m.find()) {
            regions.add(new int[]{m.start(), m.end()});
        }
        return regions;
    }

    private static boolean overlapsAngleRegion(int start, int end, List<int[]> regions) {
        for (int[] r : regions) {
            if (start < r[1] && end > r[0]) return true;
        }
        return false;
    }

    private static String stripRanges(String source, List<int[]> ranges) {
        if (ranges.isEmpty()) return source;
        StringBuilder sb = new StringBuilder(source.length());
        int cursor = 0;
        for (int[] r : ranges) {
            sb.append(source, cursor, r[0]);
            cursor = r[1];
        }
        sb.append(source, cursor, source.length());
        return sb.toString();
    }

    public static List<TextSpan> parse(String markup) {
        if (markup == null || markup.isEmpty()) {
            return Collections.emptyList();
        }

        LangSubstitution langSub = substituteLangTags(markup);
        markup = langSub.substituted();

        List<TextSpan> result = new ArrayList<>();
        Stack<TextSpan> styleStack = new Stack<>();

        Matcher matcher = TAG_PATTERN.matcher(markup);
        int lastEnd = 0;

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String content = markup.substring(lastEnd, matcher.start());
                if (!content.isEmpty()) {
                    TextSpan span = createSpanWithCurrentStyles(content, styleStack);
                    result.add(span);
                }
            }

            String isClosing = matcher.group(1);
            String tagName = matcher.group(2).toLowerCase();
            String attributes = matcher.group(3);
            String isSelfClosing = matcher.group(4);

            if ("/".equals(isSelfClosing)) {
                TextSpan currentStyle = styleStack.isEmpty() ? new TextSpan("") : styleStack.peek().inherit();
                applyTagToSpan(currentStyle, tagName, attributes);

                if ("item".equals(tagName) && currentStyle.getItemId() != null) {
                    TextSpan itemSpan = new TextSpan("");
                    itemSpan.item(currentStyle.getItemId(), currentStyle.getItemCount() != null ? currentStyle.getItemCount() : 1);
                    if (currentStyle.getItemOffsetX() != null || currentStyle.getItemOffsetY() != null) {
                        itemSpan.itemOffset(
                            currentStyle.getItemOffsetX() != null ? currentStyle.getItemOffsetX() : 0f,
                            currentStyle.getItemOffsetY() != null ? currentStyle.getItemOffsetY() : 0f
                        );
                    }
                    if (currentStyle.getItemNbt() != null) {
                        itemSpan.itemNbt(currentStyle.getItemNbt());
                    }
                    result.add(itemSpan);
                } else if ("entity".equals(tagName) && currentStyle.getEntityId() != null) {
                    TextSpan entitySpan = new TextSpan("");
                    entitySpan.entity(currentStyle.getEntityId(), currentStyle.getEntityScale() != null ? currentStyle.getEntityScale() : 1.0f);
                    if (currentStyle.getEntityOffsetX() != null || currentStyle.getEntityOffsetY() != null) {
                        entitySpan.entityOffset(
                            currentStyle.getEntityOffsetX() != null ? currentStyle.getEntityOffsetX() : 0f,
                            currentStyle.getEntityOffsetY() != null ? currentStyle.getEntityOffsetY() : 0f
                        );
                    }
                    if (currentStyle.getEntityYaw() != null || currentStyle.getEntityPitch() != null || currentStyle.getEntityRoll() != null) {
                        entitySpan.entityRotation(
                            currentStyle.getEntityYaw() != null ? currentStyle.getEntityYaw() : 45f,
                            currentStyle.getEntityPitch() != null ? currentStyle.getEntityPitch() : 0f,
                            currentStyle.getEntityRoll() != null ? currentStyle.getEntityRoll() : 0f
                        );
                    }
                    if (currentStyle.getEntityLighting() != null) {
                        entitySpan.entityLighting(currentStyle.getEntityLighting());
                    }
                    if (currentStyle.getEntitySpin() != null) {
                        entitySpan.entitySpin(currentStyle.getEntitySpin());
                    }
                    if (currentStyle.getEntityAnimation() != null) {
                        entitySpan.entityAnimation(currentStyle.getEntityAnimation());
                    }
                    if (currentStyle.getEntityNbt() != null) {
                        entitySpan.entityNbt(currentStyle.getEntityNbt());
                    }
                    result.add(entitySpan);
                }
            } else if ("/".equals(isClosing)) {

                if ("item".equals(tagName) && !styleStack.isEmpty()) {
                    TextSpan itemStyle = styleStack.peek();
                    if (itemStyle.getItemId() != null) {

                        TextSpan itemSpan = new TextSpan("");
                        itemSpan.item(itemStyle.getItemId(), itemStyle.getItemCount() != null ? itemStyle.getItemCount() : 1);
                        if (itemStyle.getItemOffsetX() != null || itemStyle.getItemOffsetY() != null) {
                            itemSpan.itemOffset(
                                itemStyle.getItemOffsetX() != null ? itemStyle.getItemOffsetX() : 0f,
                                itemStyle.getItemOffsetY() != null ? itemStyle.getItemOffsetY() : 0f
                            );
                        }
                        if (itemStyle.getItemNbt() != null) {
                            itemSpan.itemNbt(itemStyle.getItemNbt());
                        }
                        result.add(itemSpan);
                    }
                } else if ("entity".equals(tagName) && !styleStack.isEmpty()) {
                    TextSpan entityStyle = styleStack.peek();
                    if (entityStyle.getEntityId() != null) {

                        TextSpan entitySpan = new TextSpan("");
                        entitySpan.entity(entityStyle.getEntityId(), entityStyle.getEntityScale() != null ? entityStyle.getEntityScale() : 1.0f);
                        if (entityStyle.getEntityOffsetX() != null || entityStyle.getEntityOffsetY() != null) {
                            entitySpan.entityOffset(
                                entityStyle.getEntityOffsetX() != null ? entityStyle.getEntityOffsetX() : 0f,
                                entityStyle.getEntityOffsetY() != null ? entityStyle.getEntityOffsetY() : 0f
                            );
                        }
                        if (entityStyle.getEntityYaw() != null || entityStyle.getEntityPitch() != null || entityStyle.getEntityRoll() != null) {
                            entitySpan.entityRotation(
                                entityStyle.getEntityYaw() != null ? entityStyle.getEntityYaw() : 45f,
                                entityStyle.getEntityPitch() != null ? entityStyle.getEntityPitch() : 0f,
                                entityStyle.getEntityRoll() != null ? entityStyle.getEntityRoll() : 0f
                            );
                        }
                        if (entityStyle.getEntityAnimation() != null) {
                            entitySpan.entityAnimation(entityStyle.getEntityAnimation());
                        }
                        if (entityStyle.getEntityNbt() != null) {
                            entitySpan.entityNbt(entityStyle.getEntityNbt());
                        }
                        result.add(entitySpan);
                    }
                }
                if (!styleStack.isEmpty()) {
                    styleStack.pop();
                }
            } else {

                TextSpan currentStyle = styleStack.isEmpty() ? new TextSpan("") : styleStack.peek().inherit();
                applyTagToSpan(currentStyle, tagName, attributes);
                styleStack.push(currentStyle);
            }

            lastEnd = matcher.end();
        }

        if (lastEnd < markup.length()) {
            String content = markup.substring(lastEnd);
            if (!content.isEmpty()) {
                TextSpan span = createSpanWithCurrentStyles(content, styleStack);
                result.add(span);
            }
        }

        reinsertLangText(result, langSub.resolvedTexts());

        return result;
    }

    private static TextSpan createSpanWithCurrentStyles(String content, Stack<TextSpan> styleStack) {
        TextSpan span = new TextSpan(content);

        if (!styleStack.isEmpty()) {
            inheritStyles(span, styleStack.peek());
        }

        if (Boolean.TRUE.equals(span.getBold()) && span.getFont() != null) {
            String fontPath = span.getFont().getPath();
            if (!fontPath.endsWith("_bold") && FontAliasRegistry.hasBoldVariant(span.getFont())) {
                Identifier boldFont = Identifier.tryParse(span.getFont().getNamespace() + ":" + fontPath + "_bold");
                if (boldFont != null) {
                    span.font(boldFont);
                }
            }
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
        if (source.getEffects() != null) {
            for (var effect : source.getEffects()) {
                target.addEffect(effect);
            }
        }

        if (source.getObfuscateMode() != null) {
            target.obfuscate(source.getObfuscateMode(),
                source.getObfuscateSpeed() != null ? source.getObfuscateSpeed() : 1.0f);
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
    }

    private static void applyTagToSpan(TextSpan span, String tagName, String attributes) {
        Map<String, String> attrs = parseAttributes(attributes);

        switch (tagName) {
            case "bold", "b" -> span.bold(true);
            case "italic", "i" -> span.italic(true);
            case "underline", "u" -> span.underline(true);
            case "strikethrough", "s" -> span.strikethrough(true);
            case "obfuscated", "obf" -> span.obfuscated(true);

            case "color", "c" -> {
                String valueAttr = attrs.get("value");
                boolean isGradient = attrs.containsKey("col") || (valueAttr != null && valueAttr.contains(","));
                if (isGradient) {
                    String tagContent = buildEffectTag("color", attributes);
                    span.effect(tagContent);
                } else {
                    String colorValue = attrs.get("color");
                    if (colorValue == null) colorValue = valueAttr;
                    if (colorValue != null) span.color(colorValue);
                }
            }

            case "font" -> {

                String fontValue = attrs.get("id");
                if (fontValue == null) fontValue = attrs.get("value");
                if (fontValue == null) fontValue = attrs.get("font");
                if (fontValue == null) fontValue = attrs.get("name");
                if (fontValue != null) {

                    Identifier font = FontAliasRegistry.resolve(fontValue);
                    if (font != null) {
                        span.font(font);
                    } else {
                        LOGGER.debug("Unknown font name or invalid Identifier: '{}'", fontValue);
                    }
                }
            }

            case "grad", "gradient" -> {

                String tagContent = buildEffectTag("grad", attributes);
                span.effect(tagContent);
            }

            case "typewriter", "type" -> {

                String tagContent = buildEffectTag("typewriter", attributes);
                span.effect(tagContent);
            }

            case "shake" -> {
                String tagContent = buildEffectTag("shake", attributes);
                span.effect(tagContent);
            }

            case "wave" -> {

                String tagContent = buildEffectTag("wave", attributes);
                span.effect(tagContent);
            }

            case "shadow" -> {
                String tagContent = buildEffectTag("shadow", attributes);
                span.effect(tagContent);
            }

            case "fade" -> {
                String tagContent = buildEffectTag("fade", attributes);
                span.effect(tagContent);
            }

            case "item" -> {
                String itemId = attrs.getOrDefault("value", attrs.get("id"));
                String sizeStr = attrs.getOrDefault("size", attrs.getOrDefault("count", "1"));
                String offsetXStr = attrs.getOrDefault("offsetx", attrs.getOrDefault("x", "0"));
                String offsetYStr = attrs.getOrDefault("offsety", attrs.getOrDefault("y", "0"));
                String nbtStr = attrs.get("nbt");

                if (itemId != null) {
                    try {
                        int size = Integer.parseInt(sizeStr);
                        span.item(itemId, size);
                    } catch (NumberFormatException e) {
                        LOGGER.debug("Failed to parse item size '{}', using default: {}", sizeStr, e.getMessage());
                        span.item(itemId);
                    }

                    try {
                        float offsetX = Float.parseFloat(offsetXStr);
                        float offsetY = Float.parseFloat(offsetYStr);
                        if (offsetX != 0 || offsetY != 0) {
                            span.itemOffset(offsetX, offsetY);
                        }
                    } catch (NumberFormatException e) {
                        LOGGER.debug("Failed to parse item offset x='{}', y='{}': {}", offsetXStr, offsetYStr, e.getMessage());
                    }

                    if (nbtStr != null && !nbtStr.isEmpty()) {
                        span.itemNbt(nbtStr);
                    }
                }
            }

            case "entity" -> {
                String entityId = attrs.getOrDefault("value", attrs.get("id"));
                String scaleStr = attrs.getOrDefault("scale", "1.0");
                String offsetXStr = attrs.getOrDefault("offsetx", attrs.getOrDefault("x", "0"));
                String offsetYStr = attrs.getOrDefault("offsety", attrs.getOrDefault("y", "0"));
                String yawStr = attrs.getOrDefault("yaw", "45");
                String pitchStr = attrs.getOrDefault("pitch", "0");
                String rollStr = attrs.getOrDefault("roll", "0");
                String lightingStr = attrs.getOrDefault("lighting", attrs.getOrDefault("light", "15"));
                String spinStr = attrs.getOrDefault("spin", attrs.getOrDefault("rotate", null));
                String animation = attrs.getOrDefault("animation", attrs.getOrDefault("anim", "idle"));

                if (entityId != null) {
                    try {
                        float scale = Float.parseFloat(scaleStr);
                        span.entity(entityId, scale);
                    } catch (NumberFormatException e) {
                        LOGGER.debug("Failed to parse entity scale '{}', using default: {}", scaleStr, e.getMessage());
                        span.entity(entityId);
                    }

                    try {
                        float offsetX = Float.parseFloat(offsetXStr);
                        float offsetY = Float.parseFloat(offsetYStr);
                        if (offsetX != 0 || offsetY != 0) {
                            span.entityOffset(offsetX, offsetY);
                        }
                    } catch (NumberFormatException e) {
                        LOGGER.debug("Failed to parse entity offset x='{}', y='{}': {}", offsetXStr, offsetYStr, e.getMessage());
                    }

                    try {
                        float yaw = Float.parseFloat(yawStr);
                        float pitch = Float.parseFloat(pitchStr);
                        float roll = Float.parseFloat(rollStr);
                        span.entityRotation(yaw, pitch, roll);
                    } catch (NumberFormatException e) {
                        LOGGER.debug("Failed to parse entity rotation yaw='{}', pitch='{}', roll='{}': {}", yawStr, pitchStr, rollStr, e.getMessage());
                    }

                    try {
                        int lighting = Integer.parseInt(lightingStr);
                        span.entityLighting(lighting);
                    } catch (NumberFormatException e) {
                        LOGGER.debug("Failed to parse entity lighting '{}', using default: {}", lightingStr, e.getMessage());
                    }

                    if (spinStr != null) {
                        try {
                            float spin = Float.parseFloat(spinStr);
                            span.entitySpin(spin);
                        } catch (NumberFormatException e) {
                            LOGGER.debug("Failed to parse entity spin '{}': {}", spinStr, e.getMessage());
                        }
                    }

                    if (animation != null && !animation.isEmpty()) {
                        span.entityAnimation(animation);
                    }

                    String entityNbtStr = attrs.get("nbt");
                    if (entityNbtStr != null && !entityNbtStr.isEmpty()) {
                        span.entityNbt(entityNbtStr);
                    }
                }
            }

            case "rainbow", "rainb" -> {

                String tagContent = buildEffectTag("rainbow", attributes);
                span.effect(tagContent);
            }

            case "glitch" -> {
                String tagContent = buildEffectTag("glitch", attributes);
                span.effect(tagContent);
            }

            case "bounce" -> {
                String tagContent = buildEffectTag("bounce", attributes);
                span.effect(tagContent);
            }

            case "pulse" -> {
                String tagContent = buildEffectTag("pulse", attributes);
                span.effect(tagContent);
            }

            case "swing" -> {
                String tagContent = buildEffectTag("swing", attributes);
                span.effect(tagContent);
            }

            case "turb", "turbulence" -> {
                String tagContent = buildEffectTag("turb", attributes);
                span.effect(tagContent);
            }

            case "circle" -> {
                String tagContent = buildEffectTag("circle", attributes);
                span.effect(tagContent);
            }

            case "wiggle" -> {
                String tagContent = buildEffectTag("wiggle", attributes);
                span.effect(tagContent);
            }

            case "pend", "pendulum" -> {
                String tagContent = buildEffectTag("pend", attributes);
                span.effect(tagContent);
            }

            case "neon", "glow" -> {
                String tagContent = buildEffectTag("neon", attributes);
                span.effect(tagContent);
            }

            case "obfuscate" -> {
                String tagContent = buildEffectTag("obfuscate", attributes);
                span.effect(tagContent);
            }

            case "scroll" -> {
                String tagContent = buildEffectTag("scroll", attributes);
                span.effect(tagContent);
            }

            default -> {
                PresetDefinition preset = PresetRegistry.get(tagName);
                if (preset != null) {
                    applyPresetToSpan(span, preset, attrs);
                }
            }
        }
    }

    private static void applyPresetToSpan(TextSpan span, PresetDefinition preset, Map<String, String> attributes) {
        PresetDefinition.StyleOverrides styles = preset.getStyles();
        if (styles != null) {
            if (styles.bold() != null) span.bold(styles.bold());
            if (styles.italic() != null) span.italic(styles.italic());
            if (styles.underline() != null) span.underline(styles.underline());
            if (styles.strikethrough() != null) span.strikethrough(styles.strikethrough());
            if (styles.obfuscated() != null) span.obfuscated(styles.obfuscated());
            if (styles.color() != null) span.color(styles.color());
            if (styles.font() != null) {
                Identifier font = FontAliasRegistry.resolve(styles.font());
                if (font != null) {
                    span.font(font);
                } else {
                    LOGGER.debug("Unknown font name or invalid Identifier in preset styles.font: '{}'", styles.font());
                }
            }
        }

        for (PresetDefinition.EffectEntry entry : preset.getEffects()) {
            StringBuilder tagContent = new StringBuilder(entry.type());
            for (Map.Entry<String, Object> param : entry.params().entrySet()) {
                tagContent.append(' ').append(param.getKey()).append('=').append(param.getValue());
            }
            span.effect(tagContent.toString());
        }
    }

    private static String buildEffectTag(String effectName, String attributes) {
        if (attributes == null || attributes.trim().isEmpty()) {
            return effectName;
        }
        return effectName + attributes;
    }

    private static Map<String, String> parseAttributes(String attributeString) {
        Map<String, String> attributes = new HashMap<>();
        if (attributeString == null || attributeString.trim().isEmpty()) {
            return attributes;
        }

        Matcher matcher = ATTRIBUTE_PATTERN.matcher(attributeString);
        boolean first = true;
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);

            if (value == null) {
                if (first) {
                    attributes.put("value", key);
                } else {
                    attributes.put(key.toLowerCase(), "true");
                }
            } else {
                attributes.put(key.toLowerCase(), value);
            }
            first = false;
        }
        return attributes;
    }

    private static ImmersiveColor parseImmersiveColor(String value) {
        ImmersiveColor result = ColorParser.parseImmersiveColor(value);
        if (result == null && value != null && !value.trim().isEmpty()) {
            LOGGER.debug("Failed to parse color value '{}'", value);
        }
        return result;
    }

    public static String toPlainText(List<TextSpan> spans) {
        if (spans == null || spans.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (TextSpan span : spans) {
            result.append(span.getContent());
        }
        return result.toString();
    }

    public static List<TextSpan> fromPlainText(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new TextSpan(text));
    }

    public static Object[] extractDuration(String markup) {
        if (markup == null) {
            return new Object[]{-1f, ""};
        }
        Pattern durPattern = Pattern.compile("<dur:(\\d+(?:\\.\\d+)?)>");
        Matcher m = durPattern.matcher(markup);
        if (m.find()) {
            float dur = Float.parseFloat(m.group(1));
            String stripped = m.replaceFirst("").trim();
            return new Object[]{dur, stripped};
        }
        return new Object[]{-1f, markup};
    }

    public interface LangResolver {
        String resolve(String key, String[] args);
    }

    private static LangResolver langResolver = MarkupParser::defaultLangResolve;

    public static void setLangResolver(LangResolver resolver) {
        langResolver = resolver != null ? resolver : MarkupParser::defaultLangResolve;
    }

    private static String defaultLangResolve(String key, String[] args) {
        if (key == null || key.isEmpty()) return "";
        try {
            if (args == null || args.length == 0) {
                return Language.getInstance().getOrDefault(key);
            }
            return Component.translatable(key, (Object[]) args).getString();
        } catch (Throwable t) {
            return key;
        }
    }

    private static final Pattern LANG_TAG = Pattern.compile(
            "<lang(?::([^\\s>]+)|((?:\\s+[^>]+)?))>"
    );
    private static final char LANG_PLACEHOLDER_START = '';
    private static final char LANG_PLACEHOLDER_END = '';

    private record LangSubstitution(String substituted, List<String> resolvedTexts) {}

    private static LangSubstitution substituteLangTags(String markup) {
        if (markup == null || markup.indexOf("<lang") < 0) {
            return new LangSubstitution(markup, Collections.emptyList());
        }
        Matcher m = LANG_TAG.matcher(markup);
        StringBuilder out = new StringBuilder(markup.length());
        List<String> resolved = new ArrayList<>();
        int last = 0;
        while (m.find()) {
            out.append(markup, last, m.start());
            String key;
            String[] args = null;
            if (m.group(1) != null) {
                key = m.group(1);
            } else {
                Map<String, String> attrs = parseAttributes(m.group(2));
                key = attrs.getOrDefault("key", attrs.get("value"));
                String argsAttr = attrs.get("args");
                if (argsAttr != null && !argsAttr.isEmpty()) {
                    args = argsAttr.split(",");
                }
            }
            String text = langResolver.resolve(key == null ? "" : key, args);
            int idx = resolved.size();
            resolved.add(text == null ? "" : text);
            out.append(LANG_PLACEHOLDER_START).append(idx).append(LANG_PLACEHOLDER_END);
            last = m.end();
        }
        out.append(markup, last, markup.length());
        return new LangSubstitution(out.toString(), resolved);
    }

    private static void reinsertLangText(List<TextSpan> spans, List<String> resolvedTexts) {
        if (resolvedTexts.isEmpty()) return;
        for (TextSpan span : spans) {
            String content = span.getContent();
            if (content == null || content.isEmpty()) continue;
            if (content.indexOf(LANG_PLACEHOLDER_START) < 0) continue;
            StringBuilder rebuilt = new StringBuilder(content.length());
            int i = 0;
            while (i < content.length()) {
                char c = content.charAt(i);
                if (c == LANG_PLACEHOLDER_START) {
                    int end = content.indexOf(LANG_PLACEHOLDER_END, i + 1);
                    if (end < 0) {
                        rebuilt.append(c);
                        i++;
                        continue;
                    }
                    int idx;
                    try {
                        idx = Integer.parseInt(content.substring(i + 1, end));
                    } catch (NumberFormatException e) {
                        rebuilt.append(content, i, end + 1);
                        i = end + 1;
                        continue;
                    }
                    if (idx >= 0 && idx < resolvedTexts.size()) {
                        rebuilt.append(resolvedTexts.get(idx));
                    }
                    i = end + 1;
                } else {
                    rebuilt.append(c);
                    i++;
                }
            }
            span.setContent(rebuilt.toString());
        }
    }
}
