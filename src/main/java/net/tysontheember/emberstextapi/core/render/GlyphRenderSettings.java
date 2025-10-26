package net.tysontheember.emberstextapi.core.render;

import java.util.Objects;

import org.joml.Matrix4f;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.Style;
import net.tysontheember.emberstextapi.core.style.SpanEffectState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Mutable rendering payload that is enriched by the global glyph effect pipeline before a
 * character is drawn.
 */
@OnlyIn(Dist.CLIENT)
public final class GlyphRenderSettings {
    private final int logicalIndex;
    private int codePoint;
    private final boolean shadow;
    private float baseX;
    private float baseY;
    private float renderX;
    private float renderY;
    private float red;
    private float green;
    private float blue;
    private float alpha;
    private final float shadowOffset;
    private final GlyphInfo glyphInfo;
    private BakedGlyph bakedGlyph;
    private final FontSet fontSet;
    private final Style style;
    private final SpanEffectState effectState;
    private boolean visible = true;
    private Matrix4f pose;
    private VertexConsumer vertexConsumer;

    public GlyphRenderSettings(int logicalIndex, int codePoint, boolean shadow, float baseX, float baseY, float red, float green,
            float blue, float alpha, float shadowOffset, GlyphInfo glyphInfo, BakedGlyph bakedGlyph, FontSet fontSet, Style style,
            SpanEffectState effectState) {
        this.logicalIndex = logicalIndex;
        this.codePoint = codePoint;
        this.shadow = shadow;
        this.baseX = baseX;
        this.baseY = baseY;
        this.renderX = baseX + shadowOffset;
        this.renderY = baseY + shadowOffset;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.shadowOffset = shadowOffset;
        this.glyphInfo = Objects.requireNonNull(glyphInfo, "glyphInfo");
        this.bakedGlyph = Objects.requireNonNull(bakedGlyph, "bakedGlyph");
        this.fontSet = Objects.requireNonNull(fontSet, "fontSet");
        this.style = Objects.requireNonNull(style, "style");
        this.effectState = effectState;
    }

    public int logicalIndex() {
        return logicalIndex;
    }

    public int codePoint() {
        return codePoint;
    }

    public void setCodePoint(int codePoint) {
        this.codePoint = codePoint;
    }

    public boolean dropShadow() {
        return shadow;
    }

    public float baseX() {
        return baseX;
    }

    public float baseY() {
        return baseY;
    }

    public float renderX() {
        return renderX;
    }

    public void setRenderX(float value) {
        this.renderX = value;
    }

    public float renderY() {
        return renderY;
    }

    public void setRenderY(float value) {
        this.renderY = value;
    }

    public float red() {
        return red;
    }

    public void setRed(float value) {
        this.red = value;
    }

    public float green() {
        return green;
    }

    public void setGreen(float value) {
        this.green = value;
    }

    public float blue() {
        return blue;
    }

    public void setBlue(float value) {
        this.blue = value;
    }

    public float alpha() {
        return alpha;
    }

    public void setAlpha(float value) {
        this.alpha = value;
    }

    public float shadowOffset() {
        return shadowOffset;
    }

    public GlyphInfo glyphInfo() {
        return glyphInfo;
    }

    public BakedGlyph bakedGlyph() {
        return bakedGlyph;
    }

    public void setBakedGlyph(BakedGlyph bakedGlyph) {
        this.bakedGlyph = Objects.requireNonNull(bakedGlyph, "bakedGlyph");
    }

    public FontSet fontSet() {
        return fontSet;
    }

    public Style style() {
        return style;
    }

    public SpanEffectState effectState() {
        return effectState;
    }

    public boolean visible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setPose(Matrix4f pose) {
        this.pose = pose;
    }

    public Matrix4f pose() {
        return pose;
    }

    public void setVertexConsumer(VertexConsumer vertexConsumer) {
        this.vertexConsumer = vertexConsumer;
    }

    public VertexConsumer vertexConsumer() {
        return vertexConsumer;
    }

    public void translate(float dx, float dy) {
        renderX += dx;
        renderY += dy;
    }
}
