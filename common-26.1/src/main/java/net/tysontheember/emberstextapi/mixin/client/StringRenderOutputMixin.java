package net.tysontheember.emberstextapi.mixin.client;

import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.tysontheember.emberstextapi.accessor.ETAStyle;
import net.tysontheember.emberstextapi.client.RotatedGlyph;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.util.EffectApplicator;
import net.tysontheember.emberstextapi.util.ImmersiveRenderBypass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Font$PreparedTextBuilder", priority = 1200)
public abstract class StringRenderOutputMixin {

    @Shadow private float x;
    @Shadow private float y;
    @Shadow @Final private Font this$0;
    @Shadow @Final private boolean drawShadow;
    @Shadow @Final private int color;

    @Shadow abstract void addEffect(TextRenderable effect);
    @Shadow abstract void addGlyph(TextRenderable.Styled glyph);
    @Shadow private int getTextColor(TextColor textColor) { throw new AssertionError(); }
    @Shadow private int getShadowColor(Style style, int textColor) { throw new AssertionError(); }

    @Inject(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At("HEAD"), cancellable = true)
    private void emberstextapi$accept(int index, Style style, int codepoint, CallbackInfoReturnable<Boolean> cir) {
        // ImmersiveMessage.renderSingleChar re-enters prepareText per glyph; let vanilla draw
        // the pre-positioned char and don't re-apply effects that would corrupt scroll state.
        if (ImmersiveRenderBypass.isActive()) {
            return;
        }

        ETAStyle etaStyle = (ETAStyle) (Object) style;

        String itemId = etaStyle.emberstextapi$getItemId();
        if (itemId != null) {
            float offsetX = etaStyle.emberstextapi$getItemOffsetX() != null ? etaStyle.emberstextapi$getItemOffsetX() : -4.0f;
            this.x += offsetX + 16;
            cir.setReturnValue(true);
            return;
        }

        String entityId = etaStyle.emberstextapi$getEntityId();
        if (entityId != null) {
            float scale = etaStyle.emberstextapi$getEntityScale() != null ? etaStyle.emberstextapi$getEntityScale() : 1.0f;
            this.x += (int)(16 * scale);
            cir.setReturnValue(true);
            return;
        }

        java.util.List<Effect> effects = etaStyle.emberstextapi$getEffects().asList();
        if (effects.isEmpty()) {
            return;
        }

        FontAccess fontAccess = (FontAccess) this$0;
        GlyphSource glyphSource = fontAccess.callGetGlyphSource(style.getFont());
        BakedGlyph bakedGlyph = glyphSource.getGlyph(codepoint);
        GlyphInfo glyphInfo = bakedGlyph.info();

        int textColor = this.getTextColor(style.getColor());
        float alpha = ((textColor >> 24) & 0xFF) / 255.0f;
        float red = ((textColor >> 16) & 0xFF) / 255.0f;
        float green = ((textColor >> 8) & 0xFF) / 255.0f;
        float blue = (textColor & 0xFF) / 255.0f;
        float shadowOffset = this.drawShadow ? glyphInfo.getShadowOffset() : 0.0f;

        EffectSettings settings = EffectApplicator.buildSettings(
                etaStyle, style, index, codepoint,
                this.x, this.y, shadowOffset,
                red, green, blue, alpha, false
        );
        settings.charAdvance = glyphInfo.getAdvance(style.isBold());
        EffectApplicator.applyEffects(effects, settings);

        if (settings.useRandomGlyph && codepoint != 32) {
            int glyphWidth = Mth.ceil(glyphInfo.getAdvance(false));
            bakedGlyph = glyphSource.getRandomGlyph(fontAccess.getRandom(), glyphWidth);
        } else if (settings.codepoint != codepoint) {

            BakedGlyph resolved = glyphSource.getGlyph(settings.codepoint);
            if (resolved.info().getAdvance(false) > 0) {
                bakedGlyph = resolved;
            }
        }

        int effectColor = ((int)(settings.a * 255) << 24) |
                         ((int)(settings.r * 255) << 16) |
                         ((int)(settings.g * 255) << 8) |
                         ((int)(settings.b * 255));
        int shadowColor = this.getShadowColor(style, effectColor);
        float boldOffset = style.isBold() ? glyphInfo.getBoldOffset() : 0.0f;

        TextRenderable.Styled glyph = bakedGlyph.createGlyph(
                settings.x, settings.y, effectColor, shadowColor, style, boldOffset, shadowOffset);
        if (glyph != null) {

            if (settings.rot != 0f) {
                float halfWidth = glyphInfo.getAdvance(style.isBold()) / 2f;
                float centerX = settings.x + halfWidth;
                float centerY = settings.y + 4.5f;
                glyph = new RotatedGlyph(glyph, settings.rot, centerX, centerY);
            }
            this.addGlyph(glyph);
        }

        if (settings.hasSiblings()) {
            for (EffectSettings sibling : settings.getSiblingsOrEmpty()) {
                int sibColor = ((int)(sibling.a * 255) << 24) |
                              ((int)(sibling.r * 255) << 16) |
                              ((int)(sibling.g * 255) << 8) |
                              ((int)(sibling.b * 255));
                int sibShadow = this.getShadowColor(style, sibColor);
                TextRenderable.Styled sibGlyph = bakedGlyph.createGlyph(
                        sibling.x, sibling.y, sibColor, sibShadow, style, boldOffset, shadowOffset);
                if (sibGlyph != null) {
                    if (sibling.rot != 0f) {
                        float halfWidth = glyphInfo.getAdvance(style.isBold()) / 2f;
                        sibGlyph = new RotatedGlyph(sibGlyph, sibling.rot,
                                sibling.x + halfWidth, sibling.y + 4.5f);
                    }
                    this.addGlyph(sibGlyph);
                }
            }
        }

        float glyphWidth = glyphInfo.getAdvance(style.isBold());
        this.x += glyphWidth;
        cir.setReturnValue(true);
    }
}
