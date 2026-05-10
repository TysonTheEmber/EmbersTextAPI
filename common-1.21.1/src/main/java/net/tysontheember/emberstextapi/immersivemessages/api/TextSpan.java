package net.tysontheember.emberstextapi.immersivemessages.api;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.util.ColorParser;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;

import java.util.ArrayList;
import java.util.List;

public class TextSpan {
    private static final Logger LOGGER = LogUtils.getLogger();

    private String content;

    private List<Effect> effects;

    private TextColor color;
    private Boolean bold;
    private Boolean italic;
    private Boolean underline;
    private Boolean strikethrough;
    private Boolean obfuscated;
    private ResourceLocation font;

    private Float typewriterSpeed;
    private Boolean typewriterCenter;

    private transient Integer renderCharIndex;
    private transient Integer renderTotalChars;
    private transient Integer renderAbsoluteIndex;

    private ObfuscateMode obfuscateMode;
    private Float obfuscateSpeed;

    private Integer fadeInTicks;
    private Integer fadeOutTicks;

    private Boolean hasBackground;
    private ImmersiveColor backgroundColor;
    private ImmersiveColor[] backgroundGradient;

    private String itemId;
    private Integer itemCount;
    private Float itemOffsetX;
    private Float itemOffsetY;
    private String itemNbt;

    private String entityId;
    private Float entityScale;
    private Float entityOffsetX;
    private Float entityOffsetY;
    private Float entityYaw;
    private Float entityPitch;
    private Float entityRoll;
    private Integer entityLighting;
    private Float entitySpin;
    private String entityAnimation;
    private String entityNbt;

    private String clickAction;
    private String clickValue;
    private String hoverAction;
    private String hoverValue;

    public TextSpan(String content) {
        this.content = content != null ? content : "";
    }

    public TextSpan(TextSpan other) {
        this.content = other.content;
        this.effects = other.effects != null ? new ArrayList<>(other.effects) : null;
        this.color = other.color;
        this.bold = other.bold;
        this.italic = other.italic;
        this.underline = other.underline;
        this.strikethrough = other.strikethrough;
        this.obfuscated = other.obfuscated;
        this.font = other.font;
        this.typewriterSpeed = other.typewriterSpeed;
        this.typewriterCenter = other.typewriterCenter;
        this.obfuscateMode = other.obfuscateMode;
        this.obfuscateSpeed = other.obfuscateSpeed;
        this.fadeInTicks = other.fadeInTicks;
        this.fadeOutTicks = other.fadeOutTicks;
        this.hasBackground = other.hasBackground;
        this.backgroundColor = other.backgroundColor;
        this.backgroundGradient = other.backgroundGradient != null ? other.backgroundGradient.clone() : null;
        this.itemId = other.itemId;
        this.itemCount = other.itemCount;
        this.itemOffsetX = other.itemOffsetX;
        this.itemOffsetY = other.itemOffsetY;
        this.itemNbt = other.itemNbt;
        this.entityId = other.entityId;
        this.entityScale = other.entityScale;
        this.entityOffsetX = other.entityOffsetX;
        this.entityOffsetY = other.entityOffsetY;
        this.entityYaw = other.entityYaw;
        this.entityPitch = other.entityPitch;
        this.entityRoll = other.entityRoll;
        this.entityLighting = other.entityLighting;
        this.entitySpin = other.entitySpin;
        this.entityAnimation = other.entityAnimation;
        this.entityNbt = other.entityNbt;
        this.clickAction = other.clickAction;
        this.clickValue = other.clickValue;
        this.hoverAction = other.hoverAction;
        this.hoverValue = other.hoverValue;
    }

    public String getContent() { return content; }
    void setContent(String content) { this.content = content != null ? content : ""; }
    public List<Effect> getEffects() { return effects; }
    public TextColor getColor() { return color; }
    public Boolean getBold() { return bold; }
    public Boolean getItalic() { return italic; }
    public Boolean getUnderline() { return underline; }
    public Boolean getStrikethrough() { return strikethrough; }
    public Boolean getObfuscated() { return obfuscated; }
    public ResourceLocation getFont() { return font; }
    public Float getTypewriterSpeed() { return typewriterSpeed; }
    public Boolean getTypewriterCenter() { return typewriterCenter; }
    public Integer getRenderCharIndex() { return renderCharIndex; }
    public Integer getRenderTotalChars() { return renderTotalChars; }
    public Integer getRenderAbsoluteIndex() { return renderAbsoluteIndex; }
    public ObfuscateMode getObfuscateMode() { return obfuscateMode; }
    public Float getObfuscateSpeed() { return obfuscateSpeed; }
    public Integer getFadeInTicks() { return fadeInTicks; }
    public Integer getFadeOutTicks() { return fadeOutTicks; }
    public Boolean getHasBackground() { return hasBackground; }
    public ImmersiveColor getBackgroundColor() { return backgroundColor; }
    public ImmersiveColor[] getBackgroundGradient() { return backgroundGradient; }
    public String getItemId() { return itemId; }
    public Integer getItemCount() { return itemCount; }
    public Float getItemOffsetX() { return itemOffsetX; }
    public Float getItemOffsetY() { return itemOffsetY; }
    public String getItemNbt() { return itemNbt; }
    public String getEntityId() { return entityId; }
    public Float getEntityScale() { return entityScale; }
    public Float getEntityOffsetX() { return entityOffsetX; }
    public Float getEntityOffsetY() { return entityOffsetY; }
    public Float getEntityYaw() { return entityYaw; }
    public Float getEntityPitch() { return entityPitch; }
    public Float getEntityRoll() { return entityRoll; }
    public Integer getEntityLighting() { return entityLighting; }
    public Float getEntitySpin() { return entitySpin; }
    public String getEntityAnimation() { return entityAnimation; }
    public String getEntityNbt() { return entityNbt; }
    public String getClickAction() { return clickAction; }
    public String getClickValue() { return clickValue; }
    public String getHoverAction() { return hoverAction; }
    public String getHoverValue() { return hoverValue; }

    public TextSpan color(TextColor color) { this.color = color; return this; }
    public TextSpan color(int rgb) { return color(TextColor.fromRgb(rgb)); }
    public TextSpan color(String value) {
        TextColor parsed = ColorParser.parseTextColor(value);
        if (parsed != null) {
            this.color = parsed;
        }
        return this;
    }

    public TextSpan bold(boolean bold) { this.bold = bold; return this; }
    public TextSpan italic(boolean italic) { this.italic = italic; return this; }
    public TextSpan underline(boolean underline) { this.underline = underline; return this; }
    public TextSpan strikethrough(boolean strikethrough) { this.strikethrough = strikethrough; return this; }
    public TextSpan obfuscated(boolean obfuscated) { this.obfuscated = obfuscated; return this; }
    public TextSpan font(ResourceLocation font) { this.font = font; return this; }

    public TextSpan addEffect(Effect effect) {
        if (this.effects == null) {
            this.effects = new ArrayList<>();
        }
        this.effects.add(effect);
        return this;
    }

    public TextSpan effect(String tagContent) {
        if (tagContent != null && !tagContent.isEmpty()) {
            try {
                LOGGER.debug("Parsing effect tag: '{}'", tagContent);
                Effect effect = EffectRegistry.parseTag(tagContent);
                LOGGER.debug("Created effect: {}", effect.getName());
                addEffect(effect);
            } catch (IllegalArgumentException e) {

                LOGGER.debug("Failed to parse effect: '{}' - {}", tagContent, e.getMessage());
            }
        }
        return this;
    }

    public TextSpan clearEffects() {
        if (this.effects != null) {
            this.effects.clear();
        }
        return this;
    }

    public TextSpan typewriter(float speed) { return typewriter(speed, false); }
    public TextSpan typewriter(float speed, boolean center) {
        this.typewriterSpeed = speed;
        this.typewriterCenter = center;
        return this;
    }

    public TextSpan renderContext(int charIndex, int totalChars, int absoluteIndex) {
        this.renderCharIndex = charIndex;
        this.renderTotalChars = totalChars;
        this.renderAbsoluteIndex = absoluteIndex;
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

    public TextSpan item(String itemId) {
        this.itemId = itemId;
        this.itemCount = 1;
        return this;
    }

    public TextSpan item(String itemId, int count) {
        this.itemId = itemId;
        this.itemCount = Math.max(1, count);
        return this;
    }

    public TextSpan itemOffset(float x, float y) {
        this.itemOffsetX = x;
        this.itemOffsetY = y;
        return this;
    }

    public TextSpan itemNbt(String nbt) {
        this.itemNbt = nbt;
        return this;
    }

    public TextSpan entity(String entityId) {
        this.entityId = entityId;
        this.entityScale = 1.0f;
        return this;
    }

    public TextSpan entity(String entityId, float scale) {
        this.entityId = entityId;
        this.entityScale = scale;
        return this;
    }

    public TextSpan entityOffset(float x, float y) {
        this.entityOffsetX = x;
        this.entityOffsetY = y;
        return this;
    }

    public TextSpan entityRotation(float yaw, float pitch) {
        this.entityYaw = yaw;
        this.entityPitch = pitch;
        return this;
    }

    public TextSpan entityRotation(float yaw, float pitch, float roll) {
        this.entityYaw = yaw;
        this.entityPitch = pitch;
        this.entityRoll = roll;
        return this;
    }

    public TextSpan entityRoll(float roll) {
        this.entityRoll = roll;
        return this;
    }

    public TextSpan entityLighting(int lighting) {
        this.entityLighting = Math.max(0, Math.min(15, lighting));
        return this;
    }

    public TextSpan entitySpin(float degreesPerTick) {
        this.entitySpin = degreesPerTick;
        return this;
    }

    public TextSpan entityAnimation(String animation) {
        this.entityAnimation = animation;
        return this;
    }

    public TextSpan entityNbt(String nbt) {
        this.entityNbt = nbt;
        return this;
    }

    public TextSpan clickAction(String action) { this.clickAction = action; return this; }
    public TextSpan clickValue(String value) { this.clickValue = value; return this; }
    public TextSpan hoverAction(String action) { this.hoverAction = action; return this; }
    public TextSpan hoverValue(String value) { this.hoverValue = value; return this; }

    public boolean hasCustomStyling() {
        return color != null || bold != null || italic != null || underline != null ||
               strikethrough != null || obfuscated != null || font != null ||
               typewriterSpeed != null || obfuscateMode != null ||
               hasBackground != null || fadeInTicks != null || fadeOutTicks != null ||
               itemId != null || entityId != null;
    }

    public TextSpan inherit() {
        return new TextSpan(this);
    }

    @Override
    public String toString() {
        return "TextSpan{content='" + content + "', styling=" + hasCustomStyling() + "}";
    }

    public void setTypewriterCenter(Boolean center) {
        this.typewriterCenter = center;
    }

    public void setObfuscateMode(ObfuscateMode mode) {
        this.obfuscateMode = mode;
    }

    public void setObfuscateSpeed(Float speed) {
        this.obfuscateSpeed = speed;
    }

    public void itemOffsetX(Float x) {
        this.itemOffsetX = x;
    }

    public void itemOffsetY(Float y) {
        this.itemOffsetY = y;
    }

    public void entityScale(Float scale) {
        this.entityScale = scale;
    }

    public void entityOffsetX(Float x) {
        this.entityOffsetX = x;
    }

    public void entityOffsetY(Float y) {
        this.entityOffsetY = y;
    }

    public void entityYaw(Float yaw) {
        this.entityYaw = yaw;
    }

    public void entityPitch(Float pitch) {
        this.entityPitch = pitch;
    }

    public void entityRoll(Float roll) {
        this.entityRoll = roll;
    }

    public void entityLighting(Integer lighting) {
        this.entityLighting = lighting;
    }

    public void entitySpin(Float spin) {
        this.entitySpin = spin;
    }

    public void setEntityAnimation(String animation) {
        this.entityAnimation = animation;
    }

    public void setHasBackground(Boolean enabled) {
        this.hasBackground = enabled;
    }

    public void encode(net.minecraft.network.FriendlyByteBuf buf) {
        net.tysontheember.emberstextapi.serialization.TextSpanCodec.encode(this, buf);
    }

    public static TextSpan decode(net.minecraft.network.FriendlyByteBuf buf) {
        return net.tysontheember.emberstextapi.serialization.TextSpanCodec.decode(buf);
    }
}
