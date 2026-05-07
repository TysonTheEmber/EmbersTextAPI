package net.tysontheember.emberstextapi.mixin.client;

import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.accessor.ETAStyle;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.util.EffectApplicator;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput", priority = 1200)
public abstract class StringRenderOutputMixin {

    @Unique
    private static final Logger emberstextapi$LOGGER = LoggerFactory.getLogger("EmbersTextAPI/StringRenderOutput");

    @Shadow
    @Final
    private Matrix4f pose;

    @Shadow
    @Final
    private MultiBufferSource bufferSource;

    @Shadow
    private float x;

    @Shadow
    private float y;

    @Shadow(remap = false, aliases = {"f_92938_", "field_24240", "b"})
    @Final
    private Font this$0;

    @Shadow
    @Final
    private boolean dropShadow;

    @Shadow
    @Final
    private float dimFactor;

    @Shadow
    @Final
    private float a;

    @Shadow
    @Final
    private float b;

    @Shadow
    @Final
    private float r;

    @Shadow
    @Final
    private float g;

    @Shadow
    @Final
    private Font.DisplayMode mode;

    @Shadow
    @Final
    private int packedLightCoords;

    @Shadow
    protected abstract void addEffect(BakedGlyph.Effect effect);

    @Inject(method = "accept", at = @At("HEAD"), cancellable = true)
    private void emberstextapi$accept(int index, Style style, int codepoint, CallbackInfoReturnable<Boolean> cir) {
        ETAStyle etaStyle = (ETAStyle) style;

        String itemId = etaStyle.emberstextapi$getItemId();
        if (itemId != null) {
            emberstextapi$renderItem(etaStyle, itemId);
            cir.setReturnValue(true);
            return;
        }

        String entityId = etaStyle.emberstextapi$getEntityId();
        if (entityId != null) {
            emberstextapi$renderEntity(etaStyle, entityId);
            cir.setReturnValue(true);
            return;
        }

        java.util.List<Effect> effects = etaStyle.emberstextapi$getEffects().asList();

        if (effects.isEmpty()) {
            return;
        }

        FontAccess fontAccess = (FontAccess) this$0;
        FontSet fontSet = fontAccess.callGetFontSet(style.getFont());
        GlyphInfo glyphInfo = fontSet.getGlyphInfo(codepoint, fontAccess.getFilterFishyGlyphs());
        BakedGlyph bakedGlyph = style.isObfuscated() && codepoint != 32
                ? fontSet.getRandomGlyph(glyphInfo)
                : fontSet.getGlyph(codepoint);

        float red, green, blue;
        float alpha = this.a;

        TextColor textColor = style.getColor();
        if (textColor != null) {
            int colorValue = textColor.getValue();
            red = ((colorValue >> 16) & 0xFF) / 255.0f;
            green = ((colorValue >> 8) & 0xFF) / 255.0f;
            blue = (colorValue & 0xFF) / 255.0f;
        } else {
            float dim = this.dimFactor == 0f ? 1.0f : this.dimFactor;
            red = this.r / dim;
            green = this.g / dim;
            blue = this.b / dim;
        }

        float shadowOffset = this.dropShadow ? glyphInfo.getShadowOffset() : 0.0f;

        EffectSettings settings = EffectApplicator.buildSettings(
                etaStyle, style, index, codepoint,
                this.x, this.y, shadowOffset,
                red, green, blue, alpha, this.dropShadow
        );

        settings.charAdvance = glyphInfo.getAdvance(style.isBold());
        EffectApplicator.applyEffects(effects, settings);

        if (this.dropShadow) {
            settings.r *= this.dimFactor;
            settings.g *= this.dimFactor;
            settings.b *= this.dimFactor;
            if (settings.hasSiblings()) {
                for (EffectSettings sibling : settings.getSiblingsOrEmpty()) {
                    sibling.r *= this.dimFactor;
                    sibling.g *= this.dimFactor;
                    sibling.b *= this.dimFactor;
                }
            }
        }

        if (!(bakedGlyph instanceof EmptyGlyph)) {
            EffectApplicator.renderChar(settings, codepoint, style, fontSet, glyphInfo, bakedGlyph,
                    this.pose, this.bufferSource, this.mode, this.packedLightCoords, this$0.lineHeight);

            if (settings.hasSiblings()) {
                for (EffectSettings sibling : settings.getSiblingsOrEmpty()) {
                    EffectApplicator.renderChar(sibling, codepoint, style, fontSet, glyphInfo, bakedGlyph,
                            this.pose, this.bufferSource, this.mode, this.packedLightCoords, this$0.lineHeight);
                }
            }
        }

        red = settings.r;
        green = settings.g;
        blue = settings.b;
        alpha = settings.a;

        float glyphWidth = glyphInfo.getAdvance(style.isBold());

        if (alpha != 0 && style.isStrikethrough()) {
            this.addEffect(new BakedGlyph.Effect(
                    this.x + shadowOffset - 1.0f,
                    this.y + shadowOffset + 4.5f,
                    this.x + shadowOffset + glyphWidth,
                    this.y + shadowOffset + 4.5f - 1.0f,
                    0.01f,
                    red,
                    green,
                    blue,
                    alpha
            ));
        }

        if (alpha != 0 && style.isUnderlined()) {
            this.addEffect(new BakedGlyph.Effect(
                    this.x + shadowOffset - 1.0f,
                    this.y + shadowOffset + 9.0f,
                    this.x + shadowOffset + glyphWidth,
                    this.y + shadowOffset + 9.0f - 1.0f,
                    0.01f,
                    red,
                    green,
                    blue,
                    alpha
            ));
        }

        this.x += glyphWidth;
        cir.setReturnValue(true);
    }

    @Unique
    private void emberstextapi$renderItem(ETAStyle etaStyle, String itemId) {
        try {
            net.minecraft.resources.ResourceLocation itemLocation = net.minecraft.resources.ResourceLocation.tryParse(itemId);
            if (itemLocation == null) {
                return;
            }

            net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemLocation);
            if (item == null) {
                return;
            }

            int count = etaStyle.emberstextapi$getItemCount() != null ? etaStyle.emberstextapi$getItemCount() : 1;
            net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item, count);

            String itemNbt = etaStyle.emberstextapi$getItemNbt();
            if (itemNbt != null && !itemNbt.isEmpty()) {
                try {
                    net.minecraft.nbt.CompoundTag nbtTag = net.minecraft.nbt.TagParser.parseTag(itemNbt);
                    stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(nbtTag));
                } catch (Exception e) {
                }
            }

            float offsetX = etaStyle.emberstextapi$getItemOffsetX() != null ? etaStyle.emberstextapi$getItemOffsetX() : -4.0f;
            float offsetY = etaStyle.emberstextapi$getItemOffsetY() != null ? etaStyle.emberstextapi$getItemOffsetY() : -4.0f;
            int itemSize = 16;

            Matrix4f itemPose = new Matrix4f(this.pose);
            itemPose.translate(this.x + offsetX, this.y + offsetY, 0);

            var mc = net.minecraft.client.Minecraft.getInstance();

            if (this.bufferSource instanceof net.minecraft.client.renderer.MultiBufferSource.BufferSource bs) {
                bs.endBatch();
            }

            net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource;
            if (this.bufferSource instanceof net.minecraft.client.renderer.MultiBufferSource.BufferSource bs) {
                bufferSource = bs;
            } else {
                bufferSource = mc.renderBuffers().bufferSource();
            }

            var guiGraphics = new net.minecraft.client.gui.GuiGraphics(mc, bufferSource);
            guiGraphics.pose().mulPose(itemPose);
            guiGraphics.renderItem(stack, 0, 0);
            bufferSource.endBatch();

            this.x += offsetX + itemSize;

        } catch (Exception e) {
        }
    }

    @Unique
    private void emberstextapi$renderEntity(ETAStyle etaStyle, String entityId) {
        try {
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }

            float scale = etaStyle.emberstextapi$getEntityScale() != null ? etaStyle.emberstextapi$getEntityScale() : 1.0f;
            float offsetX = etaStyle.emberstextapi$getEntityOffsetX() != null ? etaStyle.emberstextapi$getEntityOffsetX() : 0f;
            float offsetY = etaStyle.emberstextapi$getEntityOffsetY() != null ? etaStyle.emberstextapi$getEntityOffsetY() : 0f;
            float yaw = etaStyle.emberstextapi$getEntityYaw() != null ? etaStyle.emberstextapi$getEntityYaw() : 45f;
            float pitch = etaStyle.emberstextapi$getEntityPitch() != null ? etaStyle.emberstextapi$getEntityPitch() : 0f;
            float roll = etaStyle.emberstextapi$getEntityRoll() != null ? etaStyle.emberstextapi$getEntityRoll() : 0f;
            int lighting = etaStyle.emberstextapi$getEntityLighting() != null ? etaStyle.emberstextapi$getEntityLighting() : 15;
            Float spin = etaStyle.emberstextapi$getEntitySpin();
            int entitySize = (int)(16 * scale);

            if (this.bufferSource instanceof net.minecraft.client.renderer.MultiBufferSource.BufferSource bs) {
                bs.endBatch();
            }

            net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource;
            if (this.bufferSource instanceof net.minecraft.client.renderer.MultiBufferSource.BufferSource bs) {
                bufferSource = bs;
            } else {
                bufferSource = mc.renderBuffers().bufferSource();
            }

            var guiGraphics = new net.minecraft.client.gui.GuiGraphics(mc, bufferSource);
            guiGraphics.pose().mulPose(this.pose);

            int renderedWidth = net.tysontheember.emberstextapi.immersivemessages.util.EntityRenderer.render(
                    guiGraphics,
                    entityId,
                    this.x,
                    this.y,
                    scale,
                    offsetX,
                    offsetY,
                    yaw,
                    pitch,
                    roll,
                    lighting,
                    spin,
                    etaStyle.emberstextapi$getEntityNbt()
            );

            bufferSource.endBatch();

            if (renderedWidth > 0) {
                this.x += renderedWidth;
            } else {
                this.x += entitySize;
            }

        } catch (Exception e) {
            emberstextapi$LOGGER.debug("Entity rendering failed for {}: {}", entityId, e.getMessage());
        }
    }
}
