package net.tysontheember.emberstextapi.immersivemessages.api;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses markup text into TextSpan objects with nested styling support.
 * Supports tags like &lt;grad from=#ff0000 to=#00ff00&gt;, &lt;bold&gt;, &lt;shake&gt;, etc.
 */
public class MarkupParser {
    
    private static final Pattern TAG_PATTERN = Pattern.compile("<(/?)([a-zA-Z][a-zA-Z0-9]*)((?:\\s+[a-zA-Z][a-zA-Z0-9]*(?:[=:](?:[\"'][^\"']*[\"']|[^\\s>]+))?)*)>");
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("([a-zA-Z][a-zA-Z0-9]*)(?:[=:](?:([\"'])([^\"']*)\\2|([^\\s>]+)))?");
    
    /**
     * Parses markup text into a list of TextSpan objects.
     * Handles nested tags by creating a stack-based approach.
     */
    public static List<TextSpan> parse(String markup) {
        if (markup == null || markup.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<TextSpan> result = new ArrayList<>();
        Stack<TextSpan> styleStack = new Stack<>();
        
        Matcher matcher = TAG_PATTERN.matcher(markup);
        int lastEnd = 0;
        
        while (matcher.find()) {
            // Add any text before this tag
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
            
            if ("/".equals(isClosing)) {
                // Closing tag - pop from stack
                // Special case: if this is an item or entity closing tag and the last span on stack has one,
                // create the span now
                if ("item".equals(tagName) && !styleStack.isEmpty()) {
                    TextSpan itemStyle = styleStack.peek();
                    if (itemStyle.getItemId() != null) {
                        // Create an empty-content span for the item
                        TextSpan itemSpan = new TextSpan("");
                        itemSpan.item(itemStyle.getItemId(), itemStyle.getItemCount() != null ? itemStyle.getItemCount() : 1);
                        if (itemStyle.getItemOffsetX() != null || itemStyle.getItemOffsetY() != null) {
                            itemSpan.itemOffset(
                                itemStyle.getItemOffsetX() != null ? itemStyle.getItemOffsetX() : 0f,
                                itemStyle.getItemOffsetY() != null ? itemStyle.getItemOffsetY() : 0f
                            );
                        }
                        result.add(itemSpan);
                    }
                } else if ("entity".equals(tagName) && !styleStack.isEmpty()) {
                    TextSpan entityStyle = styleStack.peek();
                    if (entityStyle.getEntityId() != null) {
                        // Create an empty-content span for the entity
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
                        result.add(entitySpan);
                    }
                }
                if (!styleStack.isEmpty()) {
                    styleStack.pop();
                }
            } else {
                // Opening tag - push new style to stack
                TextSpan currentStyle = styleStack.isEmpty() ? new TextSpan("") : styleStack.peek().inherit();
                applyTagToSpan(currentStyle, tagName, attributes);
                styleStack.push(currentStyle);
            }
            
            lastEnd = matcher.end();
        }
        
        // Add any remaining text
        if (lastEnd < markup.length()) {
            String content = markup.substring(lastEnd);
            if (!content.isEmpty()) {
                TextSpan span = createSpanWithCurrentStyles(content, styleStack);
                result.add(span);
            }
        }
        
        return result;
    }
    
    private static TextSpan createSpanWithCurrentStyles(String content, Stack<TextSpan> styleStack) {
        TextSpan span = new TextSpan(content);
        
        // Apply all styles from the stack (inheritance)
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
        // OLD gradient system removed - use new GradientEffect instead (v2.1.0)
        // Inherit effects (v2.1.0)
        if (source.getEffects() != null) {
            for (var effect : source.getEffects()) {
                target.addEffect(effect);
            }
        }
        // DON'T inherit typewriter - it should be a container effect, not per-span
        // Typewriter will be handled at the message level via ImmersiveMessage.typewriter flag
        // if (source.getTypewriterSpeed() != null) {
        //     target.typewriter(source.getTypewriterSpeed(), 
        //         source.getTypewriterCenter() != null ? source.getTypewriterCenter() : false);
        // }
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
            target.obfuscate(source.getObfuscateMode(), 
                source.getObfuscateSpeed() != null ? source.getObfuscateSpeed() : 1.0f);
        }
        if (source.getBackgroundColor() != null) {
            target.background(source.getBackgroundColor());
        }
        if (source.getBackgroundGradient() != null) {
            target.backgroundGradient(source.getBackgroundGradient());
        }
        
        // Inherit item properties
        if (source.getItemId() != null) {
            target.item(source.getItemId(), source.getItemCount() != null ? source.getItemCount() : 1);
            if (source.getItemOffsetX() != null || source.getItemOffsetY() != null) {
                target.itemOffset(
                    source.getItemOffsetX() != null ? source.getItemOffsetX() : 0f,
                    source.getItemOffsetY() != null ? source.getItemOffsetY() : 0f
                );
            }
        }
        
        // Inherit global message attributes (these typically aren't inherited, but just in case)
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
            target.globalTypewriter(source.getGlobalTypewriterSpeed(),
                source.getGlobalTypewriterCenter() != null ? source.getGlobalTypewriterCenter() : false);
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
                // Always use new GradientEffect (v2.1.0)
                String tagContent = buildEffectTag("grad", attributes);
                span.effect(tagContent);
            }
            
            case "typewriter", "type" -> {
                // Typewriter is a GLOBAL message attribute (container effect)
                String speedStr = attrs.getOrDefault("speed", "1.0");
                String centerStr = attrs.get("center");
                try {
                    float speed = Float.parseFloat(speedStr);
                    boolean center = "true".equalsIgnoreCase(centerStr);
                    span.globalTypewriter(speed, center);
                } catch (NumberFormatException ignored) {}
            }
            
            case "shake" -> {
                // Check if using old ShakeType system (deprecated)
                String typeStr = attrs.get("type");

                if (typeStr != null) {
                    // OLD SYSTEM: Map type parameter to new effects
                    String effectName = switch (typeStr.toLowerCase()) {
                        case "wave" -> "wave";
                        case "circle" -> "circle";
                        default -> "shake"; // "random" or any other value
                    };
                    String tagContent = buildEffectTag(effectName, attributes);
                    span.effect(tagContent);
                } else {
                    // NEW SYSTEM: Use ShakeEffect directly (v2.1.0)
                    String tagContent = buildEffectTag("shake", attributes);
                    span.effect(tagContent);
                }
            }
            
            case "charshake" -> {
                // DEPRECATED: All new effects are per-character by default
                // Map old charshake to new effect system
                String typeStr = attrs.getOrDefault("type", "random");

                String effectName = switch (typeStr.toLowerCase()) {
                    case "wave" -> "wave";
                    case "circle" -> "circle";
                    default -> "shake"; // "random" or any other value
                };

                String tagContent = buildEffectTag(effectName, attributes);
                span.effect(tagContent);
            }
            
            case "wave" -> {
                // Use new WaveEffect (v2.1.0) instead of charShake-based wave
                String tagContent = buildEffectTag("wave", attributes);
                span.effect(tagContent);

                // OLD (deprecated): charShake-based wave - kept here as reference
                // String amplitudeStr = attrs.getOrDefault("amplitude", "1.0");
                // String speedStr = attrs.getOrDefault("speed", "1.0");
                // String wavelengthStr = attrs.getOrDefault("wavelength", "1.0");
                // try {
                //     float amplitude = Float.parseFloat(amplitudeStr);
                //     if (speedStr != null && wavelengthStr != null) {
                //         float speed = Float.parseFloat(speedStr) * -1;
                //         float wavelength = Float.parseFloat(wavelengthStr);
                //         span.charShake(ShakeType.WAVE, amplitude, speed, wavelength);
                //     } else if (speedStr != null) {
                //         float speed = Float.parseFloat(speedStr)* -1;
                //         span.charShake(ShakeType.WAVE, amplitude, speed);
                //     } else {
                //         span.charShake(ShakeType.WAVE, amplitude);
                //     }
                // } catch (NumberFormatException ignored) {}
            }
            
            case "obfuscate", "scramble" -> {
                String modeStr = attrs.getOrDefault("mode", "random");
                String speedStr = attrs.getOrDefault("speed", "1.0");
                try {
                    ObfuscateMode mode = ObfuscateMode.valueOf(modeStr.toUpperCase());
                    float speed = Float.parseFloat(speedStr);
                    span.obfuscate(mode, speed);
                } catch (Exception ignored) {}
            }
            
            // Global message attributes
            case "background", "bg" -> {
                String colorStr = attrs.get("color");
                String borderColorStr = attrs.get("bordercolor");
                String borderStartStr = attrs.get("borderstart");
                String borderEndStr = attrs.get("borderend");
                
                if (colorStr != null) {
                    ImmersiveColor bgColor = parseImmersiveColor(colorStr);
                    if (bgColor != null) {
                        span.globalBackgroundColor(bgColor);
                    }
                } else {
                    span.globalBackground(true);
                }
                
                if (borderColorStr != null) {
                    ImmersiveColor borderColor = parseImmersiveColor(borderColorStr);
                    if (borderColor != null) {
                        span.globalBorder(borderColor, borderColor);
                    }
                }
                
                if (borderStartStr != null && borderEndStr != null) {
                    ImmersiveColor borderStart = parseImmersiveColor(borderStartStr);
                    ImmersiveColor borderEnd = parseImmersiveColor(borderEndStr);
                    if (borderStart != null && borderEnd != null) {
                        span.globalBorder(borderStart, borderEnd);
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
                    }
                }
            }
            
            case "scale" -> {
                String scaleStr = attrs.getOrDefault("value", "1.0");
                try {
                    float scale = Float.parseFloat(scaleStr);
                    span.globalScale(scale);
                } catch (NumberFormatException ignored) {}
            }
            
            case "offset" -> {
                String xStr = attrs.getOrDefault("x", "0.0");
                String yStr = attrs.getOrDefault("y", "0.0");
                try {
                    float x = Float.parseFloat(xStr);
                    float y = Float.parseFloat(yStr);
                    span.globalOffset(x, y);
                } catch (NumberFormatException ignored) {}
            }
            
            case "anchor" -> {
                String anchorStr = attrs.getOrDefault("value", "TOP_CENTER");
                try {
                    TextAnchor anchor = TextAnchor.valueOf(anchorStr.toUpperCase());
                    span.globalAnchor(anchor);
                } catch (IllegalArgumentException ignored) {}
            }
            
            case "align" -> {
                String alignStr = attrs.getOrDefault("value", "TOP_CENTER");
                try {
                    TextAnchor align = TextAnchor.valueOf(alignStr.toUpperCase());
                    span.globalAlign(align);
                } catch (IllegalArgumentException ignored) {}
            }
            
            case "shadow" -> {
                // Check if this is using new ShadowEffect parameters (x, y, c, r, g, b, a)
                boolean isNewEffect = attrs.containsKey("x") || attrs.containsKey("y") ||
                                     attrs.containsKey("c") || attrs.containsKey("r") ||
                                     attrs.containsKey("g") || attrs.containsKey("b");

                if (isNewEffect) {
                    // Use new ShadowEffect (v2.1.0)
                    String tagContent = buildEffectTag("shadow", attributes);
                    span.effect(tagContent);
                } else {
                    // OLD system: global shadow enable/disable
                    String shadowStr = attrs.getOrDefault("value", "true");
                    boolean shadow = "true".equalsIgnoreCase(shadowStr);
                    span.globalShadow(shadow);
                }
            }

            case "fade" -> {
                // Check if this is using new FadeEffect parameters (a, f, w)
                boolean isNewEffect = attrs.containsKey("a") || attrs.containsKey("f") ||
                                     attrs.containsKey("w");

                if (isNewEffect) {
                    // Use new FadeEffect (v2.1.0)
                    String tagContent = buildEffectTag("fade", attributes);
                    span.effect(tagContent);
                } else {
                    // OLD system: global fade in/out
                    String inStr = attrs.get("in");
                    String outStr = attrs.get("out");

                    if (inStr != null) {
                        try {
                            int inTicks = Integer.parseInt(inStr);
                            span.globalFadeIn(inTicks);
                        } catch (NumberFormatException ignored) {}
                    }

                    if (outStr != null) {
                        try {
                            int outTicks = Integer.parseInt(outStr);
                            span.globalFadeOut(outTicks);
                        } catch (NumberFormatException ignored) {}
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
                    } catch (NumberFormatException ignored) {
                        span.item(itemId);
                    }
                    
                    // Parse offsets
                    try {
                        float offsetX = Float.parseFloat(offsetXStr);
                        float offsetY = Float.parseFloat(offsetYStr);
                        if (offsetX != 0 || offsetY != 0) {
                            span.itemOffset(offsetX, offsetY);
                        }
                    } catch (NumberFormatException ignored) {}
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
                    } catch (NumberFormatException ignored) {
                        span.entity(entityId);
                    }
                    
                    // Parse offsets
                    try {
                        float offsetX = Float.parseFloat(offsetXStr);
                        float offsetY = Float.parseFloat(offsetYStr);
                        if (offsetX != 0 || offsetY != 0) {
                            span.entityOffset(offsetX, offsetY);
                        }
                    } catch (NumberFormatException ignored) {}
                    
                    // Parse rotation
                    try {
                        float yaw = Float.parseFloat(yawStr);
                        float pitch = Float.parseFloat(pitchStr);
                        span.entityRotation(yaw, pitch);
                    } catch (NumberFormatException ignored) {}
                    
                    // Set animation
                    if (animation != null && !animation.isEmpty()) {
                        span.entityAnimation(animation);
                    }
                }
            }

            // NEW: Visual effects (v2.1.0)
            // These are recognized effect names that get delegated to the effect system
            case "rainbow", "rainb" -> {
                // Build tag content from effect name and attributes
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

            case "scroll" -> {
                String tagContent = buildEffectTag("scroll", attributes);
                span.effect(tagContent);
            }

            case "neon" -> {
                String tagContent = buildEffectTag("neon", attributes);
                span.effect(tagContent);
            }

            // Note: "wave" already exists above as charShake-based effect
            // To use the new WaveEffect, users can use <effect>wave</effect> or we keep the old for compatibility
            // Note: "shake" already exists above as charShake-based effect
            // To use the new ShakeEffect, users can use <effect>shake</effect> or we keep the old for compatibility
            // Note: "fade" and "shadow" handle both old and new systems dynamically based on parameters
        }
    }

    /**
     * Helper method to build effect tag content from tag name and attributes.
     * Reconstructs the tag format expected by EffectRegistry.parseTag().
     */
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
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);

            // If value is null, treat it as a boolean flag (true)
            if (value == null) {
                attributes.put(key.toLowerCase(), "true");
            } else {
                attributes.put(key.toLowerCase(), value);
            }
        }

        return attributes;
    }
    
    private static ImmersiveColor parseImmersiveColor(String value) {
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
        } catch (NumberFormatException ignored) {}
        
        // Try to parse as named color
        ChatFormatting fmt = ChatFormatting.getByName(value);
        if (fmt != null && fmt.getColor() != null) {
            return new ImmersiveColor(0xFF000000 | fmt.getColor());
        }
        
        // Try to parse as TextColor
        TextColor parsed = TextColor.parseColor(value);
        if (parsed != null) {
            int c = parsed.getValue();
            if ((c & 0xFF000000) == 0) c |= 0xFF000000;
            return new ImmersiveColor(c);
        }
        
        return null;
    }
    
    /**
     * Converts a list of spans back to a plain text string (removes markup).
     */
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
    
    /**
     * Utility method to create a simple single-span list from plain text.
     */
    public static List<TextSpan> fromPlainText(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new TextSpan(text));
    }
}