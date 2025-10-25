package net.tysontheember.emberstextapi.mixin.client;

import java.util.List;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.client.text.EffectContext;
import net.tysontheember.emberstextapi.client.text.EffectSettings;
import net.tysontheember.emberstextapi.client.text.RenderFastPath;
import net.tysontheember.emberstextapi.client.text.SpanEffect;
import net.tysontheember.emberstextapi.client.text.SpanEffectRegistry;
import net.tysontheember.emberstextapi.duck.ETAStyle;

@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput", priority = 1200)
public abstract class FontStringRenderOutputMixin {
    @Shadow
    @Final
    private Matrix4f pose;

    @Shadow
    @Final
    private MultiBufferSource bufferSource;

    @Shadow
    float x;

    @Shadow
    float y;

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
    private float r;

    @Shadow
    @Final
    private float g;

    @Shadow
    @Final
    private float b;

    @Shadow
    @Final
    private Font.DisplayMode mode;

    @Shadow
    @Final
    private int packedLightCoords;

    @Shadow(remap = false, aliases = {"f_92938_", "field_24240", "b"})
    @Final
    private Font emberstextapi$font;

    @Shadow
    protected abstract void addEffect(BakedGlyph.Effect effect);

    @Unique
    private static final ThreadLocal<EffectSettings> EMBERSTEXTAPI$EFFECT_SETTINGS = ThreadLocal
            .withInitial(EffectSettings::new);

    @Inject(method = "accept(ILnet/minecraft/network/chat/Style;I)Z", at = @At("HEAD"), cancellable = true)
    private void emberstextapi$renderWithEffects(int glyphIndex, Style style, int codePoint,
            CallbackInfoReturnable<Boolean> cir) {
        if (RenderFastPath.shouldBypass(style)) {
            return;
        }
        if (!(style instanceof ETAStyle etaStyle)) {
            return;
        }
        List<SpanEffect> effects = etaStyle.eta$getEffects();
        if (effects.isEmpty()) {
            return;
        }

        FontAccessor fontAccessor = (FontAccessor) this.emberstextapi$font;
        FontSet fontSet = fontAccessor.emberstextapi$callGetFontSet(style.getFont());
        boolean filterFishy = fontAccessor.getFilterFishyGlyphs();
        com.mojang.blaze3d.font.GlyphInfo glyphInfo = fontSet.getGlyphInfo(codePoint, filterFishy);
        BakedGlyph bakedGlyph = style.isObfuscated() && codePoint != 32 ? fontSet.getRandomGlyph(glyphInfo)
                : fontSet.getGlyph(codePoint);
        if (bakedGlyph instanceof EmptyGlyph) {
            this.x += glyphInfo.getAdvance(style.isBold());
            cir.setReturnValue(true);
            cir.cancel();
            return;
        }

        float baseAlpha = this.a;
        float baseRed = this.r;
        float baseGreen = this.g;
        float baseBlue = this.b;
        TextColor explicit = style.getColor();
        if (explicit != null) {
            int value = explicit.getValue();
            baseRed = (value >> 16 & 0xFF) / 255.0F;
            baseGreen = (value >> 8 & 0xFF) / 255.0F;
            baseBlue = (value & 0xFF) / 255.0F;
        }
        float dim = this.dropShadow ? this.dimFactor : 1.0F;
        baseRed *= dim;
        baseGreen *= dim;
        baseBlue *= dim;

        EffectSettings settings = EMBERSTEXTAPI$EFFECT_SETTINGS.get();
        settings.reset();
        settings.setCodePoint(codePoint);
        settings.setGlyphIndex(glyphIndex + Math.max(0, etaStyle.eta$getTypewriterIndex()));
        settings.setShadow(this.dropShadow);
        settings.setBold(style.isBold());
        settings.setItalic(style.isItalic());
        float shadowOffset = this.dropShadow ? glyphInfo.getShadowOffset() : 0.0F;
        settings.setX(this.x + shadowOffset);
        settings.setY(this.y + shadowOffset);
        settings.setPartialTicks(0.0F);
        settings.setColor(baseRed, baseGreen, baseBlue, baseAlpha);

        boolean allowTint = this.mode == Font.DisplayMode.NORMAL;
        int gradientColor = SpanEffectRegistry.applyTint(style, settings.getGlyphIndex(), settings.getCodePoint(), allowTint);
        if (gradientColor != -1) {
            float tintedRed = ((gradientColor >> 16) & 0xFF) / 255.0F;
            float tintedGreen = ((gradientColor >> 8) & 0xFF) / 255.0F;
            float tintedBlue = (gradientColor & 0xFF) / 255.0F;
            float tintedAlpha = ((gradientColor >>> 24) & 0xFF) / 255.0F;
            if (this.dropShadow) {
                tintedRed *= this.dimFactor;
                tintedGreen *= this.dimFactor;
                tintedBlue *= this.dimFactor;
            }
            settings.setColor(tintedRed, tintedGreen, tintedBlue, tintedAlpha != 0.0F ? tintedAlpha : baseAlpha);
        }

        SpanEffectRegistry.applyEffects(EffectContext.obtain(), settings, effects, etaStyle, glyphInfo);

        int effectiveCodePoint = settings.getCodePoint();
        if (effectiveCodePoint != codePoint) {
            glyphInfo = fontSet.getGlyphInfo(effectiveCodePoint, filterFishy);
            bakedGlyph = fontSet.getGlyph(effectiveCodePoint);
            if (bakedGlyph instanceof EmptyGlyph) {
                this.x += glyphInfo.getAdvance(style.isBold());
                cir.setReturnValue(true);
                cir.cancel();
                return;
            }
        }

        VertexConsumer consumer = this.bufferSource.getBuffer(bakedGlyph.renderType(this.mode));
        boolean italic = style.isItalic();
        float drawX = settings.getX();
        float drawY = settings.getY();
        float red = settings.getRed();
        float green = settings.getGreen();
        float blue = settings.getBlue();
        float alpha = settings.getAlpha();

        bakedGlyph.render(italic, drawX, drawY, this.pose, consumer, red, green, blue, alpha, this.packedLightCoords);
        if (style.isBold()) {
            float boldOffset = glyphInfo.getBoldOffset();
            bakedGlyph.render(italic, drawX + boldOffset, drawY, this.pose, consumer, red, green, blue, alpha,
                    this.packedLightCoords);
        }

        float glyphAdvance = glyphInfo.getAdvance(style.isBold());
        float lineStart = this.x + shadowOffset - 1.0F;
        float lineEnd = lineStart + glyphAdvance;
        if (alpha != 0.0F && style.isStrikethrough()) {
            this.addEffect(new BakedGlyph.Effect(lineStart, this.y + shadowOffset + 4.5F, lineEnd,
                    this.y + shadowOffset + 3.5F, 0.01F, red, green, blue, alpha));
        }
        if (alpha != 0.0F && style.isUnderlined()) {
            this.addEffect(new BakedGlyph.Effect(lineStart, this.y + shadowOffset + 9.0F, lineEnd,
                    this.y + shadowOffset + 8.0F, 0.01F, red, green, blue, alpha));
        }

        this.x += glyphAdvance;
        cir.setReturnValue(true);
        cir.cancel();
    }
}
