package net.tysontheember.emberstextapi.mixin.client;

import org.joml.Matrix4f;

import java.util.IdentityHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.tysontheember.emberstextapi.core.render.EmbersBakedGlyph;
import net.tysontheember.emberstextapi.core.render.GlyphEffectRuntime;
import net.tysontheember.emberstextapi.core.render.GlyphRenderSettings;
import net.tysontheember.emberstextapi.core.render.InlineAttachmentRenderer;
import net.tysontheember.emberstextapi.core.style.EmbersStyle;
import net.tysontheember.emberstextapi.core.style.SpanEffectState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(targets = "net.minecraft.client.gui.Font$StringRenderOutput", priority = 1200)
public abstract class StringRenderOutputMixin {
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
    private Font font;
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

    @Unique
    private final IdentityHashMap<SpanEffectState, Boolean> emberstextapi$renderedAttachments = new IdentityHashMap<>();

    @Inject(method = "accept", at = @At("HEAD"), cancellable = true)
    private void emberstextapi$accept(int index, Style style, int codePoint, CallbackInfoReturnable<Boolean> cir) {
        SpanEffectState state = style instanceof EmbersStyle embers ? embers.emberstextapi$getSpanEffectState() : null;
        if (state == null || state.isEmpty()) {
            return;
        }

        Font fontInstance = this.font;
        FontAccess access = (FontAccess) fontInstance;
        FontSet fontSet = access.callGetFontSet(style.getFont());
        GlyphInfo glyphInfo = fontSet.getGlyphInfo(codePoint, access.getFilterFishyGlyphs());
        BakedGlyph bakedGlyph = style.isObfuscated() && codePoint != 32 ? fontSet.getRandomGlyph(glyphInfo)
                : fontSet.getGlyph(codePoint);

        float alpha = this.a;
        float red = this.r;
        float green = this.g;
        float blue = this.b;
        TextColor textColor = style.getColor();
        if (textColor != null) {
            int value = textColor.getValue();
            red = (float) (value >> 16 & 0xFF) / 255.0f * this.dimFactor;
            green = (float) (value >> 8 & 0xFF) / 255.0f * this.dimFactor;
            blue = (float) (value & 0xFF) / 255.0f * this.dimFactor;
        }

        float shadowOffset = dropShadow ? glyphInfo.getShadowOffset() : 0.0f;
        GlyphRenderSettings settings = new GlyphRenderSettings(index, codePoint, dropShadow, this.x, this.y, red, green, blue,
                alpha, shadowOffset, glyphInfo, bakedGlyph, fontSet, style, state);
        settings.setPose(this.pose);
        VertexConsumer consumer = this.bufferSource.getBuffer(bakedGlyph.renderType(this.mode));
        settings.setVertexConsumer(consumer);

        GlyphEffectRuntime.apply(settings);

        if (!dropShadow && settings.visible() && state != null && !state.attachments().isEmpty()
                && !emberstextapi$renderedAttachments.containsKey(state)) {
            float attachmentAdvance = InlineAttachmentRenderer.render(fontInstance, settings, this.bufferSource, this.packedLightCoords);
            if (attachmentAdvance > 0.0f) {
                settings.translate(attachmentAdvance, 0.0f);
                this.x += attachmentAdvance;
                emberstextapi$renderedAttachments.put(state, Boolean.TRUE);
            }
        }

        BakedGlyph glyph = settings.bakedGlyph();
        if (glyph != bakedGlyph) {
            consumer = this.bufferSource.getBuffer(glyph.renderType(this.mode));
            settings.setVertexConsumer(consumer);
        }

        if (!settings.visible() || settings.alpha() <= 0.0f || glyph instanceof EmptyGlyph) {
            this.x += glyphInfo.getAdvance(style.isBold());
            cir.setReturnValue(true);
            return;
        }

        Matrix4f pose = settings.pose();
        ((EmbersBakedGlyph) glyph).emberstextapi$render(settings, style.isItalic(), 0.0f, pose, consumer, packedLightCoords);
        if (style.isBold()) {
            ((EmbersBakedGlyph) glyph).emberstextapi$render(settings, style.isItalic(), glyphInfo.getBoldOffset(), pose,
                    consumer, packedLightCoords);
        }

        float glyphWidth = glyphInfo.getAdvance(style.isBold());
        float baseX = settings.renderX() - shadowOffset;
        float baseY = settings.renderY() - shadowOffset;
        if (settings.alpha() > 0.0f && style.isStrikethrough()) {
            this.addEffect(new BakedGlyph.Effect(baseX - 1.0f, baseY + 4.5f, baseX + glyphWidth, baseY + 3.5f, 0.01f,
                    settings.red(), settings.green(), settings.blue(), settings.alpha()));
        }
        if (settings.alpha() > 0.0f && style.isUnderlined()) {
            this.addEffect(new BakedGlyph.Effect(baseX - 1.0f, baseY + 9.0f, baseX + glyphWidth, baseY + 8.0f, 0.01f,
                    settings.red(), settings.green(), settings.blue(), settings.alpha()));
        }

        this.x += glyphWidth;
        cir.setReturnValue(true);
    }
}
