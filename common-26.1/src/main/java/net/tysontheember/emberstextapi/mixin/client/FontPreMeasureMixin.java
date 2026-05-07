package net.tysontheember.emberstextapi.mixin.client;

import com.mojang.blaze3d.font.GlyphInfo;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.util.FormattedCharSequence;
import net.tysontheember.emberstextapi.accessor.ETAStyle;
import net.tysontheember.emberstextapi.immersivemessages.effects.Effect;
import net.tysontheember.emberstextapi.immersivemessages.effects.EffectSettings;
import net.tysontheember.emberstextapi.util.EffectApplicator;
import net.tysontheember.emberstextapi.util.ImmersiveRenderBypass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Font.class)
public abstract class FontPreMeasureMixin {

    @Inject(
            method = "prepareText(Lnet/minecraft/util/FormattedCharSequence;FFIZZI)Lnet/minecraft/client/gui/Font$PreparedText;",
            at = @At("HEAD")
    )
    private void emberstextapi$premeasureScrollEffects(
            FormattedCharSequence text, float x, float y, int originalColor,
            boolean drawShadow, boolean includeEmpty, int backgroundColor,
            CallbackInfoReturnable<Font.PreparedText> cir) {

        if (ImmersiveRenderBypass.isActive()) {
            return;
        }

        FontAccess fontAccess = (FontAccess) this;
        final float[] cursorX = { x };

        text.accept((index, style, codepoint) -> {
            ETAStyle etaStyle = (ETAStyle) (Object) style;

            if (etaStyle.emberstextapi$getItemId() != null) {
                float offsetX = etaStyle.emberstextapi$getItemOffsetX() != null ? etaStyle.emberstextapi$getItemOffsetX() : -4.0f;
                cursorX[0] += offsetX + 16;
                return true;
            }
            if (etaStyle.emberstextapi$getEntityId() != null) {
                float scale = etaStyle.emberstextapi$getEntityScale() != null ? etaStyle.emberstextapi$getEntityScale() : 1.0f;
                cursorX[0] += (int) (16 * scale);
                return true;
            }

            List<Effect> effects = etaStyle.emberstextapi$getEffects().asList();
            if (effects.isEmpty()) {
                return true;
            }

            GlyphSource glyphSource = fontAccess.callGetGlyphSource(style.getFont());
            BakedGlyph bakedGlyph = glyphSource.getGlyph(codepoint);
            GlyphInfo glyphInfo = bakedGlyph.info();

            EffectSettings settings = EffectApplicator.buildSettings(
                    etaStyle, style, index, codepoint,
                    cursorX[0], y, 0f,
                    0f, 0f, 0f, 0f, true
            );
            settings.charAdvance = glyphInfo.getAdvance(style.isBold());
            EffectApplicator.applyEffects(effects, settings);

            cursorX[0] += glyphInfo.getAdvance(style.isBold());
            return true;
        });
    }
}
