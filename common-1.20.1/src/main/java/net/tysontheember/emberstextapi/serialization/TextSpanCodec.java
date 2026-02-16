package net.tysontheember.emberstextapi.serialization;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.immersivemessages.api.ObfuscateMode;
import net.tysontheember.emberstextapi.immersivemessages.api.ShakeType;
import net.tysontheember.emberstextapi.immersivemessages.api.TextAnchor;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static net.tysontheember.emberstextapi.serialization.SerializationUtil.*;

/**
 * Network serialization codec for TextSpan instances.
 * <p>
 * Handles encoding and decoding of TextSpan objects for network transmission
 * between server and client. This separation allows platform-specific buffer
 * implementations (Forge's FriendlyByteBuf vs Fabric's PacketByteBuf).
 * </p>
 */
public final class TextSpanCodec {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Private constructor to prevent instantiation.
     */
    private TextSpanCodec() {
        throw new UnsupportedOperationException("Codec class");
    }

    /**
     * Encode a TextSpan to a network buffer.
     *
     * @param span The TextSpan to encode
     * @param buf The buffer to write to
     */
    public static void encode(TextSpan span, FriendlyByteBuf buf) {
        buf.writeUtf(span.getContent());

        // Encode style properties (using null-safe booleans)
        buf.writeBoolean(span.getBold() != null && span.getBold());
        buf.writeBoolean(span.getItalic() != null && span.getItalic());
        buf.writeBoolean(span.getUnderline() != null && span.getUnderline());
        buf.writeBoolean(span.getStrikethrough() != null && span.getStrikethrough());
        buf.writeBoolean(span.getObfuscated() != null && span.getObfuscated());

        buf.writeBoolean(span.getColor() != null);
        if (span.getColor() != null) {
            buf.writeInt(span.getColor().getValue());
        }
        buf.writeBoolean(span.getFont() != null);
        if (span.getFont() != null) {
            buf.writeResourceLocation(span.getFont());
        }

        // Encode gradient colors
        buf.writeBoolean(span.getGradientColors() != null);
        if (span.getGradientColors() != null) {
            buf.writeVarInt(span.getGradientColors().length);
            for (TextColor gradientColor : span.getGradientColors()) {
                buf.writeInt(gradientColor.getValue());
            }
        }

        // Encode other effect properties
        buf.writeBoolean(span.getTypewriterSpeed() != null);
        if (span.getTypewriterSpeed() != null) {
            buf.writeFloat(span.getTypewriterSpeed());
            buf.writeBoolean(span.getTypewriterCenter() != null && span.getTypewriterCenter());
        }

        buf.writeBoolean(span.getShakeType() != null);
        if (span.getShakeType() != null) {
            buf.writeEnum(span.getShakeType());
            buf.writeFloat(span.getShakeAmplitude() != null ? span.getShakeAmplitude() : 0f);
            buf.writeBoolean(span.getShakeSpeed() != null);
            if (span.getShakeSpeed() != null) {
                buf.writeFloat(span.getShakeSpeed());
            }
            buf.writeBoolean(span.getShakeWavelength() != null);
            if (span.getShakeWavelength() != null) {
                buf.writeFloat(span.getShakeWavelength());
            }
        }

        buf.writeBoolean(span.getCharShakeType() != null);
        if (span.getCharShakeType() != null) {
            buf.writeEnum(span.getCharShakeType());
            buf.writeFloat(span.getCharShakeAmplitude() != null ? span.getCharShakeAmplitude() : 0f);
            buf.writeBoolean(span.getCharShakeSpeed() != null);
            if (span.getCharShakeSpeed() != null) {
                buf.writeFloat(span.getCharShakeSpeed());
            }
            buf.writeBoolean(span.getCharShakeWavelength() != null);
            if (span.getCharShakeWavelength() != null) {
                buf.writeFloat(span.getCharShakeWavelength());
            }
        }

        buf.writeBoolean(span.getObfuscateMode() != null);
        if (span.getObfuscateMode() != null) {
            buf.writeEnum(span.getObfuscateMode());
            buf.writeFloat(span.getObfuscateSpeed() != null ? span.getObfuscateSpeed() : 1f);
        }

        buf.writeBoolean(span.getHasBackground() != null && span.getHasBackground());
        buf.writeBoolean(span.getBackgroundColor() != null);
        if (span.getBackgroundColor() != null) {
            buf.writeInt(span.getBackgroundColor().getARGB());
        }

        buf.writeBoolean(span.getBackgroundGradient() != null);
        if (span.getBackgroundGradient() != null) {
            buf.writeVarInt(span.getBackgroundGradient().length);
            for (ImmersiveColor bgColor : span.getBackgroundGradient()) {
                buf.writeInt(bgColor.getARGB());
            }
        }

        // Encode global message attributes
        buf.writeBoolean(span.getGlobalBackground() != null && span.getGlobalBackground());
        buf.writeBoolean(span.getGlobalBackgroundColor() != null);
        if (span.getGlobalBackgroundColor() != null) {
            buf.writeInt(span.getGlobalBackgroundColor().getARGB());
        }

        buf.writeBoolean(span.getGlobalBackgroundGradient() != null);
        if (span.getGlobalBackgroundGradient() != null) {
            buf.writeVarInt(span.getGlobalBackgroundGradient().length);
            for (ImmersiveColor bgColor : span.getGlobalBackgroundGradient()) {
                buf.writeInt(bgColor.getARGB());
            }
        }

        buf.writeBoolean(span.getGlobalBorderStart() != null);
        if (span.getGlobalBorderStart() != null) {
            buf.writeInt(span.getGlobalBorderStart().getARGB());
        }
        buf.writeBoolean(span.getGlobalBorderEnd() != null);
        if (span.getGlobalBorderEnd() != null) {
            buf.writeInt(span.getGlobalBorderEnd().getARGB());
        }

        buf.writeBoolean(span.getGlobalXOffset() != null);
        if (span.getGlobalXOffset() != null) {
            buf.writeFloat(span.getGlobalXOffset());
        }
        buf.writeBoolean(span.getGlobalYOffset() != null);
        if (span.getGlobalYOffset() != null) {
            buf.writeFloat(span.getGlobalYOffset());
        }

        buf.writeBoolean(span.getGlobalAnchor() != null);
        if (span.getGlobalAnchor() != null) {
            buf.writeEnum(span.getGlobalAnchor());
        }
        buf.writeBoolean(span.getGlobalAlign() != null);
        if (span.getGlobalAlign() != null) {
            buf.writeEnum(span.getGlobalAlign());
        }

        buf.writeBoolean(span.getGlobalScale() != null);
        if (span.getGlobalScale() != null) {
            buf.writeFloat(span.getGlobalScale());
        }

        buf.writeBoolean(span.getGlobalShadow() != null);
        if (span.getGlobalShadow() != null) {
            buf.writeBoolean(span.getGlobalShadow());
        }

        buf.writeBoolean(span.getGlobalFadeInTicks() != null);
        if (span.getGlobalFadeInTicks() != null) {
            buf.writeInt(span.getGlobalFadeInTicks());
        }
        buf.writeBoolean(span.getGlobalFadeOutTicks() != null);
        if (span.getGlobalFadeOutTicks() != null) {
            buf.writeInt(span.getGlobalFadeOutTicks());
        }

        // Encode per-span fade effects
        buf.writeBoolean(span.getFadeInTicks() != null);
        if (span.getFadeInTicks() != null) {
            buf.writeInt(span.getFadeInTicks());
        }
        buf.writeBoolean(span.getFadeOutTicks() != null);
        if (span.getFadeOutTicks() != null) {
            buf.writeInt(span.getFadeOutTicks());
        }

        // Encode item rendering
        buf.writeBoolean(span.getItemId() != null);
        if (span.getItemId() != null) {
            buf.writeUtf(span.getItemId());
            buf.writeVarInt(span.getItemCount() != null ? span.getItemCount() : 1);
            buf.writeBoolean(span.getItemOffsetX() != null);
            if (span.getItemOffsetX() != null) {
                buf.writeFloat(span.getItemOffsetX());
            }
            buf.writeBoolean(span.getItemOffsetY() != null);
            if (span.getItemOffsetY() != null) {
                buf.writeFloat(span.getItemOffsetY());
            }
        }

        // Encode entity rendering
        buf.writeBoolean(span.getEntityId() != null);
        if (span.getEntityId() != null) {
            buf.writeUtf(span.getEntityId());
            buf.writeFloat(span.getEntityScale() != null ? span.getEntityScale() : 1.0f);
            buf.writeBoolean(span.getEntityOffsetX() != null);
            if (span.getEntityOffsetX() != null) {
                buf.writeFloat(span.getEntityOffsetX());
            }
            buf.writeBoolean(span.getEntityOffsetY() != null);
            if (span.getEntityOffsetY() != null) {
                buf.writeFloat(span.getEntityOffsetY());
            }
            buf.writeBoolean(span.getEntityYaw() != null);
            if (span.getEntityYaw() != null) {
                buf.writeFloat(span.getEntityYaw());
            }
            buf.writeBoolean(span.getEntityPitch() != null);
            if (span.getEntityPitch() != null) {
                buf.writeFloat(span.getEntityPitch());
            }
            buf.writeBoolean(span.getEntityRoll() != null);
            if (span.getEntityRoll() != null) {
                buf.writeFloat(span.getEntityRoll());
            }
            buf.writeBoolean(span.getEntityLighting() != null);
            if (span.getEntityLighting() != null) {
                buf.writeVarInt(span.getEntityLighting());
            }
            buf.writeBoolean(span.getEntitySpin() != null);
            if (span.getEntitySpin() != null) {
                buf.writeFloat(span.getEntitySpin());
            }
            buf.writeBoolean(span.getEntityAnimation() != null);
            if (span.getEntityAnimation() != null) {
                buf.writeUtf(span.getEntityAnimation());
            }
        }

        // Encode effects (v2.0.0)
        List<Effect> effects = span.getEffects();
        buf.writeVarInt(effects == null ? 0 : effects.size());
        if (effects != null && !effects.isEmpty()) {
            for (Effect effect : effects) {
                buf.writeUtf(effect.serialize());
            }
        }
    }

    /**
     * Decode a TextSpan from a network buffer.
     *
     * @param buf The buffer to read from
     * @return The decoded TextSpan
     * @throws IllegalArgumentException if buffer contains invalid data
     */
    public static TextSpan decode(FriendlyByteBuf buf) {
        // Validate and read content with length limit
        String content = buf.readUtf(MAX_CONTENT_LENGTH);
        TextSpan span = new TextSpan(content);

        // Decode style properties
        if (buf.readBoolean()) span.bold(true);
        if (buf.readBoolean()) span.italic(true);
        if (buf.readBoolean()) span.underline(true);
        if (buf.readBoolean()) span.strikethrough(true);
        if (buf.readBoolean()) span.obfuscated(true);

        if (buf.readBoolean()) {
            span.color(TextColor.fromRgb(buf.readInt()));
        }
        if (buf.readBoolean()) {
            span.font(buf.readResourceLocation());
        }

        // Decode gradient colors with size validation
        if (buf.readBoolean()) {
            int colorCount = buf.readVarInt();
            if (colorCount < 0 || colorCount > MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException("Invalid gradient color count: " + colorCount);
            }
            TextColor[] colors = new TextColor[colorCount];
            for (int i = 0; i < colorCount; i++) {
                colors[i] = TextColor.fromRgb(buf.readInt());
            }
            span.setGradientColors(colors);
        }

        // Decode other effect properties
        if (buf.readBoolean()) {
            span.typewriter(clampFloat(buf.readFloat(), 0.001f, 1000f));
            span.setTypewriterCenter(buf.readBoolean());
        }

        if (buf.readBoolean()) {
            ShakeType shakeType = buf.readEnum(ShakeType.class);
            float amplitude = clampFloat(buf.readFloat(), 0f, MAX_OFFSET);
            Float speed = null;
            if (buf.readBoolean()) {
                speed = clampFloat(buf.readFloat(), -1000f, 1000f);
            }
            Float wavelength = null;
            if (buf.readBoolean()) {
                wavelength = clampFloat(buf.readFloat(), 0.001f, 1000f);
            }
            span.setShakeType(shakeType);
            span.setShakeAmplitude(amplitude);
            if (speed != null) span.setShakeSpeed(speed);
            if (wavelength != null) span.setShakeWavelength(wavelength);
        }

        if (buf.readBoolean()) {
            ShakeType charShakeType = buf.readEnum(ShakeType.class);
            float amplitude = clampFloat(buf.readFloat(), 0f, MAX_OFFSET);
            Float speed = null;
            if (buf.readBoolean()) {
                speed = clampFloat(buf.readFloat(), -1000f, 1000f);
            }
            Float wavelength = null;
            if (buf.readBoolean()) {
                wavelength = clampFloat(buf.readFloat(), 0.001f, 1000f);
            }
            span.setCharShakeType(charShakeType);
            span.setCharShakeAmplitude(amplitude);
            if (speed != null) span.setCharShakeSpeed(speed);
            if (wavelength != null) span.setCharShakeWavelength(wavelength);
        }

        if (buf.readBoolean()) {
            span.setObfuscateMode(buf.readEnum(ObfuscateMode.class));
            span.setObfuscateSpeed(clampFloat(buf.readFloat(), 0f, 1000f));
        }

        if (buf.readBoolean()) {
            span.setHasBackground(true);
        }
        if (buf.readBoolean()) {
            span.background(new ImmersiveColor(buf.readInt()));
        }

        // Decode background gradient with size validation
        if (buf.readBoolean()) {
            int bgColorCount = buf.readVarInt();
            if (bgColorCount < 0 || bgColorCount > MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException("Invalid background gradient color count: " + bgColorCount);
            }
            ImmersiveColor[] bgColors = new ImmersiveColor[bgColorCount];
            for (int i = 0; i < bgColorCount; i++) {
                bgColors[i] = new ImmersiveColor(buf.readInt());
            }
            span.backgroundGradient(bgColors);
        }

        // Decode global message attributes
        if (buf.readBoolean()) {
            span.setGlobalBackground(true);
        }
        if (buf.readBoolean()) {
            span.globalBackgroundColor(new ImmersiveColor(buf.readInt()));
        }

        // Decode global background gradient with size validation
        if (buf.readBoolean()) {
            int globalBgColorCount = buf.readVarInt();
            if (globalBgColorCount < 0 || globalBgColorCount > MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException("Invalid global background gradient color count: " + globalBgColorCount);
            }
            ImmersiveColor[] globalBgColors = new ImmersiveColor[globalBgColorCount];
            for (int i = 0; i < globalBgColorCount; i++) {
                globalBgColors[i] = new ImmersiveColor(buf.readInt());
            }
            span.globalBackgroundGradient(globalBgColors);
        }

        if (buf.readBoolean()) {
            span.setGlobalBorderStart(new ImmersiveColor(buf.readInt()));
        }
        if (buf.readBoolean()) {
            span.setGlobalBorderEnd(new ImmersiveColor(buf.readInt()));
        }

        if (buf.readBoolean()) {
            span.setGlobalXOffset(clampFloat(buf.readFloat(), -MAX_OFFSET, MAX_OFFSET));
        }
        if (buf.readBoolean()) {
            span.setGlobalYOffset(clampFloat(buf.readFloat(), -MAX_OFFSET, MAX_OFFSET));
        }

        if (buf.readBoolean()) {
            span.setGlobalAnchor(buf.readEnum(TextAnchor.class));
        }
        if (buf.readBoolean()) {
            span.setGlobalAlign(buf.readEnum(TextAnchor.class));
        }

        if (buf.readBoolean()) {
            span.globalScale(clampFloat(buf.readFloat(), 0.01f, MAX_SCALE));
        }

        if (buf.readBoolean()) {
            span.globalShadow(buf.readBoolean());
        }

        if (buf.readBoolean()) {
            span.globalFadeIn(Math.max(0, buf.readInt()));
        }
        if (buf.readBoolean()) {
            span.globalFadeOut(Math.max(0, buf.readInt()));
        }

        // Decode per-span fade effects
        if (buf.readBoolean()) {
            span.fadeIn(Math.max(0, buf.readInt()));
        }
        if (buf.readBoolean()) {
            span.fadeOut(Math.max(0, buf.readInt()));
        }

        // Decode item rendering with validation
        if (buf.readBoolean()) {
            String itemId = buf.readUtf(MAX_ID_LENGTH);
            int itemCount = Math.min(Math.max(1, buf.readVarInt()), MAX_ITEM_COUNT);
            span.item(itemId, itemCount);

            if (buf.readBoolean()) {
                span.itemOffsetX(clampFloat(buf.readFloat(), -MAX_OFFSET, MAX_OFFSET));
            }
            if (buf.readBoolean()) {
                span.itemOffsetY(clampFloat(buf.readFloat(), -MAX_OFFSET, MAX_OFFSET));
            }
        }

        // Decode entity rendering with validation
        if (buf.readBoolean()) {
            String entityId = buf.readUtf(MAX_ID_LENGTH);
            float entityScale = clampFloat(buf.readFloat(), 0.01f, MAX_SCALE);
            span.entity(entityId);
            span.entityScale(entityScale);

            if (buf.readBoolean()) {
                span.entityOffsetX(clampFloat(buf.readFloat(), -MAX_OFFSET, MAX_OFFSET));
            }
            if (buf.readBoolean()) {
                span.entityOffsetY(clampFloat(buf.readFloat(), -MAX_OFFSET, MAX_OFFSET));
            }
            if (buf.readBoolean()) {
                span.entityYaw(clampFloat(buf.readFloat(), -360f, 360f));
            }
            if (buf.readBoolean()) {
                span.entityPitch(clampFloat(buf.readFloat(), -90f, 90f));
            }
            if (buf.readBoolean()) {
                span.entityRoll(clampFloat(buf.readFloat(), -360f, 360f));
            }
            if (buf.readBoolean()) {
                span.entityLighting(Math.max(0, Math.min(15, buf.readVarInt())));
            }
            if (buf.readBoolean()) {
                span.entitySpin(buf.readFloat()); // No clamping - allow any rotation speed
            }
            if (buf.readBoolean()) {
                span.setEntityAnimation(buf.readUtf(MAX_ID_LENGTH));
            }
        }

        // Decode effects with count and length validation (v2.0.0)
        int effectCount = buf.readVarInt();
        if (effectCount < 0 || effectCount > MAX_ARRAY_SIZE) {
            throw new IllegalArgumentException("Invalid effect count: " + effectCount);
        }
        if (effectCount > 0) {
            for (int i = 0; i < effectCount; i++) {
                String effectTag = buf.readUtf(MAX_EFFECT_TAG_LENGTH);
                try {
                    Effect effect = EffectRegistry.parseTag(effectTag);
                    span.addEffect(effect);
                } catch (IllegalArgumentException e) {
                    // Skip unknown effects (forward compatibility)
                    LOGGER.warn("Failed to decode effect: {}", effectTag, e);
                }
            }
        }

        return span;
    }
}
