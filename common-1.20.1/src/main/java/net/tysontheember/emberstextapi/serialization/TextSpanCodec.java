package net.tysontheember.emberstextapi.serialization;

import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.tysontheember.emberstextapi.immersivemessages.api.ObfuscateMode;
import net.tysontheember.emberstextapi.immersivemessages.api.TextAlign;
import net.tysontheember.emberstextapi.immersivemessages.api.TextAnchor;
import net.tysontheember.emberstextapi.immersivemessages.api.TextSpan;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectRegistry;
import net.tysontheember.emberstextapi.immersivemessages.util.ImmersiveColor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static net.tysontheember.emberstextapi.serialization.SerializationUtil.*;

public final class TextSpanCodec {

    private static final Logger LOGGER = LogUtils.getLogger();

    private TextSpanCodec() {
        throw new UnsupportedOperationException("Codec class");
    }

    public static void encode(TextSpan span, FriendlyByteBuf buf) {
        buf.writeUtf(span.getContent());

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

        buf.writeBoolean(span.getTypewriterSpeed() != null);
        if (span.getTypewriterSpeed() != null) {
            buf.writeFloat(span.getTypewriterSpeed());
            buf.writeBoolean(span.getTypewriterCenter() != null && span.getTypewriterCenter());
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

        buf.writeBoolean(span.getFadeInTicks() != null);
        if (span.getFadeInTicks() != null) {
            buf.writeInt(span.getFadeInTicks());
        }
        buf.writeBoolean(span.getFadeOutTicks() != null);
        if (span.getFadeOutTicks() != null) {
            buf.writeInt(span.getFadeOutTicks());
        }

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
            buf.writeBoolean(span.getItemNbt() != null);
            if (span.getItemNbt() != null) {
                buf.writeUtf(span.getItemNbt());
            }
        }

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
            buf.writeBoolean(span.getEntityNbt() != null);
            if (span.getEntityNbt() != null) {
                buf.writeUtf(span.getEntityNbt());
            }
        }

        buf.writeBoolean(span.getClickAction() != null && span.getClickValue() != null);
        if (span.getClickAction() != null && span.getClickValue() != null) {
            buf.writeUtf(span.getClickAction());
            buf.writeUtf(span.getClickValue());
        }
        buf.writeBoolean(span.getHoverAction() != null && span.getHoverValue() != null);
        if (span.getHoverAction() != null && span.getHoverValue() != null) {
            buf.writeUtf(span.getHoverAction());
            buf.writeUtf(span.getHoverValue());
        }

        List<Effect> effects = span.getEffects();
        buf.writeVarInt(effects == null ? 0 : effects.size());
        if (effects != null && !effects.isEmpty()) {
            for (Effect effect : effects) {
                buf.writeUtf(effect.serialize());
            }
        }
    }

    public static TextSpan decode(FriendlyByteBuf buf) {

        String content = buf.readUtf(MAX_CONTENT_LENGTH);
        TextSpan span = new TextSpan(content);

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

        if (buf.readBoolean()) {
            span.typewriter(clampFloat(buf.readFloat(), 0.001f, 1000f));
            span.setTypewriterCenter(buf.readBoolean());
        }

        if (buf.readBoolean()) {
            span.setObfuscateMode(readEnumSafe(buf, ObfuscateMode.class));
            span.setObfuscateSpeed(clampFloat(buf.readFloat(), 0f, 1000f));
        }

        if (buf.readBoolean()) {
            span.setHasBackground(true);
        }
        if (buf.readBoolean()) {
            span.background(new ImmersiveColor(buf.readInt()));
        }

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

        if (buf.readBoolean()) {
            span.fadeIn(Math.max(0, buf.readInt()));
        }
        if (buf.readBoolean()) {
            span.fadeOut(Math.max(0, buf.readInt()));
        }

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
            if (buf.readBoolean()) {
                span.itemNbt(buf.readUtf(MAX_EFFECT_TAG_LENGTH));
            }
        }

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
                span.entitySpin(buf.readFloat());
            }
            if (buf.readBoolean()) {
                span.setEntityAnimation(buf.readUtf(MAX_ID_LENGTH));
            }
            if (buf.readBoolean()) {
                span.entityNbt(buf.readUtf(MAX_EFFECT_TAG_LENGTH));
            }
        }

        if (buf.readBoolean()) {
            span.clickAction(buf.readUtf(MAX_ID_LENGTH));
            span.clickValue(buf.readUtf(MAX_EFFECT_TAG_LENGTH));
        }
        if (buf.readBoolean()) {
            span.hoverAction(buf.readUtf(MAX_ID_LENGTH));
            span.hoverValue(buf.readUtf(MAX_EFFECT_TAG_LENGTH));
        }

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

                    LOGGER.warn("Failed to decode effect: {}", effectTag, e);
                }
            }
        }

        return span;
    }
}
