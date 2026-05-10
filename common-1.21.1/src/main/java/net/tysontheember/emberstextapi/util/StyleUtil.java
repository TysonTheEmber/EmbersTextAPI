package net.tysontheember.emberstextapi.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.accessor.ETAStyle;

import java.util.ArrayList;
import java.util.List;

public class StyleUtil {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(StyleUtil.class);

    public static Style cloneAndAddEffects(Style original, List<Effect> newEffects) {
        if (newEffects == null || newEffects.isEmpty()) {
            return original;
        }

        Style cloned = Style.EMPTY
                .withColor(original.getColor())
                .withBold(original.isBold())
                .withItalic(original.isItalic())
                .withUnderlined(original.isUnderlined())
                .withStrikethrough(original.isStrikethrough())
                .withObfuscated(original.isObfuscated())
                .withClickEvent(original.getClickEvent())
                .withHoverEvent(original.getHoverEvent())
                .withInsertion(original.getInsertion())
                .withFont(original.getFont());

        ETAStyle etaStyle = (ETAStyle) cloned;
        ETAStyle originalEta = (ETAStyle) original;
        ImmutableList<Effect> existingEffects = originalEta.emberstextapi$getEffects();

        List<Effect> mergedEffects = new ArrayList<>();
        if (!existingEffects.isEmpty()) {
            mergedEffects.addAll(existingEffects);
        }
        mergedEffects.addAll(newEffects);

        etaStyle.emberstextapi$setEffects(ImmutableList.copyOf(mergedEffects));

        return cloned;
    }

    public static Style addEffects(Style style, List<Effect> effects) {
        return cloneAndAddEffects(style, effects);
    }

    public static Style withEffects(List<Effect> effects) {
        if (effects == null || effects.isEmpty()) {
            return Style.EMPTY;
        }

        Style style = Style.EMPTY.withColor((net.minecraft.network.chat.TextColor)null);
        ETAStyle etaStyle = (ETAStyle) style;
        etaStyle.emberstextapi$setEffects(ImmutableList.copyOf(effects));
        return style;
    }

    public static Style applyTextSpanFormatting(Style original, net.tysontheember.emberstextapi.immersivemessages.api.TextSpan span) {
        if (span == null) {
            return original;
        }

        Style result = original;

        if (span.getBold() != null && span.getBold()) {
            result = result.withBold(true);
        }
        if (span.getItalic() != null && span.getItalic()) {
            result = result.withItalic(true);
        }
        if (span.getUnderline() != null && span.getUnderline()) {
            result = result.withUnderlined(true);
        }
        if (span.getStrikethrough() != null && span.getStrikethrough()) {
            result = result.withStrikethrough(true);
        }
        if (span.getObfuscated() != null && span.getObfuscated()) {
            result = result.withObfuscated(true);
        }

        if (span.getFont() != null) {
            result = result.withFont(span.getFont());
        }

        List<Effect> effects = span.getEffects();
        if (effects != null && !effects.isEmpty()) {
            result = cloneAndAddEffects(result, effects);
        }

        if (span.getItemId() != null) {
            result = cloneAndAddItem(
                result,
                span.getItemId(),
                span.getItemCount() != null ? span.getItemCount() : 1,
                span.getItemOffsetX() != null ? span.getItemOffsetX() : -4.0f,
                span.getItemOffsetY() != null ? span.getItemOffsetY() : -4.0f
            );
            if (span.getItemNbt() != null) {
                ((ETAStyle) result).emberstextapi$setItemNbt(span.getItemNbt());
            }
        }

        if (span.getEntityId() != null) {
            result = cloneAndAddEntity(
                result,
                span.getEntityId(),
                span.getEntityScale() != null ? span.getEntityScale() : 1.0f,
                span.getEntityOffsetX() != null ? span.getEntityOffsetX() : 0f,
                span.getEntityOffsetY() != null ? span.getEntityOffsetY() : 0f,
                span.getEntityYaw() != null ? span.getEntityYaw() : 45f,
                span.getEntityPitch() != null ? span.getEntityPitch() : 0f,
                span.getEntityRoll() != null ? span.getEntityRoll() : 0f,
                span.getEntityLighting() != null ? span.getEntityLighting() : 15,
                span.getEntitySpin(),
                span.getEntityAnimation()
            );
            if (span.getEntityNbt() != null) {
                ((ETAStyle) result).emberstextapi$setEntityNbt(span.getEntityNbt());
            }
        }

        if (span.getClickAction() != null && span.getClickValue() != null) {
            ClickEvent click = buildClickEventLegacy(span.getClickAction(), span.getClickValue());
            if (click != null) {
                result = result.withClickEvent(click);
            }
        }
        if (span.getHoverAction() != null && span.getHoverValue() != null) {
            result = result.withHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    Component.literal(span.getHoverValue())));
        }

        return result;
    }

    public static Style withItem(String itemId, int count, float offsetX, float offsetY) {
        if (itemId == null || itemId.isEmpty()) {
            return Style.EMPTY;
        }

        Style style = Style.EMPTY.withColor((net.minecraft.network.chat.TextColor)null);
        ETAStyle etaStyle = (ETAStyle) style;
        etaStyle.emberstextapi$setItemId(itemId);
        etaStyle.emberstextapi$setItemCount(Math.max(1, count));
        etaStyle.emberstextapi$setItemOffsetX(offsetX);
        etaStyle.emberstextapi$setItemOffsetY(offsetY);

        return style;
    }

    public static Style withItem(String itemId, int count) {
        return withItem(itemId, count, -4.0f, -4.0f);
    }

    public static Style withItem(String itemId) {
        return withItem(itemId, 1, -4.0f, -4.0f);
    }

    public static Style cloneAndAddItem(Style original, String itemId, int count, float offsetX, float offsetY) {
        if (itemId == null || itemId.isEmpty()) {
            return original;
        }

        Style cloned = Style.EMPTY
                .withColor(original.getColor())
                .withBold(original.isBold())
                .withItalic(original.isItalic())
                .withUnderlined(original.isUnderlined())
                .withStrikethrough(original.isStrikethrough())
                .withObfuscated(original.isObfuscated())
                .withClickEvent(original.getClickEvent())
                .withHoverEvent(original.getHoverEvent())
                .withInsertion(original.getInsertion())
                .withFont(original.getFont());

        ETAStyle etaStyle = (ETAStyle) cloned;
        ETAStyle originalEta = (ETAStyle) original;

        ImmutableList<Effect> existingEffects = originalEta.emberstextapi$getEffects();
        if (!existingEffects.isEmpty()) {
            etaStyle.emberstextapi$setEffects(existingEffects);
        }

        etaStyle.emberstextapi$setItemId(itemId);
        etaStyle.emberstextapi$setItemCount(Math.max(1, count));
        etaStyle.emberstextapi$setItemOffsetX(offsetX);
        etaStyle.emberstextapi$setItemOffsetY(offsetY);

        return cloned;
    }

    public static Style cloneAndAddEntity(
            Style original,
            String entityId,
            float scale,
            float offsetX,
            float offsetY,
            float yaw,
            float pitch,
            float roll,
            int lighting,
            Float spin,
            String animation
    ) {
        if (entityId == null || entityId.isEmpty()) {
            return original;
        }

        Style cloned = Style.EMPTY
                .withColor(original.getColor())
                .withBold(original.isBold())
                .withItalic(original.isItalic())
                .withUnderlined(original.isUnderlined())
                .withStrikethrough(original.isStrikethrough())
                .withObfuscated(original.isObfuscated())
                .withClickEvent(original.getClickEvent())
                .withHoverEvent(original.getHoverEvent())
                .withInsertion(original.getInsertion())
                .withFont(original.getFont());

        ETAStyle etaStyle = (ETAStyle) cloned;
        ETAStyle originalEta = (ETAStyle) original;

        ImmutableList<Effect> existingEffects = originalEta.emberstextapi$getEffects();
        if (!existingEffects.isEmpty()) {
            etaStyle.emberstextapi$setEffects(existingEffects);
        }

        etaStyle.emberstextapi$setEntityId(entityId);
        etaStyle.emberstextapi$setEntityScale(scale);
        etaStyle.emberstextapi$setEntityOffsetX(offsetX);
        etaStyle.emberstextapi$setEntityOffsetY(offsetY);
        etaStyle.emberstextapi$setEntityYaw(yaw);
        etaStyle.emberstextapi$setEntityPitch(pitch);
        etaStyle.emberstextapi$setEntityRoll(roll);
        etaStyle.emberstextapi$setEntityLighting(Math.max(0, Math.min(15, lighting)));
        if (spin != null) {
            etaStyle.emberstextapi$setEntitySpin(spin);
        }
        if (animation != null && !animation.isEmpty()) {
            etaStyle.emberstextapi$setEntityAnimation(animation);
        }

        return cloned;
    }

    private static ClickEvent buildClickEventLegacy(String action, String value) {
        if (action == null || value == null) return null;
        return switch (action) {
            case "open_url"          -> new ClickEvent(ClickEvent.Action.OPEN_URL, value);
            case "run_command"       -> new ClickEvent(ClickEvent.Action.RUN_COMMAND, value);
            case "suggest_command"   -> new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, value);
            case "copy_to_clipboard" -> new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, value);
            case "change_page" -> {
                try {
                    yield new ClickEvent(ClickEvent.Action.CHANGE_PAGE, Integer.toString(Integer.parseInt(value)));
                } catch (NumberFormatException e) {
                    LOGGER.warn("<click action='change_page'> expects integer, got '{}'", value);
                    yield null;
                }
            }
            default -> null;
        };
    }
}
