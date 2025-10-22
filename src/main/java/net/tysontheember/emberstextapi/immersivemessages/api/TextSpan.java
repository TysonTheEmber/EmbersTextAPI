package net.tysontheember.emberstextapi.immersivemessages.api;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;

/**
 * Represents a styled span of text with its own attributes and effects.
 * This replaces the global styling approach in favor of per-span control.
 */
public class TextSpan {
    private final String content;
    
    // Basic text styling
    private TextColor color;
    private Boolean bold;
    private Boolean italic;
    private Boolean underline;
    private Boolean strikethrough;
    private Boolean obfuscated;
    private ResourceLocation font;
    
    // Gradient settings
    private TextColor[] gradientColors;
    
    // Typewriter effect
    private Float typewriterSpeed;
    private Boolean typewriterCenter;
    
    // Shake effects
    private ShakeType shakeType;
    private Float shakeAmplitude;
    private Float shakeSpeed;
    private Float shakeWavelength;
    
    // Character-level shake
    private ShakeType charShakeType;
    private Float charShakeAmplitude;
    private Float charShakeSpeed;
    private Float charShakeWavelength;
    
    // Obfuscation settings
    private ObfuscateMode obfuscateMode;
    private Float obfuscateSpeed;
    
    // Per-span fade effects
    private Integer fadeInTicks;
    private Integer fadeOutTicks;
    
    // Background override (per-span backgrounds)
    private Boolean hasBackground;
    private ImmersiveColor backgroundColor;
    private ImmersiveColor[] backgroundGradient;
    
    // Global message attributes (only used by top-level/wrapper spans)
    private Boolean globalBackground;
    private ImmersiveColor globalBackgroundColor;
    private ImmersiveColor[] globalBackgroundGradient;
    private ImmersiveColor globalBorderStart;
    private ImmersiveColor globalBorderEnd;
    private Float globalXOffset;
    private Float globalYOffset;
    private TextAnchor globalAnchor;
    private TextAnchor globalAlign;
    private Float globalScale;
    private Boolean globalShadow;
    private Integer globalFadeInTicks;
    private Integer globalFadeOutTicks;
    private Float globalTypewriterSpeed;
    private Boolean globalTypewriterCenter;
    
    public TextSpan(String content) {
        this.content = content != null ? content : "";
    }
    
    // Copy constructor for building from existing spans
    public TextSpan(TextSpan other) {
        this.content = other.content;
        this.color = other.color;
        this.bold = other.bold;
        this.italic = other.italic;
        this.underline = other.underline;
        this.strikethrough = other.strikethrough;
        this.obfuscated = other.obfuscated;
        this.font = other.font;
        this.gradientColors = other.gradientColors != null ? other.gradientColors.clone() : null;
        this.typewriterSpeed = other.typewriterSpeed;
        this.typewriterCenter = other.typewriterCenter;
        this.shakeType = other.shakeType;
        this.shakeAmplitude = other.shakeAmplitude;
        this.shakeSpeed = other.shakeSpeed;
        this.shakeWavelength = other.shakeWavelength;
        this.charShakeType = other.charShakeType;
        this.charShakeAmplitude = other.charShakeAmplitude;
        this.charShakeSpeed = other.charShakeSpeed;
        this.charShakeWavelength = other.charShakeWavelength;
        this.obfuscateMode = other.obfuscateMode;
        this.obfuscateSpeed = other.obfuscateSpeed;
        this.fadeInTicks = other.fadeInTicks;
        this.fadeOutTicks = other.fadeOutTicks;
        this.hasBackground = other.hasBackground;
        this.backgroundColor = other.backgroundColor;
        this.backgroundGradient = other.backgroundGradient != null ? other.backgroundGradient.clone() : null;
        this.globalBackground = other.globalBackground;
        this.globalBackgroundColor = other.globalBackgroundColor;
        this.globalBackgroundGradient = other.globalBackgroundGradient != null ? other.globalBackgroundGradient.clone() : null;
        this.globalBorderStart = other.globalBorderStart;
        this.globalBorderEnd = other.globalBorderEnd;
        this.globalXOffset = other.globalXOffset;
        this.globalYOffset = other.globalYOffset;
        this.globalAnchor = other.globalAnchor;
        this.globalAlign = other.globalAlign;
        this.globalScale = other.globalScale;
        this.globalShadow = other.globalShadow;
        this.globalFadeInTicks = other.globalFadeInTicks;
        this.globalFadeOutTicks = other.globalFadeOutTicks;
        this.globalTypewriterSpeed = other.globalTypewriterSpeed;
        this.globalTypewriterCenter = other.globalTypewriterCenter;
    }
    
    // Getters
    public String getContent() { return content; }
    public TextColor getColor() { return color; }
    public Boolean getBold() { return bold; }
    public Boolean getItalic() { return italic; }
    public Boolean getUnderline() { return underline; }
    public Boolean getStrikethrough() { return strikethrough; }
    public Boolean getObfuscated() { return obfuscated; }
    public ResourceLocation getFont() { return font; }
    public TextColor[] getGradientColors() { return gradientColors; }
    public Float getTypewriterSpeed() { return typewriterSpeed; }
    public Boolean getTypewriterCenter() { return typewriterCenter; }
    public ShakeType getShakeType() { return shakeType; }
    public Float getShakeAmplitude() { return shakeAmplitude; }
    public Float getShakeSpeed() { return shakeSpeed; }
    public Float getShakeWavelength() { return shakeWavelength; }
    public ShakeType getCharShakeType() { return charShakeType; }
    public Float getCharShakeAmplitude() { return charShakeAmplitude; }
    public Float getCharShakeSpeed() { return charShakeSpeed; }
    public Float getCharShakeWavelength() { return charShakeWavelength; }
    public ObfuscateMode getObfuscateMode() { return obfuscateMode; }
    public Float getObfuscateSpeed() { return obfuscateSpeed; }
    public Integer getFadeInTicks() { return fadeInTicks; }
    public Integer getFadeOutTicks() { return fadeOutTicks; }
    public Boolean getHasBackground() { return hasBackground; }
    public ImmersiveColor getBackgroundColor() { return backgroundColor; }
    public ImmersiveColor[] getBackgroundGradient() { return backgroundGradient; }
    
    // Global message attribute getters
    public Boolean getGlobalBackground() { return globalBackground; }
    public ImmersiveColor getGlobalBackgroundColor() { return globalBackgroundColor; }
    public ImmersiveColor[] getGlobalBackgroundGradient() { return globalBackgroundGradient; }
    public ImmersiveColor getGlobalBorderStart() { return globalBorderStart; }
    public ImmersiveColor getGlobalBorderEnd() { return globalBorderEnd; }
    public Float getGlobalXOffset() { return globalXOffset; }
    public Float getGlobalYOffset() { return globalYOffset; }
    public TextAnchor getGlobalAnchor() { return globalAnchor; }
    public TextAnchor getGlobalAlign() { return globalAlign; }
    public Float getGlobalScale() { return globalScale; }
    public Boolean getGlobalShadow() { return globalShadow; }
    public Integer getGlobalFadeInTicks() { return globalFadeInTicks; }
    public Integer getGlobalFadeOutTicks() { return globalFadeOutTicks; }
    public Float getGlobalTypewriterSpeed() { return globalTypewriterSpeed; }
    public Boolean getGlobalTypewriterCenter() { return globalTypewriterCenter; }
    
    // Builder-style setters
    public TextSpan color(TextColor color) { this.color = color; return this; }
    public TextSpan color(int rgb) { return color(TextColor.fromRgb(rgb)); }
    public TextSpan color(String value) {
        ChatFormatting fmt = ChatFormatting.getByName(value);
        if (fmt != null) return color(TextColor.fromLegacyFormat(fmt));
        TextColor parsed = TextColor.parseColor(value);
        if (parsed != null) this.color = parsed;
        return this;
    }
    
    public TextSpan bold(boolean bold) { this.bold = bold; return this; }
    public TextSpan italic(boolean italic) { this.italic = italic; return this; }
    public TextSpan underline(boolean underline) { this.underline = underline; return this; }
    public TextSpan strikethrough(boolean strikethrough) { this.strikethrough = strikethrough; return this; }
    public TextSpan obfuscated(boolean obfuscated) { this.obfuscated = obfuscated; return this; }
    public TextSpan font(ResourceLocation font) { this.font = font; return this; }
    
    public TextSpan gradient(TextColor... colors) {
        if (colors != null && colors.length >= 2) {
            this.gradientColors = colors.clone();
        }
        return this;
    }
    
    public TextSpan gradient(int... rgbs) {
        if (rgbs != null && rgbs.length >= 2) {
            TextColor[] colors = new TextColor[rgbs.length];
            for (int i = 0; i < rgbs.length; i++) {
                colors[i] = TextColor.fromRgb(rgbs[i]);
            }
            this.gradientColors = colors;
        }
        return this;
    }
    
    public TextSpan gradient(String... values) {
        if (values != null && values.length >= 2) {
            TextColor[] colors = new TextColor[values.length];
            boolean allValid = true;
            for (int i = 0; i < values.length; i++) {
                ChatFormatting fmt = ChatFormatting.getByName(values[i]);
                colors[i] = fmt != null ? TextColor.fromLegacyFormat(fmt) : TextColor.parseColor(values[i]);
                if (colors[i] == null) {
                    allValid = false;
                    break;
                }
            }
            if (allValid) {
                this.gradientColors = colors;
            }
        }
        return this;
    }
    
    public TextSpan typewriter(float speed) { return typewriter(speed, false); }
    public TextSpan typewriter(float speed, boolean center) {
        this.typewriterSpeed = speed;
        this.typewriterCenter = center;
        return this;
    }
    
    public TextSpan shake(ShakeType type, float amplitude) {
        this.shakeType = type;
        this.shakeAmplitude = amplitude;
        return this;
    }
    
    public TextSpan shake(ShakeType type, float amplitude, float speed) {
        this.shakeType = type;
        this.shakeAmplitude = amplitude;
        this.shakeSpeed = speed;
        return this;
    }
    
    public TextSpan shake(ShakeType type, float amplitude, float speed, float wavelength) {
        this.shakeType = type;
        this.shakeAmplitude = amplitude;
        this.shakeSpeed = speed;
        this.shakeWavelength = wavelength;
        return this;
    }
    
    public TextSpan charShake(ShakeType type, float amplitude) {
        this.charShakeType = type;
        this.charShakeAmplitude = amplitude;
        return this;
    }
    
    public TextSpan charShake(ShakeType type, float amplitude, float speed) {
        this.charShakeType = type;
        this.charShakeAmplitude = amplitude;
        this.charShakeSpeed = speed;
        return this;
    }
    
    public TextSpan charShake(ShakeType type, float amplitude, float speed, float wavelength) {
        this.charShakeType = type;
        this.charShakeAmplitude = amplitude;
        this.charShakeSpeed = speed;
        this.charShakeWavelength = wavelength;
        return this;
    }
    
    public TextSpan obfuscate(ObfuscateMode mode, float speed) {
        this.obfuscateMode = mode;
        this.obfuscateSpeed = speed;
        return this;
    }
    
    public TextSpan fadeIn(int ticks) {
        this.fadeInTicks = ticks >= 0 ? ticks : null;
        return this;
    }
    
    public TextSpan fadeOut(int ticks) {
        this.fadeOutTicks = ticks >= 0 ? ticks : null;
        return this;
    }
    
    public TextSpan fade(int inTicks, int outTicks) {
        this.fadeInTicks = inTicks >= 0 ? inTicks : null;
        this.fadeOutTicks = outTicks >= 0 ? outTicks : null;
        return this;
    }
    
    public TextSpan background(ImmersiveColor color) {
        this.hasBackground = true;
        this.backgroundColor = color;
        return this;
    }
    
    public TextSpan backgroundGradient(ImmersiveColor... colors) {
        if (colors != null && colors.length >= 2) {
            this.hasBackground = true;
            this.backgroundGradient = colors.clone();
        }
        return this;
    }
    
    // Global message attribute setters
    public TextSpan globalBackground(boolean enabled) {
        this.globalBackground = enabled;
        return this;
    }
    
    public TextSpan globalBackgroundColor(ImmersiveColor color) {
        this.globalBackgroundColor = color;
        this.globalBackground = true;
        return this;
    }
    
    public TextSpan globalBackgroundGradient(ImmersiveColor... colors) {
        if (colors != null && colors.length >= 2) {
            this.globalBackgroundGradient = colors.clone();
            this.globalBackground = true;
        }
        return this;
    }
    
    public TextSpan globalBorder(ImmersiveColor start, ImmersiveColor end) {
        this.globalBorderStart = start;
        this.globalBorderEnd = end;
        this.globalBackground = true;
        return this;
    }
    
    public TextSpan globalOffset(float x, float y) {
        this.globalXOffset = x;
        this.globalYOffset = y;
        return this;
    }
    
    public TextSpan globalAnchor(TextAnchor anchor) {
        this.globalAnchor = anchor;
        return this;
    }
    
    public TextSpan globalAlign(TextAnchor align) {
        this.globalAlign = align;
        return this;
    }
    
    public TextSpan globalScale(float scale) {
        this.globalScale = scale;
        return this;
    }
    
    public TextSpan globalShadow(boolean shadow) {
        this.globalShadow = shadow;
        return this;
    }
    
    public TextSpan globalFadeIn(int ticks) {
        this.globalFadeInTicks = ticks >= 0 ? ticks : null;
        return this;
    }
    
    public TextSpan globalFadeOut(int ticks) {
        this.globalFadeOutTicks = ticks >= 0 ? ticks : null;
        return this;
    }
    
    public TextSpan globalTypewriter(float speed, boolean center) {
        this.globalTypewriterSpeed = speed;
        this.globalTypewriterCenter = center;
        return this;
    }
    
    /**
     * Checks if this span has any custom styling applied.
     * Used to determine if span-specific rendering is needed.
     */
    public boolean hasCustomStyling() {
        return color != null || bold != null || italic != null || underline != null ||
               strikethrough != null || obfuscated != null || font != null ||
               gradientColors != null || typewriterSpeed != null ||
               shakeType != null || shakeSpeed != null || charShakeType != null || charShakeSpeed != null ||
               obfuscateMode != null || hasBackground != null || fadeInTicks != null || fadeOutTicks != null ||
               hasGlobalAttributes();
    }
    
    /**
     * Checks if this span has global message attributes.
     */
    public boolean hasGlobalAttributes() {
        return globalBackground != null || globalBackgroundColor != null || 
               globalBackgroundGradient != null || globalBorderStart != null || globalBorderEnd != null ||
               globalXOffset != null || globalYOffset != null || globalAnchor != null || globalAlign != null ||
               globalScale != null || globalShadow != null || globalFadeInTicks != null || globalFadeOutTicks != null ||
               globalTypewriterSpeed != null || globalTypewriterCenter != null;
    }
    
    /**
     * Creates a new TextSpan that inherits attributes from this span
     * but can be overridden by child attributes.
     */
    public TextSpan inherit() {
        return new TextSpan(this);
    }
    
    @Override
    public String toString() {
        return "TextSpan{content='" + content + "', styling=" + hasCustomStyling() + "}";
    }
    
    // Serialization support for network transmission
    public void encode(net.minecraft.network.FriendlyByteBuf buf) {
        buf.writeUtf(content);
        
        // Encode style properties (using null-safe booleans)
        buf.writeBoolean(bold != null && bold);
        buf.writeBoolean(italic != null && italic);
        buf.writeBoolean(underline != null && underline);
        buf.writeBoolean(strikethrough != null && strikethrough);
        buf.writeBoolean(obfuscated != null && obfuscated);
        
        buf.writeBoolean(color != null);
        if (color != null) {
            buf.writeInt(color.getValue());
        }
        buf.writeBoolean(font != null);
        if (font != null) {
            buf.writeResourceLocation(font);
        }
        
        // Encode gradient colors
        buf.writeBoolean(gradientColors != null);
        if (gradientColors != null) {
            buf.writeVarInt(gradientColors.length);
            for (net.minecraft.network.chat.TextColor gradientColor : gradientColors) {
                buf.writeInt(gradientColor.getValue());
            }
        }
        
        // Encode other effect properties
        buf.writeBoolean(typewriterSpeed != null);
        if (typewriterSpeed != null) {
            buf.writeFloat(typewriterSpeed);
            buf.writeBoolean(typewriterCenter != null && typewriterCenter);
        }
        
        buf.writeBoolean(shakeType != null);
        if (shakeType != null) {
            buf.writeEnum(shakeType);
            buf.writeFloat(shakeAmplitude != null ? shakeAmplitude : 0f);
            buf.writeBoolean(shakeSpeed != null);
            if (shakeSpeed != null) {
                buf.writeFloat(shakeSpeed);
            }
            buf.writeBoolean(shakeWavelength != null);
            if (shakeWavelength != null) {
                buf.writeFloat(shakeWavelength);
            }
        }
        
        buf.writeBoolean(charShakeType != null);
        if (charShakeType != null) {
            buf.writeEnum(charShakeType);
            buf.writeFloat(charShakeAmplitude != null ? charShakeAmplitude : 0f);
            buf.writeBoolean(charShakeSpeed != null);
            if (charShakeSpeed != null) {
                buf.writeFloat(charShakeSpeed);
            }
            buf.writeBoolean(charShakeWavelength != null);
            if (charShakeWavelength != null) {
                buf.writeFloat(charShakeWavelength);
            }
        }
        
        buf.writeBoolean(obfuscateMode != null);
        if (obfuscateMode != null) {
            buf.writeEnum(obfuscateMode);
            buf.writeFloat(obfuscateSpeed != null ? obfuscateSpeed : 1f);
        }
        
        buf.writeBoolean(hasBackground != null && hasBackground);
        buf.writeBoolean(backgroundColor != null);
        if (backgroundColor != null) {
            buf.writeInt(backgroundColor.getARGB());
        }
        
        buf.writeBoolean(backgroundGradient != null);
        if (backgroundGradient != null) {
            buf.writeVarInt(backgroundGradient.length);
            for (ImmersiveColor bgColor : backgroundGradient) {
                buf.writeInt(bgColor.getARGB());
            }
        }
        
        // Encode global message attributes
        buf.writeBoolean(globalBackground != null && globalBackground);
        buf.writeBoolean(globalBackgroundColor != null);
        if (globalBackgroundColor != null) {
            buf.writeInt(globalBackgroundColor.getARGB());
        }
        
        buf.writeBoolean(globalBackgroundGradient != null);
        if (globalBackgroundGradient != null) {
            buf.writeVarInt(globalBackgroundGradient.length);
            for (ImmersiveColor bgColor : globalBackgroundGradient) {
                buf.writeInt(bgColor.getARGB());
            }
        }
        
        buf.writeBoolean(globalBorderStart != null);
        if (globalBorderStart != null) {
            buf.writeInt(globalBorderStart.getARGB());
        }
        buf.writeBoolean(globalBorderEnd != null);
        if (globalBorderEnd != null) {
            buf.writeInt(globalBorderEnd.getARGB());
        }
        
        buf.writeBoolean(globalXOffset != null);
        if (globalXOffset != null) {
            buf.writeFloat(globalXOffset);
        }
        buf.writeBoolean(globalYOffset != null);
        if (globalYOffset != null) {
            buf.writeFloat(globalYOffset);
        }
        
        buf.writeBoolean(globalAnchor != null);
        if (globalAnchor != null) {
            buf.writeEnum(globalAnchor);
        }
        buf.writeBoolean(globalAlign != null);
        if (globalAlign != null) {
            buf.writeEnum(globalAlign);
        }
        
        buf.writeBoolean(globalScale != null);
        if (globalScale != null) {
            buf.writeFloat(globalScale);
        }
        
        buf.writeBoolean(globalShadow != null);
        if (globalShadow != null) {
            buf.writeBoolean(globalShadow);
        }
        
        buf.writeBoolean(globalFadeInTicks != null);
        if (globalFadeInTicks != null) {
            buf.writeInt(globalFadeInTicks);
        }
        buf.writeBoolean(globalFadeOutTicks != null);
        if (globalFadeOutTicks != null) {
            buf.writeInt(globalFadeOutTicks);
        }
        
        // Encode per-span fade effects
        buf.writeBoolean(fadeInTicks != null);
        if (fadeInTicks != null) {
            buf.writeInt(fadeInTicks);
        }
        buf.writeBoolean(fadeOutTicks != null);
        if (fadeOutTicks != null) {
            buf.writeInt(fadeOutTicks);
        }
    }
    
    public static TextSpan decode(net.minecraft.network.FriendlyByteBuf buf) {
        String content = buf.readUtf();
        TextSpan span = new TextSpan(content);
        
        // Decode style properties
        if (buf.readBoolean()) span.bold = true;
        if (buf.readBoolean()) span.italic = true;
        if (buf.readBoolean()) span.underline = true;
        if (buf.readBoolean()) span.strikethrough = true;
        if (buf.readBoolean()) span.obfuscated = true;
        
        if (buf.readBoolean()) {
            span.color = net.minecraft.network.chat.TextColor.fromRgb(buf.readInt());
        }
        if (buf.readBoolean()) {
            span.font = buf.readResourceLocation();
        }
        
        // Decode gradient colors
        if (buf.readBoolean()) {
            int colorCount = buf.readVarInt();
            net.minecraft.network.chat.TextColor[] colors = new net.minecraft.network.chat.TextColor[colorCount];
            for (int i = 0; i < colorCount; i++) {
                colors[i] = net.minecraft.network.chat.TextColor.fromRgb(buf.readInt());
            }
            span.gradientColors = colors;
        }
        
        // Decode other effect properties
        if (buf.readBoolean()) {
            span.typewriterSpeed = buf.readFloat();
            span.typewriterCenter = buf.readBoolean();
        }
        
        if (buf.readBoolean()) {
            span.shakeType = buf.readEnum(ShakeType.class);
            span.shakeAmplitude = buf.readFloat();
            if (buf.readBoolean()) {
                span.shakeSpeed = buf.readFloat();
            }
            if (buf.readBoolean()) {
                span.shakeWavelength = buf.readFloat();
            }
        }
        
        if (buf.readBoolean()) {
            span.charShakeType = buf.readEnum(ShakeType.class);
            span.charShakeAmplitude = buf.readFloat();
            if (buf.readBoolean()) {
                span.charShakeSpeed = buf.readFloat();
            }
            if (buf.readBoolean()) {
                span.charShakeWavelength = buf.readFloat();
            }
        }
        
        if (buf.readBoolean()) {
            span.obfuscateMode = buf.readEnum(ObfuscateMode.class);
            span.obfuscateSpeed = buf.readFloat();
        }
        
        if (buf.readBoolean()) {
            span.hasBackground = true;
        }
        if (buf.readBoolean()) {
            span.backgroundColor = new ImmersiveColor(buf.readInt());
        }
        
        if (buf.readBoolean()) {
            int bgColorCount = buf.readVarInt();
            ImmersiveColor[] bgColors = new ImmersiveColor[bgColorCount];
            for (int i = 0; i < bgColorCount; i++) {
                bgColors[i] = new ImmersiveColor(buf.readInt());
            }
            span.backgroundGradient = bgColors;
        }
        
        // Decode global message attributes
        if (buf.readBoolean()) {
            span.globalBackground = true;
        }
        if (buf.readBoolean()) {
            span.globalBackgroundColor = new ImmersiveColor(buf.readInt());
        }
        
        if (buf.readBoolean()) {
            int globalBgColorCount = buf.readVarInt();
            ImmersiveColor[] globalBgColors = new ImmersiveColor[globalBgColorCount];
            for (int i = 0; i < globalBgColorCount; i++) {
                globalBgColors[i] = new ImmersiveColor(buf.readInt());
            }
            span.globalBackgroundGradient = globalBgColors;
        }
        
        if (buf.readBoolean()) {
            span.globalBorderStart = new ImmersiveColor(buf.readInt());
        }
        if (buf.readBoolean()) {
            span.globalBorderEnd = new ImmersiveColor(buf.readInt());
        }
        
        if (buf.readBoolean()) {
            span.globalXOffset = buf.readFloat();
        }
        if (buf.readBoolean()) {
            span.globalYOffset = buf.readFloat();
        }
        
        if (buf.readBoolean()) {
            span.globalAnchor = buf.readEnum(TextAnchor.class);
        }
        if (buf.readBoolean()) {
            span.globalAlign = buf.readEnum(TextAnchor.class);
        }
        
        if (buf.readBoolean()) {
            span.globalScale = buf.readFloat();
        }
        
        if (buf.readBoolean()) {
            span.globalShadow = buf.readBoolean();
        }
        
        if (buf.readBoolean()) {
            span.globalFadeInTicks = buf.readInt();
        }
        if (buf.readBoolean()) {
            span.globalFadeOutTicks = buf.readInt();
        }
        
        // Decode per-span fade effects
        if (buf.readBoolean()) {
            span.fadeInTicks = buf.readInt();
        }
        if (buf.readBoolean()) {
            span.fadeOutTicks = buf.readInt();
        }
        
        return span;
    }
}
