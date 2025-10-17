package net.tysontheember.emberstextapi.text;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.tysontheember.emberstextapi.text.TextEffect.GlyphCtx;
import net.tysontheember.emberstextapi.text.Attributes.CompiledEffect;
import net.tysontheember.emberstextapi.text.Attribute;
import net.tysontheember.emberstextapi.text.Span;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Applies compiled text effects and renders the resulting glyphs.
 */
public final class AttributedRenderer {
    private AttributedRenderer() {
    }

    public static void draw(GuiGraphics graphics, Font font, AttributedText text, float x, float y,
                             int baseColor, boolean shadow, int maxWidth, float timeSeconds) {
        if (text == null) {
            return;
        }
        String raw = text.raw();
        if (raw.isEmpty()) {
            return;
        }
        CompileContext context = CompileContext.ofClient();
        List<List<Attributes.CompiledEffect>> effectTable = buildEffectTable(text, context);
        PoseStack pose = graphics.pose();
        pose.pushPose();
        float cursorX = x;
        float cursorY = y;
        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            if (ch == '\n') {
                cursorX = x;
                cursorY += font.lineHeight;
                continue;
            }
            GlyphCtx glyph = new GlyphCtx();
            glyph.glyphIndex = i;
            glyph.color = baseColor;
            glyph.shadow = shadow;
            glyph.alpha = 1f;
            glyph.scale = 1f;
            glyph.xOffset = 0f;
            glyph.yOffset = 0f;
            List<Attributes.CompiledEffect> effects = i < effectTable.size() ? effectTable.get(i) : Collections.emptyList();
            for (Attributes.CompiledEffect compiled : effects) {
                glyph.indexInSpan = i - compiled.span().startIndex();
                compiled.effect().apply(glyph, compiled.span(), timeSeconds);
            }
            int finalColor = applyAlpha(glyph.color, glyph.alpha);
            float drawX = cursorX + glyph.xOffset;
            float drawY = cursorY + glyph.yOffset;
            graphics.drawString(font, Character.toString(ch), (int) drawX, (int) drawY, finalColor, glyph.shadow);
            float advance = font.width(Character.toString(ch));
            cursorX += advance * Math.max(0.01f, glyph.scale);
        }
        pose.popPose();
    }

    private static int applyAlpha(int color, float alpha) {
        int argb = color;
        int baseAlpha = (argb >> 24) & 0xFF;
        int combined = Math.min(255, Math.max(0, Math.round(baseAlpha * alpha)));
        return (combined << 24) | (argb & 0x00FFFFFF);
    }

    private static List<List<Attributes.CompiledEffect>> buildEffectTable(AttributedText text, CompileContext context) {
        String raw = text.raw();
        int length = raw.length();
        List<List<Attributes.CompiledEffect>> table = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            table.add(new ArrayList<>());
        }
        for (Span span : text.spans()) {
            int start = Math.max(0, Math.min(length, span.start()));
            int end = Math.max(0, Math.min(length, span.end()));
            if (start >= end) {
                continue;
            }
            for (Attribute attribute : span.attributes()) {
                Attributes.CompiledEffect compiled = Attributes.compile(attribute, start, end, context);
                if (compiled == null) {
                    continue;
                }
                for (int i = start; i < end && i < length; i++) {
                    table.get(i).add(compiled);
                }
            }
        }
        return table;
    }
}
